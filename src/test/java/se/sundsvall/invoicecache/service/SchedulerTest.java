package se.sundsvall.invoicecache.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.sundsvall.invoicecache.TestObjectFactory.createJobExecution;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import se.sundsvall.invoicecache.service.batch.JobHelper;

@ExtendWith(MockitoExtension.class)
class SchedulerTest {

	@Mock
	private JobLauncher mockJobLauncher;

	@Mock
	private Job mockBackupJob;

	@Mock
	private Job mockInvoiceJob;

	@Mock
	private Job mockRestoreBackupJob;

	@Mock
	private JobHelper mockJobHelper;

	private Scheduler scheduler;

	@BeforeEach
	void setup() {
		this.scheduler = new Scheduler(mockJobLauncher, mockInvoiceJob, mockBackupJob, mockRestoreBackupJob, mockJobHelper);
	}

	@Test
	void testLaunchJob_whenInvoicesAreOutdated_shouldFetchInvoicesAndCreateBackup()
		throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {

		when(mockJobHelper.areInvoicesOutdated()).thenReturn(true);
		when(mockJobLauncher.run(eq(mockInvoiceJob), any(JobParameters.class))).thenReturn(createJobExecution(ExitStatus.COMPLETED));
		when(mockJobLauncher.run(eq(mockBackupJob), any(JobParameters.class))).thenReturn(createJobExecution(ExitStatus.COMPLETED));
		when(mockJobHelper.invoiceTableHasInvoices()).thenReturn(true);
		scheduler.launchJob();

		verify(mockJobHelper).areInvoicesOutdated();
		verify(mockJobLauncher).run(eq(mockInvoiceJob), any(JobParameters.class));
		verify(mockJobLauncher).run(eq(mockBackupJob), any(JobParameters.class));
	}

	@Test
	void testFetchingInvoicesFails_shouldRestoreBackup()
		throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {

		when(mockJobHelper.areInvoicesOutdated()).thenReturn(true);
		when(mockJobLauncher.run(eq(mockInvoiceJob), any(JobParameters.class))).thenReturn(createJobExecution(ExitStatus.FAILED));
		when(mockJobLauncher.run(eq(mockRestoreBackupJob), any(JobParameters.class))).thenReturn(createJobExecution(ExitStatus.COMPLETED));
		scheduler.launchJob();

		verify(mockJobHelper).areInvoicesOutdated();
		verify(mockJobLauncher).run(eq(mockInvoiceJob), any(JobParameters.class));
		verify(mockJobLauncher, never()).run(eq(mockBackupJob), any(JobParameters.class));
		verify(mockJobLauncher).run(eq(mockRestoreBackupJob), any(JobParameters.class));
	}

	@Test
	void testBackupsAreRecent_shouldNotDoAnything() throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {
		when(mockJobHelper.areInvoicesOutdated()).thenReturn(false);
		scheduler.launchJob();

		verify(mockJobHelper).areInvoicesOutdated();
		verify(mockJobLauncher, never()).run(eq(mockInvoiceJob), any(JobParameters.class));
		verify(mockJobLauncher, never()).run(eq(mockBackupJob), any(JobParameters.class));
		verify(mockJobLauncher, never()).run(eq(mockRestoreBackupJob), any(JobParameters.class));
	}
}
