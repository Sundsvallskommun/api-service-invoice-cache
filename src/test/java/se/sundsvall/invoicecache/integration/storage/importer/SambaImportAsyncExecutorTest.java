package se.sundsvall.invoicecache.integration.storage.importer;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import net.javacrumbs.shedlock.core.LockConfiguration;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.core.SimpleLock;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SambaImportAsyncExecutorTest {

	@Mock
	private LockProvider lockProvider;

	@Mock
	private SambaImportWorker worker;

	@Mock
	private SambaImportProperties properties;

	@Mock
	private SimpleLock lock;

	@Captor
	private ArgumentCaptor<LockConfiguration> lockConfigurationCaptor;

	@InjectMocks
	private SambaImportAsyncExecutor executor;

	@AfterEach
	void tearDown() {
		verifyNoMoreInteractions(lockProvider, worker, properties, lock);
	}

	@Test
	void executeAsync_acquiredLock_runsWorkerAndUnlocks() {
		when(properties.lockAtMostFor()).thenReturn(Duration.ofMinutes(10));
		when(properties.lockAtLeastFor()).thenReturn(Duration.ofSeconds(30));
		when(lockProvider.lock(any())).thenReturn(Optional.of(lock));
		final var ack = new CompletableFuture<Boolean>();

		executor.executeAsync("2281", ack);

		assertThat(ack).isCompletedWithValue(true);
		verify(lockProvider).lock(lockConfigurationCaptor.capture());
		assertThat(lockConfigurationCaptor.getValue().getName()).isEqualTo(SambaImportAsyncExecutor.LOCK_NAME);
		assertThat(lockConfigurationCaptor.getValue().getLockAtMostFor()).isEqualTo(Duration.ofMinutes(10));
		assertThat(lockConfigurationCaptor.getValue().getLockAtLeastFor()).isEqualTo(Duration.ofSeconds(30));
		verify(properties).lockAtMostFor();
		verify(properties).lockAtLeastFor();
		verify(worker).importAll("2281");
		verify(lock).unlock();
	}

	@Test
	void executeAsync_lockNotAcquired_signalsFalseAndDoesNotRun() {
		when(properties.lockAtMostFor()).thenReturn(Duration.ofMinutes(10));
		when(properties.lockAtLeastFor()).thenReturn(Duration.ofSeconds(30));
		when(lockProvider.lock(any())).thenReturn(Optional.empty());
		final var ack = new CompletableFuture<Boolean>();

		executor.executeAsync("2281", ack);

		assertThat(ack).isCompletedWithValue(false);
		verify(lockProvider).lock(any());
		verify(properties).lockAtMostFor();
		verify(properties).lockAtLeastFor();
		verify(worker, never()).importAll(any());
	}

	@Test
	void executeAsync_workerThrows_stillUnlocks() {
		when(properties.lockAtMostFor()).thenReturn(Duration.ofMinutes(10));
		when(properties.lockAtLeastFor()).thenReturn(Duration.ofSeconds(30));
		when(lockProvider.lock(any())).thenReturn(Optional.of(lock));
		org.mockito.Mockito.doThrow(new RuntimeException("worker boom")).when(worker).importAll("2281");
		final var ack = new CompletableFuture<Boolean>();

		executor.executeAsync("2281", ack);

		assertThat(ack).isCompletedWithValue(true);
		verify(lockProvider).lock(any());
		verify(properties).lockAtMostFor();
		verify(properties).lockAtLeastFor();
		verify(worker).importAll("2281");
		verify(lock).unlock();
	}
}
