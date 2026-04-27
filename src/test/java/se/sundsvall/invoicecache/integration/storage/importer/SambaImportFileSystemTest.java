package se.sundsvall.invoicecache.integration.storage.importer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.invoicecache.integration.storage.StorageSambaProperties;
import se.sundsvall.invoicecache.util.exception.BlobIntegrityException;
import se.sundsvall.invoicecache.util.exception.BlobWriteException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SambaImportFileSystemTest {

	@Mock
	private StorageSambaProperties storageSambaProperties;

	@Mock
	private SambaImportProperties sambaImportProperties;

	@InjectMocks
	private SambaImportFileSystem fileSystem;

	@AfterEach
	void tearDown() {
		verifyNoMoreInteractions(storageSambaProperties, sambaImportProperties);
	}

	private void wireSourceUrl() {
		when(storageSambaProperties.host()).thenReturn("samba-host");
		when(storageSambaProperties.port()).thenReturn(445);
		when(storageSambaProperties.share()).thenReturn("share");
		when(sambaImportProperties.sourceDirectory()).thenReturn("import");
	}

	@Test
	void listZipFiles_filtersOnlyZips() throws IOException {
		wireSourceUrl();

		final var zip1 = mockSmbFileEntry("Apr.zip");
		final var zip2 = mockSmbFileEntry("123.ZIP");
		final var notZip = mockSmbFileEntry("readme.txt");

		try (final var construction = mockConstruction(SmbFile.class, (mock, context) -> when(mock.listFiles()).thenReturn(new SmbFile[] {
			zip1, zip2, notZip
		}))) {
			final var result = fileSystem.listZipFiles();

			assertThat(result).containsExactly("123.ZIP", "Apr.zip");
			assertThat(construction.constructed()).hasSize(1);
			verify(construction.constructed().getFirst()).listFiles();
		}

		verify(storageSambaProperties).cifsContext();
	}

	@Test
	void listZipFiles_throwsOnIoError() {
		wireSourceUrl();

		try (final var _ = mockConstruction(SmbFile.class, (mock, context) -> when(mock.listFiles()).thenThrow(new SmbException("boom")))) {
			assertThatThrownBy(() -> fileSystem.listZipFiles())
				.isInstanceOf(BlobIntegrityException.class)
				.hasMessageContaining("Could not list zip files");
		}

		verify(storageSambaProperties).cifsContext();
	}

	@Test
	void openInputStream_streamsBytes() throws IOException {
		wireSourceUrl();

		final var content = "TestZipBytes".getBytes();

		try (final var construction = mockConstruction(SmbFile.class, (mock, context) -> when(mock.getInputStream()).thenReturn(new ByteArrayInputStream(content)))) {
			try (final var stream = fileSystem.openInputStream("foo.zip")) {
				assertThat(stream.readAllBytes()).isEqualTo(content);
			}
			assertThat(construction.constructed()).hasSize(1);
			final var smbFile = construction.constructed().getFirst();
			verify(smbFile).getInputStream();
			verify(smbFile).close();
		}

		verify(storageSambaProperties).cifsContext();
	}

	@Test
	void openInputStream_throwsOnIoError() {
		wireSourceUrl();

		try (final var _ = mockConstruction(SmbFile.class, (mock, context) -> when(mock.getInputStream()).thenThrow(new SmbException("nope")))) {
			assertThatThrownBy(() -> fileSystem.openInputStream("foo.zip"))
				.isInstanceOf(BlobIntegrityException.class)
				.hasMessageContaining("Could not open input stream for foo.zip");
		}

		verify(storageSambaProperties).cifsContext();
	}

	@Test
	void delete_invokesSmbDelete() {
		wireSourceUrl();

		try (final var construction = mockConstruction(SmbFile.class)) {
			fileSystem.delete("foo.zip");

			assertThat(construction.constructed()).hasSize(1);
			final var smbFile = construction.constructed().getFirst();
			try {
				verify(smbFile).delete();
			} catch (final IOException e) {
				throw new AssertionError(e);
			}
		}

		verify(storageSambaProperties).cifsContext();
	}

	@Test
	void delete_throwsOnIoError() {
		wireSourceUrl();

		try (final var _ = mockConstruction(SmbFile.class, (mock, context) -> doThrow(new SmbException("fail")).when(mock).delete())) {
			assertThatThrownBy(() -> fileSystem.delete("foo.zip"))
				.isInstanceOf(BlobWriteException.class)
				.hasMessageContaining("Could not delete foo.zip");
		}

		verify(storageSambaProperties).cifsContext();
	}

	private static SmbFile mockSmbFileEntry(final String name) {
		final var file = mock(SmbFile.class);
		when(file.getName()).thenReturn(name);
		try {
			when(file.isFile()).thenReturn(true);
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
		return file;
	}
}
