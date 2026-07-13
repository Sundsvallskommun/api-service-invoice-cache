package se.sundsvall.invoicecache.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.parameters.InvalidJobParametersException;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.launch.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.launch.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.launch.JobRestartException;
import se.sundsvall.invoicecache.service.batch.JobHelper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static se.sundsvall.invoicecache.TestObjectFactory.createJobExecution;

@ExtendWith(MockitoExtension.class)
class SchedulerTest {

	@Mock
	private JobOperator mockJobOperator;

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
		this.scheduler = new Scheduler(mockJobOperator, mockInvoiceJob, mockBackupJob, mockRestoreBackupJob, mockJobHelper, true);
	}

	@Test
	void testLaunchJob_whenInvoicesAreOutdated_shouldFetchInvoicesAndCreateBackup()
		throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, InvalidJobParametersException, JobRestartException {

		when(mockJobHelper.areInvoicesOutdated()).thenReturn(true);
		when(mockJobOperator.start(eq(mockInvoiceJob), any(JobParameters.class))).thenReturn(createJobExecution(ExitStatus.COMPLETED));
		when(mockJobOperator.start(eq(mockBackupJob), any(JobParameters.class))).thenReturn(createJobExecution(ExitStatus.COMPLETED));
		when(mockJobHelper.invoiceTableHasInvoices()).thenReturn(true);
		scheduler.launchJob();

		verify(mockJobHelper, times(1)).areInvoicesOutdated();
		verify(mockJobOperator, times(1)).start(eq(mockInvoiceJob), any(JobParameters.class));
		verify(mockJobOperator, times(1)).start(eq(mockBackupJob), any(JobParameters.class));
	}

	@Test
	void testFetchingInvoicesFails_shouldRestoreBackup()
		throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, InvalidJobParametersException, JobRestartException {

		when(mockJobHelper.areInvoicesOutdated()).thenReturn(true);
		when(mockJobOperator.start(eq(mockInvoiceJob), any(JobParameters.class))).thenReturn(createJobExecution(ExitStatus.FAILED));
		when(mockJobOperator.start(eq(mockRestoreBackupJob), any(JobParameters.class))).thenReturn(createJobExecution(ExitStatus.COMPLETED));
		scheduler.launchJob();

		verify(mockJobHelper, times(1)).areInvoicesOutdated();
		verify(mockJobOperator, times(1)).start(eq(mockInvoiceJob), any(JobParameters.class));
		verify(mockJobOperator, times(0)).start(eq(mockBackupJob), any(JobParameters.class));
		verify(mockJobOperator, times(1)).start(eq(mockRestoreBackupJob), any(JobParameters.class));
	}

	@Test
	void testBackupsAreRecent_shouldNotDoAnything() throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, InvalidJobParametersException, JobRestartException {
		when(mockJobHelper.areInvoicesOutdated()).thenReturn(false);
		scheduler.launchJob();

		verify(mockJobHelper, times(1)).areInvoicesOutdated();
		verify(mockJobOperator, times(0)).start(eq(mockInvoiceJob), any(JobParameters.class));
		verify(mockJobOperator, times(0)).start(eq(mockBackupJob), any(JobParameters.class));
		verify(mockJobOperator, times(0)).start(eq(mockRestoreBackupJob), any(JobParameters.class));
	}

	@Test
	void testSchedulingIsDisabled_shouldNotRunAnyJobs() throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, InvalidJobParametersException, JobRestartException {
		// Disable scheduling
		scheduler = new Scheduler(mockJobOperator, mockInvoiceJob, mockBackupJob, mockRestoreBackupJob, mockJobHelper, false);
		scheduler.launchJob();

		verifyNoInteractions(mockJobOperator, mockInvoiceJob, mockBackupJob, mockRestoreBackupJob, mockJobHelper);
	}
}
