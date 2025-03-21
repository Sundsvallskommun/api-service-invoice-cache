package se.sundsvall.invoicecache.service.batch.invoice;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import se.sundsvall.invoicecache.integration.db.InvoiceRepository;

@ExtendWith(MockitoExtension.class)
class InvoiceListenerTest {

	@Mock
	private InvoiceRepository mockRepository;
	@Mock
	private FetchInvoicesJobHealthIndicator mockHealthIndicator;

	@Mock
	private StepExecution mockStepExecution;

	@InjectMocks
	private InvoiceListener invoiceListener;

	@BeforeEach
	void setup() {
		when(mockRepository.count()).thenReturn(1L);
	}

	@Test
	void testBeforeStep() {

		doNothing().when(mockRepository).truncateTable();
		invoiceListener.beforeStep(mockStepExecution);

		verify(mockRepository, times(1)).count();
		verify(mockRepository, times(1)).truncateTable();
	}

	@Test
	void testSuccessfulAfterStep() {
		when(mockStepExecution.getExitStatus()).thenReturn(ExitStatus.COMPLETED);

		var exitStatus = invoiceListener.afterStep(mockStepExecution);
		assertNull(exitStatus); // intended

		verify(mockStepExecution, times(0)).getSummary();
		verify(mockHealthIndicator).setHealthy();
	}

	@Test
	void testFailedAfterStep() {
		when(mockStepExecution.getExitStatus()).thenReturn(ExitStatus.FAILED);

		var exitStatus = invoiceListener.afterStep(mockStepExecution);
		assertNull(exitStatus); // intended

		verify(mockStepExecution, times(1)).getSummary();
		verify(mockHealthIndicator).setUnhealthy();
	}
}
