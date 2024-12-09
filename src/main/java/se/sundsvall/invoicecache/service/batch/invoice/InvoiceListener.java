package se.sundsvall.invoicecache.service.batch.invoice;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.stereotype.Component;
import se.sundsvall.invoicecache.integration.db.InvoiceEntityRepository;

/**
 * Listens for events related to reading invoices from raindance and writing them to the "local" DB
 */
@Component
public class InvoiceListener implements StepExecutionListener {

	private static final Logger LOG = LoggerFactory.getLogger(InvoiceListener.class);

	private final InvoiceEntityRepository invoiceEntityRepository;

	public InvoiceListener(final InvoiceEntityRepository invoiceEntityRepository) {
		this.invoiceEntityRepository = invoiceEntityRepository;
	}

	@Override
	public void beforeStep(final @NotNull StepExecution stepExecution) {
		LOG.info("Before job execution there are {} items in the backup table", invoiceEntityRepository.count());
		LOG.info("Starting to clean local DB.");
		invoiceEntityRepository.deleteAllInBatch();
		LOG.info("Done cleaning backup table.");

	}

	@Override
	public ExitStatus afterStep(final StepExecution stepExecution) {
		final long count = invoiceEntityRepository.count();
		if (stepExecution.getExitStatus().equals(ExitStatus.COMPLETED)) {
			LOG.info("After backup job execution there are {} items in the db", count);
		} else {
			LOG.info("Something went wrong while reading invoices{}", stepExecution.getSummary());
		}
		return null;
	}
}
