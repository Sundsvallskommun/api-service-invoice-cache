package se.sundsvall.invoicecache.service.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import se.sundsvall.dept44.scheduling.Dept44Scheduled;

@Component
public class RemovalScheduler {

	private static final Logger LOG = LoggerFactory.getLogger(RemovalScheduler.class);
	private final RemovalWorker removalWorker;

	public RemovalScheduler(final RemovalWorker removalWorker) {
		this.removalWorker = removalWorker;
	}

	@Dept44Scheduled(
		cron = "${scheduler.removal.cron}",
		name = "${scheduler.removal.name}",
		lockAtMostFor = "${scheduler.removal.shedlock-lock-at-most-for}",
		maximumExecutionTime = "${scheduler.removal.maximum-execution-time}")
	void removeOldRaindanceInvoices() {
		LOG.info("Starting scheduled job to remove old Raindance invoices");
		removalWorker.removeOldRaindanceInvoices();
		LOG.info("Finished scheduled job to remove old Raindance invoices");
	}

}
