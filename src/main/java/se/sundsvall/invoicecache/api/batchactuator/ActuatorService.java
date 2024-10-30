package se.sundsvall.invoicecache.api.batchactuator;

import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import se.sundsvall.invoicecache.service.Scheduler;

/**
 * Class / methods only to be used by actuators.
 */
@Component
class ActuatorService {

	private final Scheduler scheduler;

	ActuatorService(Scheduler scheduler) {
		this.scheduler = scheduler;
	}

	@Async
	public void forceFetchInvoices() throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {
		scheduler.fetchInvoices();
	}

	@Async
	public void forceCreateBackup() throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {
		scheduler.runBackup();
	}

	@Async
	public void forceRestoreBackup() throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {
		scheduler.restoreBackup();
	}
}
