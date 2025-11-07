package se.sundsvall.invoicecache.service.batch.backup;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import se.sundsvall.invoicecache.integration.db.BackupInvoiceRepository;
import se.sundsvall.invoicecache.integration.db.InvoiceRepository;

@ExtendWith(MockitoExtension.class)
class RestoreBackupListenerTest {

	@Mock
	private InvoiceRepository mockInvoiceRepository;

	@Mock
	private BackupInvoiceRepository mockBackupInvoiceRepository;

	@Mock
	private RestoreBackupJobHealthIndicator mockHealthIndicator;

	@Mock
	private StepExecution mockStepExecution;

	@InjectMocks
	private RestoreBackupListener invoiceListener;

	@Test
	void testBeforeStep() {
		doNothing().when(mockInvoiceRepository).truncateTable();
		when(mockBackupInvoiceRepository.count()).thenReturn(10L);

		invoiceListener.beforeStep(mockStepExecution);

		verify(mockInvoiceRepository, times(1)).truncateTable();
		verify(mockBackupInvoiceRepository, times(1)).count();
	}

	@Test
	void testSuccessfulAfterStep() {
		when(mockStepExecution.getExitStatus()).thenReturn(ExitStatus.COMPLETED);

		final var exitStatus = invoiceListener.afterStep(mockStepExecution);
		assertThat(exitStatus).isNull(); // intended

		verify(mockStepExecution, times(0)).getSummary();
		verify(mockHealthIndicator).setHealthy();
	}

	@Test
	void testFailedAfterStep() {
		when(mockStepExecution.getExitStatus()).thenReturn(ExitStatus.FAILED);

		final var exitStatus = invoiceListener.afterStep(mockStepExecution);
		assertThat(exitStatus).isNull(); // intended

		verify(mockStepExecution, times(1)).getSummary();
		verify(mockHealthIndicator).setUnhealthy();
	}
}
