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

import se.sundsvall.invoicecache.service.InvoiceCacheService;

@Component
@Endpoint(id = "restorebackup")
public class RestoreBackupEndpoint {

    private static final Logger LOG = LoggerFactory.getLogger(RestoreBackupEndpoint.class);

    private final InvoiceCacheService service;

    public RestoreBackupEndpoint(InvoiceCacheService service) {
        this.service = service;
    }

    @ReadOperation
    public void restoreBackup() {
        try {
            service.forceRestoreBackup();
        } catch (JobInstanceAlreadyCompleteException | JobExecutionAlreadyRunningException | JobParametersInvalidException | JobRestartException e) {
            LOG.warn("Couldn't restore backup", e);
        }
    }
}
