package se.sundsvall.invoicecache.service.scheduler;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class RemovalSchedulerTest {

	@Mock
	private RemovalWorker removalWorkerMock;

	@InjectMocks
	private RemovalScheduler removalScheduler;

	@AfterEach
	void tearDown() {
		verifyNoMoreInteractions(removalWorkerMock);
	}

	@Test
	void removeOldRaindanceInvoices() {
		removalScheduler.removeOldRaindanceInvoices();

		verify(removalWorkerMock).removeOldRaindanceInvoices();
	}

}
