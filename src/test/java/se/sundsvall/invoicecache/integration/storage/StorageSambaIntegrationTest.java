package se.sundsvall.invoicecache.integration.storage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.stream.Stream;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.zalando.problem.Problem;
import se.sundsvall.invoicecache.util.exception.BlobIntegrityException;
import se.sundsvall.invoicecache.util.exception.BlobWriteException;

@ExtendWith(MockitoExtension.class)
class StorageSambaIntegrationTest {

	@Mock
	private StorageSambaProperties storageSambaProperties;

	@InjectMocks
	private StorageSambaIntegration storageSambaIntegration;

	private static Stream<Arguments> verifyBlobIntegrityArgumentProvider() {
		return Stream.of(
			Arguments.of("TestString", "6dd79f2770a0bb38073b814a5ff000647b37be5abbde71ec9176c6ce0cb32a27"),
			Arguments.of("AnotherTestString", "8df77a731fb9cacafb233b32ace2fa58ebbbfbd48849a83c48e2d717bd333acd"),
			Arguments.of("ThirdString", "9eade98be98408fa03c656457076018866d8ac1f78c9fe9fea1b6f4efe75c2ae"));
	}

	@AfterEach
	void tearDown() {
		verifyNoMoreInteractions(storageSambaProperties);
	}

	@Test
	void readFile() throws IOException {
		final var content = "TestString";
		final var blobKey = "6dd79f2770a0bb38073b814a5ff000647b37be5abbde71ec9176c6ce0cb32a27";

		when(storageSambaProperties.targetUrl()).thenReturn("smb://samba-hostname/abc/invoice-cache/test");
		when(storageSambaProperties.cifsContext()).thenCallRealMethod();

		try (final var constructor = mockConstruction(SmbFile.class, (mock, context) -> when(mock.getInputStream()).thenReturn(new ByteArrayInputStream(content.getBytes())))) {

			final var result = storageSambaIntegration.readFile(blobKey);

			assertThat(result).isEqualTo(content.getBytes());

			final var smbFile = constructor.constructed().getFirst();
			verify(smbFile).getInputStream();
		}
		verify(storageSambaProperties).targetUrl();
		verify(storageSambaProperties).cifsContext();
	}

	@ParameterizedTest
	@NullAndEmptySource
	void readFile_nullBlobKey(final String blobKey) {
		assertThatThrownBy(() -> storageSambaIntegration.readFile(blobKey))
			.isInstanceOf(Problem.class)
			.hasMessageContaining("Blob key cannot be null or empty");
	}

	@Test
	void writeFile() throws SQLException, IOException {
		final var content = "TestString";
		final var expectedHash = "6dd79f2770a0bb38073b814a5ff000647b37be5abbde71ec9176c6ce0cb32a27";

		final var blob = Mockito.mock(Blob.class);
		final var inputStream = new ByteArrayInputStream(content.getBytes());
		final var outputStream = new ByteArrayOutputStream();

		when(blob.getBinaryStream()).thenReturn(inputStream);
		when(storageSambaProperties.targetUrl()).thenReturn("smb://samba-hostname/abc/invoice-cache/test");
		when(storageSambaProperties.cifsContext()).thenCallRealMethod();

		try (final var constructor = mockConstruction(SmbFile.class, (mock, context) -> {
			// Directory exists
			when(mock.exists()).thenReturn(true);
			when(mock.getOutputStream()).thenReturn(outputStream);
		})) {

			final var resultHash = storageSambaIntegration.writeFile(blob);

			assertThat(outputStream.toByteArray()).isEqualTo(inputStream.readAllBytes());
			assertThat(resultHash).isEqualTo(expectedHash);

			final var constructed = constructor.constructed();
			assertThat(constructed).hasSize(2);
			final var dirMock = constructed.get(0);
			final var fileMock = constructed.get(1);

			verify(dirMock).exists();

			// Should not attempt to create directory since it already exists
			verify(dirMock, never()).mkdirs();
			verify(fileMock).getOutputStream();
		}

		verify(storageSambaProperties).targetUrl();
		verify(storageSambaProperties, times(2)).cifsContext();
	}

