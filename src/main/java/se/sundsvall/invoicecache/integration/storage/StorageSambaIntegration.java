package se.sundsvall.invoicecache.integration.storage;

import java.io.IOException;
import java.sql.Blob;
import jcifs.smb.SmbFile;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.zalando.problem.Problem;
import se.sundsvall.invoicecache.integration.storage.util.HashUtil;
import se.sundsvall.invoicecache.util.exception.BlobIntegrityException;
import se.sundsvall.invoicecache.util.exception.BlobWriteException;

import static org.zalando.problem.Status.INTERNAL_SERVER_ERROR;

@Component
public class StorageSambaIntegration {

	private static final Logger LOGGER = LoggerFactory.getLogger(StorageSambaIntegration.class);
	private static final Integer DIRECTORY_PREFIX_LENGTH = 2;
	private static final Integer DIRECTORY_BEGIN_INDEX = 0;

	private final StorageSambaProperties properties;

	public StorageSambaIntegration(final StorageSambaProperties storageSambaProperties) {
		this.properties = storageSambaProperties;
	}

	/**
	 * Reads file from Samba storage and SHA256 hashes the content.
	 *
	 * @param  blobKey Hash value used as the file name in the Samba storage.
	 * @return         SHA256 hash of the file content as a 64-character hexadecimal string. If everything works as
	 *                 expected, it should always be equal to the provided blobKey.
	 */
	public String verifyBlobIntegrity(final String blobKey) {
		LOGGER.info("Verifying blob integrity for blobKey '{}'", blobKey);
		if (blobKey == null || blobKey.isEmpty()) {
			throw Problem.valueOf(INTERNAL_SERVER_ERROR, "Blob key cannot be null or empty");
		}

		var directory = extractDirectory(blobKey);
		// Takes the targetUrl and appends the directory and blobKey to form the full file path.
		var filePath = properties.targetUrl() + "/" + directory + "/" + blobKey + ".pdf";

		try (final var file = new SmbFile(filePath, properties.cifsContext())) {
			return HashUtil.SHA256(file.getInputStream());
		} catch (IOException e) {
			LOGGER.error("Failed to verify blob integrity for key '{}', path '{}'", blobKey, filePath);
			throw new BlobIntegrityException("Could not verify blob integrity for " + blobKey, e);
		}
	}

	/**
	 * Writes file to Samba storage and returns a SHA256 hash of the file content
	 *
	 * @param  blob the file content to write
	 * @return      the SHA256 hash of the file content
	 */
	public String writeFile(final Blob blob) {
		LOGGER.info("Writing file to Samba storage");
		if (blob == null) {
			throw Problem.valueOf(INTERNAL_SERVER_ERROR, "Blob cannot be null");
		}

		try {
			// Creates a SHA256 hash of the file content.
			var blobKey = HashUtil.SHA256(blob.getBinaryStream());
			var directory = extractDirectory(blobKey);

			var directoryPath = properties.targetUrl() + "/" + directory;
			var filePath = directoryPath + "/" + blobKey + ".pdf";

			// Ensure the directory exists, if not, creates it.
			try (final var sambaDirectory = new SmbFile(directoryPath, properties.cifsContext())) {
				if (!sambaDirectory.exists()) {
					sambaDirectory.mkdirs();
				}
			}

			// Writes the file to Samba storage.
			try (final var file = new SmbFile(filePath, properties.cifsContext());
				var outputStream = file.getOutputStream();
				var inputStream = blob.getBinaryStream()) {
				IOUtils.copy(inputStream, outputStream);
			}

			return blobKey;
		} catch (Exception e) {
			LOGGER.error("Failed to write PDF file to Samba storage");
			throw new BlobWriteException("Could not write file to Samba", e);
		}
	}

	/**
	 * Reads file from Samba storage as an InputStream
	 *
	 * @param  blobKey the blob key used to identify the file
	 * @return         InputStream of the file content
	 */
	public byte[] readFile(final String blobKey) {
		LOGGER.info("Reading file with blobKey '{}'", blobKey);
		if (blobKey == null || blobKey.isEmpty()) {
			throw Problem.valueOf(INTERNAL_SERVER_ERROR, "Blob key cannot be null or empty");
		}

		var directory = extractDirectory(blobKey);
		// Takes the targetUrl and appends the directory and blobKey to form the full file path.
		var filePath = properties.targetUrl() + "/" + directory + "/" + blobKey + ".pdf";

		try (var smbFile = new SmbFile(filePath, properties.cifsContext());
			var inputStream = smbFile.getInputStream()) {
			return inputStream.readAllBytes();
		} catch (Exception e) {
			LOGGER.error("Failed to read blob for key '{}', path '{}'", blobKey, filePath);
			throw new BlobIntegrityException("Could not read blob for " + blobKey, e);
		}

	}

	/**
	 * Extracts the directory from the blob key.
	 *
	 * @param  blobKey the blob key
	 * @return         the directory extracted from the blob key
	 */
	private String extractDirectory(final String blobKey) {
		// The first two characters of the blobKey are used as a directory name.
		return blobKey.substring(DIRECTORY_BEGIN_INDEX, DIRECTORY_PREFIX_LENGTH);
	}

}
