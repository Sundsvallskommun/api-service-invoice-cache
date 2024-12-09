package se.sundsvall.invoicecache.api.batchactuator;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.stereotype.Component;
import se.sundsvall.invoicecache.service.batch.JobHelper;

/**
 * Expose some useful information regarding batches
 */
@Component
@Endpoint(id = "batchhealth")
class BatchEndpoint {

	private final JobHelper jobHelper;

	BatchEndpoint(final JobHelper jobHelper) {
		this.jobHelper = jobHelper;
	}

	@ReadOperation
	BatchHealth health() {
		Map<String, List<JobStatus>> details = new LinkedHashMap<>();
		final List<JobStatus> jobs = jobHelper.getJobs();

		details.put("batchHistory", jobs);
		BatchHealth batchHealth = new BatchHealth();
		batchHealth.setDetails(details);
		return batchHealth;
	}
}
