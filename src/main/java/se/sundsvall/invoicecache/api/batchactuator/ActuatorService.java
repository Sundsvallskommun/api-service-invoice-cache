package se.sundsvall.invoicecache.api.batchactuator;

import org.springframework.batch.core.job.parameters.InvalidJobParametersException;
import org.springframework.batch.core.launch.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.launch.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.launch.JobRestartException;
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
	public void forceFetchInvoices() throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, InvalidJobParametersException, JobRestartException {
		scheduler.fetchInvoices();
	}

	@Async
	public void forceCreateBackup() throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, InvalidJobParametersException, JobRestartException {
		scheduler.runBackup();
	}

	@Async
	public void forceRestoreBackup() throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, InvalidJobParametersException, JobRestartException {
		scheduler.restoreBackup();
	}
}
