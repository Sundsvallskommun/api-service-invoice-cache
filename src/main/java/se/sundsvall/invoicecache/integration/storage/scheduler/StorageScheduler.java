package se.sundsvall.invoicecache.integration.storage.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import se.sundsvall.dept44.scheduling.Dept44Scheduled;

@Component
public class StorageScheduler {

	private final StorageSchedulerWorker storageSchedulerWorker;
	private static final Logger LOG = LoggerFactory.getLogger(StorageScheduler.class);

	public StorageScheduler(final StorageSchedulerWorker storageSchedulerWorker) {
		this.storageSchedulerWorker = storageSchedulerWorker;
	}

	@Dept44Scheduled(
		cron = "${integration.storage.samba.scheduler.jobs.transfer.cron}",
		name = "${integration.storage.samba.scheduler.jobs.transfer.name}",
		lockAtMostFor = "${integration.storage.samba.scheduler.jobs.transfer.shedlock-lock-at-most-for}",
		maximumExecutionTime = "${integration.storage.samba.scheduler.jobs.transfer.maximum-execution-time}")
	void transferFile() {
		LOG.info("Starting scheduled job to transfer a file to Samba storage");
		storageSchedulerWorker.transferFile();
		LOG.info("Finished scheduled job to transfer a file to Samba storage");
	}

	@Dept44Scheduled(
		cron = "${integration.storage.samba.scheduler.jobs.truncate.cron}",
		name = "${integration.storage.samba.scheduler.jobs.truncate.name}",
		lockAtMostFor = "${integration.storage.samba.scheduler.jobs.truncate.shedlock-lock-at-most-for}",
		maximumExecutionTime = "${integration.storage.samba.scheduler.jobs.truncate.maximum-execution-time}")
	void truncateBlob() {
		LOG.info("Starting scheduled job to truncate a file to Samba storage");
		storageSchedulerWorker.truncateFile();
		LOG.info("Finished scheduled job to truncate a file to Samba storage");
	}

}
