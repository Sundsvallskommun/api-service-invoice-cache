package se.sundsvall.invoicecache.integration.storage.scheduler;

import java.time.OffsetDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import se.sundsvall.invoicecache.integration.db.PdfRepository;
import se.sundsvall.invoicecache.integration.db.entity.PdfEntity;
import se.sundsvall.invoicecache.integration.storage.StorageSambaIntegration;

@Component
public class StorageSchedulerWorker {

	private static final Logger LOG = LoggerFactory.getLogger(StorageSchedulerWorker.class);
	private final StorageSambaIntegration storageSambaIntegration;
	private final PdfRepository pdfRepository;

	// Sundsvalls Kommun invoices should not be transferred to Samba storage
	private static final String EXCLUDED_ISSUER_LEGAL_ID = "2120002411";

	@Value("${integration.storage.samba.scheduler.jobs.transfer.threshold-months:6}")
	private Integer transferThresholdMonths = 6;

	@Value("${integration.storage.samba.scheduler.jobs.transfer.limit:1}")
	private Integer transferLimit = 1;

	@Value("${integration.storage.samba.scheduler.jobs.truncate.limit:1}")
	private Integer truncateLimit = 1;

	public StorageSchedulerWorker(
		final StorageSambaIntegration storageSambaIntegration,
		final PdfRepository pdfRepository) {
		this.storageSambaIntegration = storageSambaIntegration;
		this.pdfRepository = pdfRepository;

	}

	/**
	 * Finds files eligible for transfer and writes to the Samba storage.
	 */
	@Transactional
	public void transferFiles() {
		var files = findPdfToTransfer();
		if (files.isEmpty()) {
			LOG.info("Found no files eligible for transfer.");
			return;
		}
		for (var file : files) {
			LOG.info("Transferring file with id='{}'", file.getId());

			// Writes the file to Samba storage and returns a SHA256 hash of the file content
			var fileHash = storageSambaIntegration.writeFile(file.getDocument());
			var movedAt = OffsetDateTime.now();
			file.setFileHash(fileHash);
			file.setMovedAt(movedAt);
			pdfRepository.save(file);
			LOG.info("File transfer completed successfully. ID='{}', movedAt='{}', hash='{}'.", file.getId(), movedAt, fileHash);
		}
	}

	/**
	 * Finds files eligible for truncation and removes the blob from the database after verifying its integrity.
	 */
	@Transactional
	public void truncateFiles() {
		var files = findPdfToTruncate();
		if (files.isEmpty()) {
			LOG.info("Found no files eligible for truncation.");
			return;
		}

		for (var file : files) {
			LOG.info("Truncating file with id='{}'", file.getId());
			var fileHash = file.getFileHash();

			// verifyBlobIntegrity finds a file using the given file hash and returns a calculated hash of the file content
			var calculatedHash = storageSambaIntegration.verifyBlobIntegrity(fileHash);

			// If the calculated hash matches the stored file hash, we can safely truncate the blob
			if (calculatedHash.equals(file.getFileHash())) {
				file.setDocument(null);
				file.setTruncatedAt(OffsetDateTime.now());
				pdfRepository.save(file);
				LOG.info("File truncation completed successfully. ID='{}', truncatedAt='{}'.", file.getId(), file.getTruncatedAt());
			} else {
				LOG.error("Blob integrity verification failed for file with id='{}'. Stored hash='{}', calculated hash='{}'. Blob was not be truncated.", file.getId(), fileHash, calculatedHash);
			}
		}

	}

	/**
	 * Finds a PDF that is older than 'transferThresholdMonths' months and has not been moved yet.
	 *
	 * @return the PDF entity
	 */
	private List<PdfEntity> findPdfToTransfer() {
		return pdfRepository.findPdfsToTransfer(OffsetDateTime.now().minusMonths(transferThresholdMonths), EXCLUDED_ISSUER_LEGAL_ID, transferLimit);
	}

	/**
	 * Finds a PDF that has been moved but not yet truncated.
	 *
	 * @return the PDF entity
	 */
	private List<PdfEntity> findPdfToTruncate() {
		return pdfRepository.findPdfsToTruncate(truncateLimit);
	}

}
