package se.sundsvall.invoicecache.integration.storage.scheduler;

import static se.sundsvall.invoicecache.Constant.RAINDANCE_ISSUER_LEGAL_ID;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import se.sundsvall.invoicecache.integration.db.PdfRepository;
import se.sundsvall.invoicecache.integration.storage.StorageSambaIntegration;

@Component
public class StorageSchedulerWorker {

	private static final Logger LOG = LoggerFactory.getLogger(StorageSchedulerWorker.class);
	private final StorageSambaIntegration storageSambaIntegration;
	private final PdfRepository pdfRepository;

	@Value("${integration.storage.samba.scheduler.jobs.transfer.threshold-months:6}")
	private Integer transferThresholdMonths = 6;

	public StorageSchedulerWorker(
		final StorageSambaIntegration storageSambaIntegration,
		final PdfRepository pdfRepository) {
		this.storageSambaIntegration = storageSambaIntegration;
		this.pdfRepository = pdfRepository;

	}

	/**
	 * Finds files eligible for transfer and writes to the Samba storage. Processes all invoices older than the threshold
	 * without limit.
	 */
	public void transferFiles(final Consumer<String> transferFileUnhealthyConsumer) {
		final var fileIds = findPdfIdsToTransfer();
		if (fileIds.isEmpty()) {
			LOG.info("Found no files eligible for transfer.");
			return;
		}
		LOG.info("Found {} files eligible for transfer.", fileIds.size());

		fileIds.forEach(fileId -> {
			try {
				final var fileOptional = pdfRepository.findById(fileId);
				if (fileOptional.isEmpty()) {
					LOG.warn("File with id='{}' not found. Skipping.", fileId);
					return;
				}

				final var file = fileOptional.get();
				LOG.info("Transferring file with id='{}'", file.getId());

				// Writes the file to Samba storage and returns a SHA256 hash of the file content
				final var fileHash = storageSambaIntegration.writeFile(file.getDocument());
				final var movedAt = OffsetDateTime.now();
				file.setFileHash(fileHash);
				file.setMovedAt(movedAt);
				pdfRepository.save(file);
				LOG.info("File transfer completed successfully. ID='{}', movedAt='{}', hash='{}'.", file.getId(), movedAt, fileHash);
			} catch (final Exception e) {
				transferFileUnhealthyConsumer.accept("Failed to transfer file to Samba storage.");
				LOG.error("Failed to transfer file with id='{}'. Error: {}", fileId, e.getMessage(), e);
			}
		});
	}

	/**
	 * Finds files eligible for truncation.
	 *
	 * @return list of file IDs eligible for truncation
	 */
	@Transactional
	public List<Integer> getFileIdsToTruncate() {
		final var fileIds = findPdfIdsToTruncate();
		if (fileIds.isEmpty()) {
			LOG.info("Found no files eligible for truncation.");
			return List.of();
		}

		return fileIds;
	}

	/**
	 * Truncates files eligible for truncation by removing their blob data from the database after verifying integrity.
	 *
	 */
	@Transactional
	public void truncateFile(final Integer fileId, final Consumer<String> unhealthyConsumer) {

		final var fileOptional = pdfRepository.findById(fileId);
		if (fileOptional.isEmpty()) {
			LOG.warn("File with id='{}' not found, skipping truncation.", fileId);
			return;
		}

		final var file = fileOptional.get();
		LOG.info("Truncating file with id='{}'", file.getId());
		final var fileHash = file.getFileHash();

		// verifyBlobIntegrity finds a file using the given file hash and returns a calculated hash of the file content
		// If the calculated hash matches the stored file hash, we can safely truncate the blob
		final var calculatedHash = storageSambaIntegration.verifyBlobIntegrity(fileHash);
		if (calculatedHash.equals(file.getFileHash())) {
			file.setDocument(null);
			file.setTruncatedAt(OffsetDateTime.now());
			pdfRepository.save(file);
			LOG.info("File truncation completed successfully. ID='{}', truncatedAt='{}'.", file.getId(), file.getTruncatedAt());
		} else {
			unhealthyConsumer.accept("Blob integrity verification failed for file. Blob was not be truncated.");
			LOG.error("Blob integrity verification failed for file with id='{}'. Stored hash='{}', calculated hash='{}'. Blob was not be truncated.", file.getId(), fileHash, calculatedHash);
		}

	}

	/**
	 * Finds IDs of PDFs that are older than 'transferThresholdMonths' months and have not been moved yet.
	 *
	 * @return list of PDF entity IDs
	 */
	private List<Integer> findPdfIdsToTransfer() {
		return pdfRepository.findPdfIdsToTransfer(OffsetDateTime.now().minusMonths(transferThresholdMonths), RAINDANCE_ISSUER_LEGAL_ID);
	}

	/**
	 * Finds IDs of PDFs that have been moved but not yet truncated.
	 *
	 * @return list of PDF entity IDs
	 */
	private List<Integer> findPdfIdsToTruncate() {
		return pdfRepository.findPdfIdsToTruncate();
	}

}
