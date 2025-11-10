package se.sundsvall.invoicecache.integration.storage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.stream.Stream;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.invoicecache.integration.storage.util.HashUtil;
import se.sundsvall.invoicecache.util.exception.BlobIntegrityException;
import se.sundsvall.invoicecache.util.exception.BlobWriteException;

@ExtendWith(MockitoExtension.class)
class StorageSambaIntegrationTest {

	@Mock
	private StorageSambaProperties storageSambaProperties;

	@InjectMocks
	private StorageSambaIntegration storageSambaIntegration;

	@Test
	void readFile() throws IOException {
		var content = "TestString";
		var blobKey = "6dd79f2770a0bb38073b814a5ff000647b37be5abbde71ec9176c6ce0cb32a27";

		when(storageSambaProperties.targetUrl()).thenReturn("smb://samba-hostname/abc/invoice-cache/test");
		when(storageSambaProperties.cifsContext()).thenCallRealMethod();

		try (var constructor = mockConstruction(SmbFile.class, (mock, context) -> when(mock.getInputStream()).thenReturn(new ByteArrayInputStream(content.getBytes())))) {

			var result = storageSambaIntegration.readFile(blobKey);

			assertThat(result).isNotNull().isInstanceOf(SmbFile.class);
			assertThat(HashUtil.SHA256(result.getInputStream())).isEqualTo(blobKey);

			var smbFile = constructor.constructed().getFirst();
			verify(smbFile).getInputStream();
		}
		verify(storageSambaProperties).targetUrl();
		verify(storageSambaProperties).cifsContext();
	}

	@Test
	void writeFile() throws SQLException, IOException {
		var content = "TestString";
		var expectedHash = "6dd79f2770a0bb38073b814a5ff000647b37be5abbde71ec9176c6ce0cb32a27";

		var blob = Mockito.mock(Blob.class);
		var inputStream = new ByteArrayInputStream(content.getBytes());
		var outputStream = new ByteArrayOutputStream();

		when(blob.getBinaryStream()).thenReturn(inputStream);
		when(storageSambaProperties.targetUrl()).thenReturn("smb://samba-hostname/abc/invoice-cache/test");
		when(storageSambaProperties.cifsContext()).thenCallRealMethod();

		try (var constructor = mockConstruction(SmbFile.class, (mock, context) -> {
			// Directory exists
			when(mock.exists()).thenReturn(true);
			when(mock.getOutputStream()).thenReturn(outputStream);
		})) {

			var resultHash = storageSambaIntegration.writeFile(blob);

			assertThat(outputStream.toByteArray()).isEqualTo(inputStream.readAllBytes());
			assertThat(resultHash).isEqualTo(expectedHash);

			var constructed = constructor.constructed();
			assertThat(constructed).hasSize(2);
			var dirMock = constructed.get(0);
			var fileMock = constructed.get(1);

			verify(dirMock).exists();

			// Should not attempt to create directory since it already exists
			verify(dirMock, never()).mkdirs();
			verify(fileMock).getOutputStream();
		}

		verify(storageSambaProperties).targetUrl();
		verify(storageSambaProperties, times(2)).cifsContext();
	}

	@Test
	void writeFile_createDirectory() throws SQLException, IOException {
		var content = "TestString";
		var expectedHash = "6dd79f2770a0bb38073b814a5ff000647b37be5abbde71ec9176c6ce0cb32a27";

		var blob = Mockito.mock(Blob.class);
		var inputStream = new ByteArrayInputStream(content.getBytes());
		var outputStream = new ByteArrayOutputStream();

		when(blob.getBinaryStream()).thenReturn(inputStream);
		when(storageSambaProperties.targetUrl()).thenReturn("smb://samba-hostname/abc/invoice-cache/test");
		when(storageSambaProperties.cifsContext()).thenCallRealMethod();

		try (var constructor = mockConstruction(SmbFile.class, (mock, context) -> {
			// Directory does not exist
			when(mock.exists()).thenReturn(false);
			when(mock.getOutputStream()).thenReturn(outputStream);
		})) {

			var resultHash = storageSambaIntegration.writeFile(blob);

			assertThat(outputStream.toByteArray()).isEqualTo(inputStream.readAllBytes());
			assertThat(resultHash).isEqualTo(expectedHash);

			var constructed = constructor.constructed();
			assertThat(constructed).hasSize(2);
			var dirMock = constructed.get(0);
			var fileMock = constructed.get(1);

			verify(dirMock).exists();
			// Should attempt to create directory since it does not exist
			verify(dirMock).mkdirs();
			verify(fileMock).getOutputStream();
		}

		verify(storageSambaProperties).targetUrl();
		verify(storageSambaProperties, times(2)).cifsContext();
	}

	@Test
	void writeFile_throws() throws SQLException {
		var content = "TestString";
		var blob = Mockito.mock(Blob.class);

		var inputStream = new ByteArrayInputStream(content.getBytes());

		when(blob.getBinaryStream()).thenReturn(inputStream);

		try (var ignored = mockConstruction(SmbFile.class, (mock, context) -> when(mock.exists()).thenThrow(new SmbException("Random error")))) {

			assertThatThrownBy(() -> storageSambaIntegration.writeFile(blob))
				.isInstanceOf(BlobWriteException.class)
				.hasMessageContaining("Could not write file to Samba");
		}
	}

	@ParameterizedTest
	@MethodSource("verifyBlobIntegrityArgumentProvider")
	void verifyBlobIntegrity(final String content, final String expectedHash) {
		try (var ignored = mockConstruction(SmbFile.class, (mock, context) -> when(mock.getInputStream()).thenReturn(new ByteArrayInputStream(content.getBytes())))) {

			when(storageSambaProperties.targetUrl()).thenReturn("smb://samba-hostname/abc/invoice-cache/test");

			var result = storageSambaIntegration.verifyBlobIntegrity(expectedHash);

			assertThat(result).isEqualTo(expectedHash);
		}
	}

	@Test
	void verifyBlobIntegrity_throws() {
		var blobKey = "6dd79f2770a0bb38073b814a5ff000647b37be5abbde71ec9176c6ce0cb32a27";

		try (var ignored = mockConstruction(SmbFile.class, (mock, context) -> when(mock.getInputStream()).thenThrow(new IOException("Read error")))) {

			assertThatThrownBy(() -> storageSambaIntegration.verifyBlobIntegrity(blobKey))
				.isInstanceOf(BlobIntegrityException.class)
				.hasMessageContaining("Could not verify blob integrity for %s".formatted(blobKey));
		}
	}

	private static Stream<Arguments> verifyBlobIntegrityArgumentProvider() {
		return Stream.of(
			Arguments.of("TestString", "6dd79f2770a0bb38073b814a5ff000647b37be5abbde71ec9176c6ce0cb32a27"),
			Arguments.of("AnotherTestString", "8df77a731fb9cacafb233b32ace2fa58ebbbfbd48849a83c48e2d717bd333acd"),
			Arguments.of("ThirdString", "9eade98be98408fa03c656457076018866d8ac1f78c9fe9fea1b6f4efe75c2ae"));
	}

}
