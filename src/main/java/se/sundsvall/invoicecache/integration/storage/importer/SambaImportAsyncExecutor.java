package se.sundsvall.invoicecache.integration.storage.importer;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import net.javacrumbs.shedlock.core.LockConfiguration;
import net.javacrumbs.shedlock.core.LockProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class SambaImportAsyncExecutor {

	private static final Logger LOG = LoggerFactory.getLogger(SambaImportAsyncExecutor.class);
	static final String LOCK_NAME = "samba-zip-import";

	private final LockProvider lockProvider;
	private final SambaImportWorker worker;
	private final SambaImportProperties properties;

	public SambaImportAsyncExecutor(
		final LockProvider lockProvider,
		final SambaImportWorker worker,
		final SambaImportProperties properties) {
		this.lockProvider = lockProvider;
		this.worker = worker;
		this.properties = properties;
	}

	@Async
	public void executeAsync(final String municipalityId, final CompletableFuture<Boolean> acquisition) {
		final var config = new LockConfiguration(
			Instant.now(),
			LOCK_NAME,
			properties.lockAtMostFor(),
			properties.lockAtLeastFor());

		final var lockOptional = lockProvider.lock(config);
		if (lockOptional.isEmpty()) {
			LOG.info("Samba import lock '{}' is held elsewhere, skipping", LOCK_NAME);
			acquisition.complete(false);
			return;
		}
		acquisition.complete(true);

		final var lock = lockOptional.get();
		try {
			LOG.info("Samba import lock acquired, starting import for municipalityId='{}'", municipalityId);
			worker.importAll(municipalityId);
		} catch (final Exception e) {
			LOG.error("Samba import failed", e);
		} finally {
			try {
				lock.unlock();
				LOG.info("Samba import lock released");
			} catch (final Exception e) {
				LOG.error("Failed to release samba import lock", e);
			}
		}
	}
}
