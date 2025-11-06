package se.sundsvall.invoicecache.service.batch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static se.sundsvall.invoicecache.service.batch.invoice.BatchConfig.RAINDANCE_JOB_NAME;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.NoSuchJobException;
import se.sundsvall.invoicecache.integration.db.InvoiceRepository;

@ExtendWith(MockitoExtension.class)
class JobHelperTest {

	@Mock
	private JobExplorer mockJobExplorer;

	@Mock
	private InvoiceRepository mockInvoiceRepository;

	private JobHelper jobHelper;

	@BeforeEach
	void setup() {
		this.jobHelper = new JobHelper(mockJobExplorer, mockInvoiceRepository, Duration.ofMinutes(1L));
	}

	@Test
	void areInvoicesOutdated_shouldReturnFalse_whenFoundCompletedJob() throws NoSuchJobException {
		setupGetSuccessfulJobMethod(LocalDateTime.now(), ExitStatus.COMPLETED);
		when(mockInvoiceRepository.count()).thenReturn(100L);
		assertThat(jobHelper.areInvoicesOutdated()).isFalse();
	}

	@Test
	void areInvoicesOutdated_shouldReturnTrue_whenNoCompletedJob() throws NoSuchJobException {
		setupGetSuccessfulJobMethod(LocalDateTime.now().minusDays(7L), ExitStatus.COMPLETED);
		assertThat(jobHelper.areInvoicesOutdated()).isTrue();
	}

	@Test
	void testGetSuccessfulJobWithinTimePeriod_shouldReturnPopulatedExecution_whenWithinGivenTime() throws NoSuchJobException {
		final var jobName = setupGetSuccessfulJobMethod(LocalDateTime.now(), ExitStatus.COMPLETED);

		final var successfulJobWithinTimePeriod = jobHelper.getSuccessfulJobWithinTimePeriod(jobName);
		assertThat(successfulJobWithinTimePeriod.isPresent()).isTrue();
	}

	@Test
	void testGetSuccessfulJobWithinTimePeriod_shouldReturnEmptyExecution_whenOutsideGivenTime() throws NoSuchJobException {
		final var jobName = setupGetSuccessfulJobMethod(LocalDateTime.now().minusDays(7L), ExitStatus.COMPLETED);

		final var successfulJobWithinTimePeriod = jobHelper.getSuccessfulJobWithinTimePeriod(jobName);
		assertThat(successfulJobWithinTimePeriod.isPresent()).isFalse();
	}

	@Test
	void testGetSuccessfulJobWithinTimePeriod_shouldReturnEmptyExecution_whenNoCompletedExitStatus() throws NoSuchJobException {
		final var jobName = setupGetSuccessfulJobMethod(LocalDateTime.now(), ExitStatus.FAILED);

		final var successfulJobWithinTimePeriod = jobHelper.getSuccessfulJobWithinTimePeriod(jobName);
		assertThat(successfulJobWithinTimePeriod.isPresent()).isFalse();
	}

	@Test
	void testGetSuccessfulJobWithinTimePeriod_shouldThrowException_whenNoJobExists() throws NoSuchJobException {
		final var jobName = setupGetSuccessfulJobMethod(LocalDateTime.now(), ExitStatus.FAILED);// Ignore job name, we want to get a job that doesn't exist.
		when(mockJobExplorer.getJobInstanceCount(jobName)).thenThrow(new NoSuchJobException("missing job"));
		final var successfulJobWithinTimePeriod = jobHelper.getSuccessfulJobWithinTimePeriod(jobName);
		assertThat(successfulJobWithinTimePeriod.isPresent()).isFalse();
	}

	@Test
	void testGetJobsShouldReturnListOfLatestJobStatuses() throws NoSuchJobException {
		when(mockJobExplorer.getJobInstanceCount(RAINDANCE_JOB_NAME)).thenReturn(100L);

		final var jobInstance = new JobInstance(1L, RAINDANCE_JOB_NAME);
		final var jobList = List.of(jobInstance);

		final var jobExecution = new JobExecution(jobInstance, null);
		jobExecution.setStatus(BatchStatus.COMPLETED);
		jobExecution.setStartTime(LocalDateTime.of(2022, 8, 10, 1, 10, 0));
		jobExecution.setEndTime(LocalDateTime.of(2022, 8, 10, 1, 10, 10));
		jobExecution.setJobInstance(jobInstance);

		final var step = new StepExecution("stepName", jobExecution);
		step.setReadCount(15);
		step.setWriteCount(20);

		jobExecution.addStepExecutions(List.of(step));

		when(mockJobExplorer.getJobInstances(RAINDANCE_JOB_NAME, 0, 50)).thenReturn(jobList);
		when(mockJobExplorer.getJobExecutions(jobInstance)).thenReturn(List.of(jobExecution));

		final var jobs = jobHelper.getJobs();
		assertThat(jobs).isNotNull().hasSize(1);
		assertThat(jobs.getFirst()).satisfies(job -> {
			assertThat(job.getStartTime()).isEqualTo(LocalDateTime.of(2022, 8, 10, 1, 10, 0));
			assertThat(job.getEndTime()).isEqualTo(LocalDateTime.of(2022, 8, 10, 1, 10, 10));
			assertThat(job.getStatus()).isEqualTo(BatchStatus.COMPLETED.toString());
			assertThat(job.getStepStatusMap().get("stepName").getStepName()).isEqualTo("stepName");
			assertThat(job.getStepStatusMap().get("stepName").getStepReadCount()).isEqualTo(15L);
			assertThat(job.getStepStatusMap().get("stepName").getStepWriteCount()).isEqualTo(20L);
		});
	}

	@Test
	void testInvoiceTableHasInvoices_shouldReturnTrueWhenMoreThanZero() {
		when(mockInvoiceRepository.count()).thenReturn(123456L);
		final var hasInvoices = jobHelper.invoiceTableHasInvoices();
		assertThat(hasInvoices).isTrue();
	}

	@Test
	void testInvoiceTableHasInvoices_shouldReturnFalseWhenZero() {
		when(mockInvoiceRepository.count()).thenReturn(0L);
		final var hasInvoices = jobHelper.invoiceTableHasInvoices();
		assertThat(hasInvoices).isFalse();
	}

	/**
	 * Set some boilerplate stuff and make the parameters that matters configurable
	 *
	 * @param endTime    which endtime to set for the last successful job
	 * @param exitStatus exitStatus, everything other than "COMPLETED" will be ignored.
	 */
	private String setupGetSuccessfulJobMethod(final LocalDateTime endTime, ExitStatus exitStatus) throws NoSuchJobException {
		// We want to see if there are any successful jobs within the last minute
		final var jobInstance = new JobInstance(1L, RAINDANCE_JOB_NAME);
		final var jobList = List.of(jobInstance);

		final var jobExecution = new JobExecution(jobInstance, null);
		jobExecution.setExitStatus(exitStatus);

		jobExecution.setEndTime(endTime);    // Last successful job is now, which means we should get a successful job.
		final var executionList = List.of(jobExecution);

		// Lenient since we want to reuse this method.
		Mockito.lenient().when(mockJobExplorer.getJobInstanceCount(eq(RAINDANCE_JOB_NAME))).thenReturn(1L);
		Mockito.lenient().when(mockJobExplorer.getJobInstances(eq(RAINDANCE_JOB_NAME), eq(0), eq(1))).thenReturn(jobList);
		Mockito.lenient().when(mockJobExplorer.getJobExecutions(jobInstance)).thenReturn(executionList);
		return RAINDANCE_JOB_NAME;
	}
}
