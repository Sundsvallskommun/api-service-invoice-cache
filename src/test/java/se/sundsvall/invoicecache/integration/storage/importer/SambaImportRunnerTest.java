package se.sundsvall.invoicecache.integration.storage.importer;

import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class SambaImportRunnerTest {

	@Mock
	private SambaImportAsyncExecutor asyncExecutor;

	@InjectMocks
	private SambaImportRunner runner;

	@AfterEach
	void tearDown() {
		verifyNoMoreInteractions(asyncExecutor);
	}

	@Test
	@SuppressWarnings("unchecked")
	void tryStartImport_lockAcquired_returnsTrue() {
		doAnswer(invocation -> {
			final var future = (CompletableFuture<Boolean>) invocation.getArgument(1);
			future.complete(true);
			return null;
		}).when(asyncExecutor).executeAsync(eq("2281"), any());

		final var result = runner.tryStartImport("2281");

		assertThat(result).isTrue();
		verify(asyncExecutor).executeAsync(eq("2281"), any());
	}

	@Test
	@SuppressWarnings("unchecked")
	void tryStartImport_lockContention_returnsFalse() {
		doAnswer(invocation -> {
			final var future = (CompletableFuture<Boolean>) invocation.getArgument(1);
			future.complete(false);
			return null;
		}).when(asyncExecutor).executeAsync(eq("2281"), any());

		final var result = runner.tryStartImport("2281");

		assertThat(result).isFalse();
		verify(asyncExecutor).executeAsync(eq("2281"), any());
	}

	@Test
	void tryStartImport_acquisitionTimeout_returnsTrue() {
		// async executor never completes the future within 2s — runner assumes acquired
		final var result = runner.tryStartImport("2281");

		assertThat(result).isTrue();
		verify(asyncExecutor).executeAsync(eq("2281"), any());
	}

	@Test
	@SuppressWarnings("unchecked")
	void tryStartImport_executionException_returnsFalse() {
		doAnswer(invocation -> {
			final var future = (CompletableFuture<Boolean>) invocation.getArgument(1);
			future.completeExceptionally(new RuntimeException("boom"));
			return null;
		}).when(asyncExecutor).executeAsync(eq("2281"), any());

		final var result = runner.tryStartImport("2281");

		assertThat(result).isFalse();
		verify(asyncExecutor).executeAsync(eq("2281"), any());
	}

	@Test
	void tryStartImport_interrupted_returnsFalse() {
		Thread.currentThread().interrupt();
		try {
			final var result = runner.tryStartImport("2281");

			assertThat(result).isFalse();
			assertThat(Thread.currentThread().isInterrupted()).isTrue();
		} finally {
			// Clear the interrupt flag so the rest of the test suite isn't affected.
			final var interrupt = Thread.interrupted();
			assertThat(interrupt).isTrue();
		}
		verify(asyncExecutor).executeAsync(eq("2281"), any());
	}
}
