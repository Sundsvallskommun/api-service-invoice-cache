package se.sundsvall.invoicecache.api.batchactuator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import se.sundsvall.invoicecache.service.Scheduler;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ActuatorServiceTest {

	@Mock
	private Scheduler mockScheduler;

	@InjectMocks
	private ActuatorService service;

	@Test
	void testForceFetchInvoices() throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {
		when(mockScheduler.fetchInvoices()).thenReturn(new JobExecution(1L));
		service.forceFetchInvoices();
		verify(mockScheduler, times(1)).fetchInvoices();
	}

	@Test
	void testForceRunBackup() throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {
		doNothing().when(mockScheduler).runBackup();
		service.forceCreateBackup();
		verify(mockScheduler, times(1)).runBackup();
	}

	@Test
	void testForceRestoreBackup() throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {
		doNothing().when(mockScheduler).restoreBackup();
		service.forceRestoreBackup();
		verify(mockScheduler, times(1)).restoreBackup();
	}
}
