package se.sundsvall.invoicecache.service.batch;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import se.sundsvall.invoicecache.api.batchactuator.JobStatus;
import se.sundsvall.invoicecache.integration.db.InvoiceRepository;

import static se.sundsvall.invoicecache.service.batch.invoice.BatchConfig.RAINDANCE_JOB_NAME;

/**
 * Helper class for everything related to fetching statuses for batches and determining whether to run or not.
 */
@Component
public class JobHelper {

	private static final Logger LOG = LoggerFactory.getLogger(JobHelper.class);
	private static final int MAX_JOB_INSTANCES = 50;  // Upper bound on how many recent job instances we inspect

	private final Duration successfulWithin;  // Check for successful jobs within this Duration

	private final JobRepository jobRepository;
	private final InvoiceRepository invoiceRepository;

	public JobHelper(final JobRepository jobRepository, final InvoiceRepository invoiceRepository,
		@Value("${raindance.invoice.outdated}") final Duration timeToWait) {
		this.jobRepository = jobRepository;
		this.invoiceRepository = invoiceRepository;
		this.successfulWithin = timeToWait;
	}

	public boolean invoiceTableHasInvoices() {
		return invoiceRepository.count() > 0;
	}

	/**
	 * Check if there is a successful job within the configured {@code raindance.invoice.outdated} period, if not, we should
	 * fetch invoices.
	 *
	 * @return true if we should update, false if not.
	 */
	public boolean areInvoicesOutdated() {
		final Optional<JobExecution> recentSuccess = getSuccessfulJobWithinTimePeriod(RAINDANCE_JOB_NAME);

		if (recentSuccess.isEmpty()) {
			LOG.info("No successful job found within the configured period of {}.", successfulWithin);
			return true;
		}
		if (invoiceRepository.count() == 0) {
			LOG.info("No invoices found in local DB, get them.");
			return true;
		}
		LOG.debug("Found a completed successful job from: {}, not running a new one.", recentSuccess.get().getEndTime());
		return false;
	}

	/**
	 * Look for a successful execution that ended within the configured {@code successfulWithin} period, inspecting at most
	 * the {@value #MAX_JOB_INSTANCES} most recent job instances.
	 *
	 * @param jobName name of the job to check
	 */
	Optional<JobExecution> getSuccessfulJobWithinTimePeriod(final String jobName) {
		try {
			// Probe existence (throws NoSuchJobException if the job was never run) and cap how many instances we load.
			final int instanceCount = (int) Math.min(jobRepository.getJobInstanceCount(jobName), MAX_JOB_INSTANCES);
			final LocalDateTime threshold = LocalDateTime.now().minus(successfulWithin);

			return jobRepository.getJobInstances(jobName, 0, instanceCount).stream()
				.map(jobRepository::getJobExecutions)
				.flatMap(List<JobExecution>::stream)
				.filter(jobExecution -> jobExecution.getExitStatus().equals(ExitStatus.COMPLETED))
				.filter(jobExecution -> Objects.requireNonNull(jobExecution.getEndTime()).isAfter(threshold))
				.findFirst();

		} catch (final NoSuchJobException _) {
			// If we can't find any job, we don't care, run a new one.
			LOG.info("Couldn't find any job with name: {}", jobName);
			return Optional.empty();
		}
	}

	/**
	 * Mainly used for actuator
	 *
	 * @return list of the 50 latest jobs
	 */
	public List<JobStatus> getJobs() {
		try {
			// Instances are returned most-recent-first; cap the number we inspect.
			final int instanceCount = (int) Math.min(jobRepository.getJobInstanceCount(RAINDANCE_JOB_NAME), MAX_JOB_INSTANCES);

			return jobRepository.getJobInstances(RAINDANCE_JOB_NAME, 0, instanceCount)
				.stream()
				.map(jobRepository::getJobExecutions)
				.flatMap(List<JobExecution>::stream)
				.map(this::mapJobExecutionToJobStatus)
				.toList();

		} catch (final NoSuchJobException _) {
			// If we can't find any job, we don't care, run a new one.
			LOG.info("Couldn't find any job with name: {}", RAINDANCE_JOB_NAME);
			return List.of();
		}
	}

	private JobStatus mapJobExecutionToJobStatus(final JobExecution jobExecution) {
		final JobStatus jobStatus = JobStatus.builder()
			.withStatus(jobExecution.getStatus().toString())
			.withStartTime(jobExecution.getStartTime())
			.withEndTime(jobExecution.getEndTime())
			.build();

		// We also want to know how many rows we read and wrote.
		jobExecution.getStepExecutions()
			.stream()
			.map(stepExecution -> new JobStatus.StepStatus(stepExecution.getStepName(), stepExecution.getReadCount(), stepExecution.getWriteCount()))
			.forEach(jobStatus::addStepStatus);

		return jobStatus;
	}
}
