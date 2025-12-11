package se.sundsvall.invoicecache.integration.storage.scheduler;

import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import se.sundsvall.dept44.scheduling.Dept44Scheduled;
import se.sundsvall.dept44.scheduling.health.Dept44HealthUtility;

@Component
public class StorageScheduler {

	private static final Logger LOG = LoggerFactory.getLogger(StorageScheduler.class);
	private final StorageSchedulerWorker storageSchedulerWorker;
	private final Consumer<String> truncateFileUnhealhtyConsumer;
	private final Consumer<String> transferFileUnhealthyConsumer;
	@Value("${integration.storage.samba.scheduler.jobs.transfer.name}")
	private String transferFileJobName;
	@Value("${integration.storage.samba.scheduler.jobs.truncate.name}")
	private String truncateFileJobName;

	public StorageScheduler(final StorageSchedulerWorker storageSchedulerWorker, final Dept44HealthUtility healthUtility) {
		this.storageSchedulerWorker = storageSchedulerWorker;
		this.transferFileUnhealthyConsumer = msg -> healthUtility.setHealthIndicatorUnhealthy(transferFileJobName,
			String.format("transfer error: %s", msg));
		this.truncateFileUnhealhtyConsumer = msg -> healthUtility.setHealthIndicatorUnhealthy(truncateFileJobName,
			String.format("Truncate error: %s", msg));
	}

	@Dept44Scheduled(
		cron = "${integration.storage.samba.scheduler.jobs.transfer.cron}",
		name = "${integration.storage.samba.scheduler.jobs.transfer.name}",
		lockAtMostFor = "${integration.storage.samba.scheduler.jobs.transfer.shedlock-lock-at-most-for}",
		maximumExecutionTime = "${integration.storage.samba.scheduler.jobs.transfer.maximum-execution-time}")
	void transferFiles() {
		LOG.info("Starting scheduled job to transfer a file to Samba storage");
		storageSchedulerWorker.transferFiles(transferFileUnhealthyConsumer);
		LOG.info("Finished scheduled job to transfer a file to Samba storage");
	}

	@Dept44Scheduled(
		cron = "${integration.storage.samba.scheduler.jobs.truncate.cron}",
		name = "${integration.storage.samba.scheduler.jobs.truncate.name}",
		lockAtMostFor = "${integration.storage.samba.scheduler.jobs.truncate.shedlock-lock-at-most-for}",
		maximumExecutionTime = "${integration.storage.samba.scheduler.jobs.truncate.maximum-execution-time}")
	void truncateFiles() {
		LOG.info("Starting scheduled job to truncate a file to Samba storage");
		storageSchedulerWorker.getFileIdsToTruncate().forEach(fileId -> storageSchedulerWorker.truncateFile(fileId, truncateFileUnhealhtyConsumer));
		LOG.info("Finished scheduled job to truncate a file to Samba storage");
	}

}
