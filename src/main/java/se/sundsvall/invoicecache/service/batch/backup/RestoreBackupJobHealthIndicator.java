package se.sundsvall.invoicecache.service.batch.backup;

import org.springframework.stereotype.Component;
import se.sundsvall.invoicecache.service.batch.BatchHealthIndicator;

@Component
class RestoreBackupJobHealthIndicator extends BatchHealthIndicator {

	static final String NAME = "RestoreBackupJob";

	RestoreBackupJobHealthIndicator() {
		super(NAME);
	}
}
