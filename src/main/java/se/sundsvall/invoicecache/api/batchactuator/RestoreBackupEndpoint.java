package se.sundsvall.invoicecache.api.batchactuator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.job.parameters.InvalidJobParametersException;
import org.springframework.batch.core.launch.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.launch.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.launch.JobRestartException;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.stereotype.Component;

@Component
@Endpoint(id = "restorebackup")
class RestoreBackupEndpoint {

	private static final Logger LOG = LoggerFactory.getLogger(RestoreBackupEndpoint.class);

	private final ActuatorService actuatorService;

	RestoreBackupEndpoint(ActuatorService actuatorService) {
		this.actuatorService = actuatorService;
	}

	@ReadOperation
	void restoreBackup() {
		try {
			actuatorService.forceRestoreBackup();
		} catch (JobInstanceAlreadyCompleteException | JobExecutionAlreadyRunningException | InvalidJobParametersException | JobRestartException e) {
			LOG.warn("Couldn't restore backup", e);
		}
	}
}
