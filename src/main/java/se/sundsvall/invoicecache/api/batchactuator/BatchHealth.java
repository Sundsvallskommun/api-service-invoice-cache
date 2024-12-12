package se.sundsvall.invoicecache.api.batchactuator;

import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class BatchHealth {

	private Map<String, List<JobStatus>> details;

}
