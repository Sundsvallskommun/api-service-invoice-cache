package se.sundsvall.invoicecache.service.batch;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.NoSuchJobException;
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

	private final Duration successfulWithin;  // Check for successful jobs withing this Duration

	private final JobExplorer jobExplorer;
	private final InvoiceRepository invoiceRepository;

	public JobHelper(final JobExplorer jobExplorer, final InvoiceRepository invoiceRepository,
		@Value("${raindance.invoice.outdated}") final Duration timeToWait) {
		this.jobExplorer = jobExplorer;
		this.invoiceRepository = invoiceRepository;
		this.successfulWithin = timeToWait;
	}

	public boolean invoiceTableHasInvoices() {
		return invoiceRepository.count() > 0;
	}

	/**
	 * Check if there is a successful job within the last "minutesToCheck" minutes, if not, we should fetch invoices.
	 *
	 * @return true if we should update, false if not.
	 */
	public boolean areInvoicesOutdated() {
		boolean isOutdated = getSuccessfulJobWithinTimePeriod(RAINDANCE_JOB_NAME).isEmpty();

		if (isOutdated) {
			LOG.info("No successful job within the last {} minutes were found.", successfulWithin.toMinutes());
		} else if (invoiceRepository.count() == 0) {
			LOG.info("No invoices found in local DB, get them.");
			isOutdated = true;
		} else {
			if (getSuccessfulJobWithinTimePeriod(RAINDANCE_JOB_NAME).isPresent()) {
				LOG.debug("Found a completed successful job from: {}, not running a new one.", getSuccessfulJobWithinTimePeriod(RAINDANCE_JOB_NAME).orElseThrow().getEndTime());
			}
		}

		return isOutdated;
	}

	/**
	 * Check if there are any successful job within the last 24 hrs.
	 *
	 * @param jobName name of the job to check
	 */
	Optional<JobExecution> getSuccessfulJobWithinTimePeriod(final String jobName) {
		try {
			final int jobInstanceCount = (int) jobExplorer.getJobInstanceCount(jobName);

			// See of there are any successful jobs done within the last 24 hours.
			return jobExplorer.getJobInstances(jobName, 0, jobInstanceCount).stream()
				.map(jobExplorer::getJobExecutions)
				.flatMap(List<JobExecution>::stream)
				.filter(jobExecution -> jobExecution.getExitStatus().equals(ExitStatus.COMPLETED))
				.filter(jobExecution -> Objects.requireNonNull(jobExecution.getEndTime())
					.isAfter(LocalDateTime.now().minusMinutes(successfulWithin.toMinutes())))
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
		final List<JobStatus> listOfJobs = new ArrayList<>();
		final int jobsToFetch = 50;
		try {
			final int jobInstanceCount = (int) jobExplorer.getJobInstanceCount(RAINDANCE_JOB_NAME);
			int mostRecentInstances = jobInstanceCount;
			// Only get the latest 50
			if (jobInstanceCount > jobsToFetch) {
				mostRecentInstances = jobInstanceCount - (jobInstanceCount - jobsToFetch);
			}

			// The latest job has index 0. If there are a total of 20 execution, fetching #20 will get the first, which is why we
			// get from 0.
			return jobExplorer.getJobInstances(RAINDANCE_JOB_NAME, 0, mostRecentInstances)
				.stream()
				.map(jobExplorer::getJobExecutions)
				.flatMap(List<JobExecution>::stream)
				.map(this::mapJobExecutionToJobStatus)
				.toList();

		} catch (final NoSuchJobException _) {
			// If we can't find any job, we don't care, run a new one.
			LOG.info("Couldn't find any job with name: {}", RAINDANCE_JOB_NAME);
		}

		return listOfJobs;
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
