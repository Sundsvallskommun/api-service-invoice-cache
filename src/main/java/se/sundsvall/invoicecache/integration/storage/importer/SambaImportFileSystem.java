package se.sundsvall.invoicecache.integration.storage.importer;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import jcifs.smb.SmbFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import se.sundsvall.dept44.util.LogUtils;
import se.sundsvall.invoicecache.integration.storage.StorageSambaProperties;
import se.sundsvall.invoicecache.util.exception.BlobIntegrityException;
import se.sundsvall.invoicecache.util.exception.BlobWriteException;

@Component
public class SambaImportFileSystem {

	private static final Logger LOG = LoggerFactory.getLogger(SambaImportFileSystem.class);

	private final StorageSambaProperties storageProperties;
	private final SambaImportProperties importProperties;

	public SambaImportFileSystem(
		final StorageSambaProperties storageProperties,
		final SambaImportProperties importProperties) {
		this.storageProperties = storageProperties;
		this.importProperties = importProperties;
	}

	private static boolean isFile(final SmbFile file) {
		try {
			return file.isFile();
		} catch (final IOException e) {
			final var cleanedName = LogUtils.sanitizeForLogging(file.getName());
			LOG.warn("Could not determine if '{}' is a file, skipping", cleanedName, e);
			return false;
		}
	}

	String sourceUrl() {
		return "smb://%s:%d/%s/%s".formatted(
			storageProperties.host(),
			storageProperties.port(),
			storageProperties.share(),
			importProperties.sourceDirectory());
	}

	public List<String> listZipFiles() {
		final var sourceUrl = sourceUrl();
		LOG.info("Listing zip files at '{}'", sourceUrl);
		try (final var dir = new SmbFile(sourceUrl + "/", storageProperties.cifsContext())) {
			final SmbFile[] files = dir.listFiles();
			return Arrays.stream(files)
				.filter(SambaImportFileSystem::isFile)
				.map(SmbFile::getName)
				.filter(name -> name.toLowerCase(Locale.ROOT).endsWith(".zip"))
				.sorted()
				.toList();
		} catch (final IOException e) {
			LOG.error("Failed to list zip files at '{}'", sourceUrl, e);
			throw new BlobIntegrityException("Could not list zip files at " + sourceUrl, e);
		}
	}

	public InputStream openInputStream(final String name) {
		final var cleanedName = LogUtils.sanitizeForLogging(name);
		LOG.info("Opening input stream for '{}'", cleanedName);
		final var sourceUrl = sourceUrl();
		SmbFile smbFile = null;
		try {
			smbFile = new SmbFile(sourceUrl + "/" + name, storageProperties.cifsContext());
			final var inputStream = smbFile.getInputStream();
			// Hand ownership to ClosingInputStream — caller closes when done with the stream.
			final var owned = smbFile;
			smbFile = null;
			return new ClosingInputStream(inputStream, owned);
		} catch (final IOException e) {
			LOG.error("Failed to open input stream for '{}'", cleanedName, e);
			throw new BlobIntegrityException("Could not open input stream for " + name, e);
		} finally {
			closeQuietly(smbFile);
		}
	}

	private static void closeQuietly(final SmbFile smbFile) {
		if (smbFile == null) {
			return;
		}
		try {
			smbFile.close();
		} catch (final RuntimeException e) {
			LOG.warn("Failed to close SmbFile", e);
		}
	}

	public void delete(final String name) {
		final var cleanedName = LogUtils.sanitizeForLogging(name);
		LOG.info("Deleting '{}'", cleanedName);
		final var sourceUrl = sourceUrl();
		try (final var smbFile = new SmbFile(sourceUrl + "/" + name, storageProperties.cifsContext())) {
			smbFile.delete();
		} catch (final IOException e) {
			LOG.error("Failed to delete '{}'", cleanedName, e);
			throw new BlobWriteException("Could not delete " + name, e);
		}
	}

	private static final class ClosingInputStream extends InputStream {
		private final InputStream delegate;
		private final SmbFile smbFile;

		private ClosingInputStream(final InputStream delegate, final SmbFile smbFile) {
			this.delegate = delegate;
			this.smbFile = smbFile;
		}

		@Override
		public int read() throws IOException {
			return delegate.read();
		}

		@Override
		public int read(final byte[] b, final int off, final int len) throws IOException {
			return delegate.read(b, off, len);
		}

		@Override
		public void close() throws IOException {
			try {
				delegate.close();
			} finally {
				smbFile.close();
			}
		}
	}
}
