package se.sundsvall.invoicecache.service.batch.backup;

import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import se.sundsvall.invoicecache.integration.db.BackupInvoiceRepository;
import se.sundsvall.invoicecache.integration.db.InvoiceRepository;

/**
 * Listens for events related to restoring backups in case fetching of invoices fails.
 */
@Component
public class RestoreBackupListener implements StepExecutionListener {

	private static final Logger LOG = LoggerFactory.getLogger(RestoreBackupListener.class);

	private final InvoiceRepository invoiceRepository;
	private final BackupInvoiceRepository backupRepository;
	private final RestoreBackupJobHealthIndicator healthIndicator;

	RestoreBackupListener(final InvoiceRepository invoiceRepository, final BackupInvoiceRepository backupRepository,
		final RestoreBackupJobHealthIndicator healthIndicator) {
		this.invoiceRepository = invoiceRepository;
		this.backupRepository = backupRepository;
		this.healthIndicator = healthIndicator;
	}

	@Override
	@Transactional
	public void beforeStep(final @NotNull StepExecution stepExecution) {
		LOG.info("Starting to restore backup, truncating invoices table");
		// Clean the invoiceRespository before restoring the backup
		invoiceRepository.truncateTable();
		LOG.info("Done truncating invoices table, starting to restore {} invoices from backup", backupRepository.count());
	}

	@Override
	public ExitStatus afterStep(final StepExecution stepExecution) {
		if (stepExecution.getExitStatus().equals(ExitStatus.COMPLETED)) {
			LOG.info("Successfully restored {} invoices from backup.", invoiceRepository.count());
			healthIndicator.setHealthy();
		} else {
			LOG.info("Something went wrong while restoring backups{}", stepExecution.getSummary());
			healthIndicator.setUnhealthy();
		}
		return null;
	}
}
