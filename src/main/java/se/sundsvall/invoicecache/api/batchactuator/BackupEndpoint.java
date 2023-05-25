package se.sundsvall.invoicecache.api.batchactuator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.stereotype.Component;

@Component
@Endpoint(id = "backup")
class BackupEndpoint {

    private static final Logger LOG = LoggerFactory.getLogger(BackupEndpoint.class);

    private final ActuatorService actuatorService;

    BackupEndpoint(ActuatorService actuatorService) {
        this.actuatorService = actuatorService;
    }

    @ReadOperation
    void backup() {
        try {
            actuatorService.forceCreateBackup();
        } catch (JobInstanceAlreadyCompleteException | JobExecutionAlreadyRunningException | JobParametersInvalidException | JobRestartException e) {
            LOG.warn("Couldn't create backup", e);
        }
    }
}