	@Test
	void writeFile_nullBlob() {
		assertThatThrownBy(() -> storageSambaIntegration.writeFile(null))
			.isInstanceOf(Problem.class)
			.hasMessageContaining("Blob cannot be null");
	}

	@Test
	void writeFile_createDirectory() throws SQLException, IOException {
		final var content = "TestString";
		final var expectedHash = "6dd79f2770a0bb38073b814a5ff000647b37be5abbde71ec9176c6ce0cb32a27";

		final var blob = Mockito.mock(Blob.class);
		final var inputStream = new ByteArrayInputStream(content.getBytes());
		final var outputStream = new ByteArrayOutputStream();

		when(blob.getBinaryStream()).thenReturn(inputStream);
		when(storageSambaProperties.targetUrl()).thenReturn("smb://samba-hostname/abc/invoice-cache/test");
		when(storageSambaProperties.cifsContext()).thenCallRealMethod();

		try (final var constructor = mockConstruction(SmbFile.class, (mock, context) -> {
			// Directory does not exist
			when(mock.exists()).thenReturn(false);
			when(mock.getOutputStream()).thenReturn(outputStream);
		})) {

			final var resultHash = storageSambaIntegration.writeFile(blob);

			assertThat(outputStream.toByteArray()).isEqualTo(inputStream.readAllBytes());
			assertThat(resultHash).isEqualTo(expectedHash);

			final var constructed = constructor.constructed();
			assertThat(constructed).hasSize(2);
			final var dirMock = constructed.get(0);
			final var fileMock = constructed.get(1);

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
		final var content = "TestString";
		final var blob = Mockito.mock(Blob.class);

		final var inputStream = new ByteArrayInputStream(content.getBytes());

		when(blob.getBinaryStream()).thenReturn(inputStream);

		try (var construction = mockConstruction(SmbFile.class, (mock, context) -> when(mock.exists()).thenThrow(new SmbException("Random error")))) {

			assertThatThrownBy(() -> storageSambaIntegration.writeFile(blob))
				.isInstanceOf(BlobWriteException.class)
				.hasMessageContaining("Could not write file to Samba");
		}
		verify(storageSambaProperties).targetUrl();
		verify(storageSambaProperties).cifsContext();
	}

	@ParameterizedTest
	@MethodSource("verifyBlobIntegrityArgumentProvider")
	void verifyBlobIntegrity(final String content, final String expectedHash) {
		try (final var construction = mockConstruction(SmbFile.class, (mock, context) -> when(mock.getInputStream()).thenReturn(new ByteArrayInputStream(content.getBytes())))) {

			when(storageSambaProperties.targetUrl()).thenReturn("smb://samba-hostname/abc/invoice-cache/test");

			final var result = storageSambaIntegration.verifyBlobIntegrity(expectedHash);

			verify(storageSambaProperties).targetUrl();
			verify(storageSambaProperties).cifsContext();
			assertThat(result).isEqualTo(expectedHash);
		}
	}

	@ParameterizedTest
	@NullAndEmptySource
	void verifyBlobIntegrity_nullOrEmptyBlobKey(final String blobKey) {
		assertThatThrownBy(() -> storageSambaIntegration.verifyBlobIntegrity(blobKey))
			.isInstanceOf(Problem.class)
			.hasMessageContaining("Blob key cannot be null or empty");
	}

	@Test
	void verifyBlobIntegrity_throws() {
		final var blobKey = "6dd79f2770a0bb38073b814a5ff000647b37be5abbde71ec9176c6ce0cb32a27";

		try (final var construction = mockConstruction(SmbFile.class, (mock, context) -> when(mock.getInputStream()).thenThrow(new IOException("Read error")))) {

			assertThatThrownBy(() -> storageSambaIntegration.verifyBlobIntegrity(blobKey))
				.isInstanceOf(BlobIntegrityException.class)
				.hasMessageContaining("Could not verify blob integrity for %s".formatted(blobKey));
		}
		verify(storageSambaProperties).targetUrl();
		verify(storageSambaProperties).cifsContext();
	}

}
