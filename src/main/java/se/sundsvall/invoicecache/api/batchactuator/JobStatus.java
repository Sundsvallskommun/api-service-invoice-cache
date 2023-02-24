package se.sundsvall.invoicecache.api.batchactuator;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@Builder(setterPrefix = "with")
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class JobStatus {
    
    private String status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    
    @Builder.Default
    private Map<String, StepStatus> stepStatusMap = new HashMap<>();
    
    public void addStepStatus(StepStatus stepStatus) {
        this.stepStatusMap.put(stepStatus.stepName, stepStatus);
    }
    
    @Getter
    @Setter
    @Builder(setterPrefix = "with")
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    public static class StepStatus {
        private String stepName;
        private long stepReadCount;
        private long stepWriteCount;
    }
}
