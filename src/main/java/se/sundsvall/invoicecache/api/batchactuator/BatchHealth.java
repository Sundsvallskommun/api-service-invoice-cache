package se.sundsvall.invoicecache.api.batchactuator;

import java.util.List;
import java.util.Map;
import lombok.Data;

@Data
public class BatchHealth {

	private Map<String, List<JobStatus>> details;

}
