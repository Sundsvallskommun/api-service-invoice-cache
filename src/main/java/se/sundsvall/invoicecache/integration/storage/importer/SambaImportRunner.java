package se.sundsvall.invoicecache.integration.storage.importer;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class SambaImportRunner {

	private static final Logger LOG = LoggerFactory.getLogger(SambaImportRunner.class);
	static final long ACQUISITION_TIMEOUT_SECONDS = 2L;

	private final SambaImportAsyncExecutor asyncExecutor;

	public SambaImportRunner(final SambaImportAsyncExecutor asyncExecutor) {
		this.asyncExecutor = asyncExecutor;
	}

	public boolean tryStartImport(final String municipalityId) {
		final var acquisition = new CompletableFuture<Boolean>();
		asyncExecutor.executeAsync(municipalityId, acquisition);
		try {
			return acquisition.get(ACQUISITION_TIMEOUT_SECONDS, TimeUnit.SECONDS);
		} catch (final TimeoutException e) {
			LOG.warn("Did not receive lock acquisition signal within {}s, assuming acquired", ACQUISITION_TIMEOUT_SECONDS);
			return true;
		} catch (final InterruptedException e) {
			Thread.currentThread().interrupt();
			LOG.warn("Interrupted waiting for lock acquisition signal", e);
			return false;
		} catch (final Exception e) {
			LOG.error("Failed waiting for lock acquisition signal", e);
			return false;
		}
	}
}
