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
@Endpoint(id = "fetchinvoices")
class FetchInvoicesEndpoint {

    private static final Logger LOG = LoggerFactory.getLogger(FetchInvoicesEndpoint.class);

    private final ActuatorService actuatorService;

    FetchInvoicesEndpoint(ActuatorService actuatorService) {
        this.actuatorService = actuatorService;
    }

    @ReadOperation
    void fetchInvoices() {
        LOG.info("Manually fetching invoices");
        try {
            actuatorService.forceFetchInvoices();
        } catch (JobInstanceAlreadyCompleteException | JobExecutionAlreadyRunningException | JobParametersInvalidException | JobRestartException e) {
            LOG.warn("Couldn't start job for fetching invoices", e);
        }
    }
}
