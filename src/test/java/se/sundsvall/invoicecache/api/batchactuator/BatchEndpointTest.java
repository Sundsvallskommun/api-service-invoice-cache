package se.sundsvall.invoicecache.api.batchactuator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.ExitStatus;

import se.sundsvall.invoicecache.TestObjectFactory;
import se.sundsvall.invoicecache.service.batch.JobHelper;

@ExtendWith(MockitoExtension.class)
class BatchEndpointTest {
    
    @Mock
    private JobHelper mockJobHelper;
    
    @InjectMocks
    private BatchEndpoint batchEndpoint;
    
    @Test
    void health() {
        final JobStatus jobStatus = TestObjectFactory.generateCompletedJobStatus();
        
        when(mockJobHelper.getJobs()).thenReturn(List.of(jobStatus, jobStatus));
    
        final BatchHealth health = batchEndpoint.health();
        final List<JobStatus> statuses = health.getDetails().get("batchHistory");
        
        assertThat(statuses).hasSize(2)
                .allMatch(status -> status.getStepStatusMap().get("invoiceStep").getStepName().equals("invoiceStep"))
                .allMatch(status -> status.getStepStatusMap().get("invoiceStep").getStepWriteCount() == 5L)
                .allMatch(status -> status.getStepStatusMap().get("invoiceStep").getStepReadCount() == 6L)
                .allMatch(status -> status.getStatus().equals(ExitStatus.COMPLETED.toString()))
                .allMatch(status -> status.getStartTime().isEqual(LocalDateTime.of(2022, 1, 1, 1, 1, 1)))
                .allMatch(status -> status.getEndTime().isEqual(LocalDateTime.of(2022, 1, 1, 1, 2, 2)));
    }
}