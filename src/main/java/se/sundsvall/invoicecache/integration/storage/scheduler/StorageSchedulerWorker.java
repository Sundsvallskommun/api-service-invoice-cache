package se.sundsvall.invoicecache.integration.storage.scheduler;

import static org.zalando.problem.Status.NOT_FOUND;

import java.time.OffsetDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.zalando.problem.Problem;
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

	@Value("${integration.storage.samba.scheduler.jobs.transfer.threshold:6}")
	private Integer transferThresholdMonths = 6;

	public StorageSchedulerWorker(
		final StorageSambaIntegration storageSambaIntegration,
		final PdfRepository pdfRepository) {
		this.storageSambaIntegration = storageSambaIntegration;
		this.pdfRepository = pdfRepository;

	}

	/**
	 * Finds a file eligible for transfer and writes it to the Samba storage.
	 */
	@Transactional
	public void transferFile() {
		var file = findPdfToTransfer();
		LOG.info("Transferring file with id='{}'", file.getId());

		// Writes the file to Samba storage and returns a SHA256 hash of the file content
		var fileHash = storageSambaIntegration.writeFile(file.getDocument());
		var movedAt = OffsetDateTime.now();
		file.setFileHash(fileHash);
		file.setMovedAt(movedAt);
		pdfRepository.save(file);
		LOG.info("File transfer completed successfully. ID='{}', movedAt='{}', hash='{}'.", file.getId(), movedAt, fileHash);
	}

	/**
	 * Find a file eligible for truncation and removes the blob from the database after verifying its integrity.
	 */
	@Transactional
	public void truncateFile() {
		var file = findPdfToTruncate();
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

	/**
	 * Finds a PDF that is older than 'transferThresholdMonths' months and has not been moved yet.
	 *
	 * @return the PDF entity
	 */
	private PdfEntity findPdfToTransfer() {
		return pdfRepository.findPdfToTransfer(OffsetDateTime.now().minusMonths(transferThresholdMonths), EXCLUDED_ISSUER_LEGAL_ID)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, "Found no PDF that is older than 6 months and have not been moved yet"));
	}

	/**
	 * Finds a PDF that has been moved but not yet truncated.
	 *
	 * @return the PDF entity
	 */
	private PdfEntity findPdfToTruncate() {
		return pdfRepository.findPdfToTruncate()
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, "Found no PDF that have been moved and not yet truncated"));
	}

}
