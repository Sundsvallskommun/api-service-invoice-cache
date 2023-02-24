package se.sundsvall.invoicecache.api.batchactuator;

import java.util.List;
import java.util.Map;

public class BatchHealth {
    
    private Map<String, List<JobStatus>> details;
    
    public Map<String, List<JobStatus>> getDetails() {
        return details;
    }
    
    public void setDetails(final Map<String, List<JobStatus>> details) {
        this.details = details;
    }
}
