package se.sundsvall.invoicecache.service.batch.backup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.stereotype.Component;
import se.sundsvall.invoicecache.integration.db.BackupInvoiceRepository;

/**
 * Listens for events related to performing backup tasks/batches
 */
@Component
public class BackupListener implements StepExecutionListener {
    
    private static final Logger LOG = LoggerFactory.getLogger(BackupListener.class);
    
    private final BackupInvoiceRepository repository;
    
    public BackupListener(final BackupInvoiceRepository repository) {
        this.repository = repository;
    }
    
    @Override
    public void beforeStep(final StepExecution stepExecution) {
        LOG.info("Starting to remove old backups from backup table");
        repository.deleteAllInBatch();
        LOG.info("Deleted old backups");
    }
    
    @Override
    public ExitStatus afterStep(final StepExecution stepExecution) {
        if(stepExecution.getExitStatus().equals(ExitStatus.COMPLETED)) {
            LOG.info("Done writing invoices to backup table");
        } else {
            LOG.warn("Something went wrong while performing backup of invoices: {}", stepExecution.getSummary());
        }
        return null;
    }
}
