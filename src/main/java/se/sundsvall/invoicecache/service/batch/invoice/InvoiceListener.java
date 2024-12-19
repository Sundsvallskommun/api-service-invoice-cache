package se.sundsvall.invoicecache.service.batch.invoice;

import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.stereotype.Component;
import se.sundsvall.invoicecache.integration.db.InvoiceRepository;

/**
 * Listens for events related to reading invoices from raindance and writing them to the "local" DB
 */
@Component
public class InvoiceListener implements StepExecutionListener {

	private static final Logger LOG = LoggerFactory.getLogger(InvoiceListener.class);

	private final InvoiceRepository invoiceRepository;
	private final FetchInvoicesJobHealthIndicator healthIndicator;

	InvoiceListener(final InvoiceRepository invoiceRepository,
		final FetchInvoicesJobHealthIndicator healthIndicator) {
		this.invoiceRepository = invoiceRepository;
		this.healthIndicator = healthIndicator;
	}

	@Override
	public void beforeStep(final @NotNull StepExecution stepExecution) {
		LOG.info("Before job execution there are {} items in the backup table", invoiceRepository.count());
		LOG.info("Starting to truncate invoice table.");
		invoiceRepository.truncateTable();
		LOG.info("Done truncating invoice table.");

	}

	@Override
	public ExitStatus afterStep(final StepExecution stepExecution) {
		final long count = invoiceRepository.count();
		if (stepExecution.getExitStatus().equals(ExitStatus.COMPLETED)) {
			LOG.info("After backup job execution there are {} items in the db", count);
			healthIndicator.setHealthy();
		} else {
			LOG.info("Something went wrong while reading invoices{}", stepExecution.getSummary());
			healthIndicator.setUnhealthy();
		}
		return null;
	}
}
