package se.sundsvall.invoicecache.integration.storage.scheduler;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StorageSchedulerTest {

	@Mock
	private StorageSchedulerWorker storageSchedulerWorkerMock;

	@InjectMocks
	private StorageScheduler storageScheduler;

	@AfterEach
	void tearDown() {
		verifyNoMoreInteractions(storageSchedulerWorkerMock);
	}

	@Test
	void transferFiles() {
		storageScheduler.transferFiles();

		verify(storageSchedulerWorkerMock).transferFiles();
	}

	@Test
	void truncateFiles() {
		storageScheduler.truncateFiles();

		verify(storageSchedulerWorkerMock).truncateFiles();
	}

}
