package se.sundsvall.invoicecache.service.batch.backup;

import static org.junit.jupiter.api.Assertions.assertNull;
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
import se.sundsvall.invoicecache.integration.db.InvoiceEntityRepository;

@ExtendWith(MockitoExtension.class)
class RestoreBackupListenerTest {

	@Mock
	private InvoiceEntityRepository mockInvoiceRepository;

	@Mock
	private BackupInvoiceRepository mockBackupInvoiceRepository;

	@Mock
	private StepExecution mockStepExecution;

	@InjectMocks
	private RestoreBackupListener invoiceListener;

	@Test
	void testBeforeStep() {
		doNothing().when(mockInvoiceRepository).deleteAllInBatch();
		when(mockBackupInvoiceRepository.count()).thenReturn(10L);
		invoiceListener.beforeStep(mockStepExecution);

		verify(mockInvoiceRepository, times(1)).deleteAllInBatch();
		verify(mockBackupInvoiceRepository, times(1)).count();
	}

	@Test
    void testSuccessfulAfterStep() {
        when(mockStepExecution.getExitStatus()).thenReturn(ExitStatus.COMPLETED);
        final ExitStatus exitStatus = invoiceListener.afterStep(mockStepExecution);
        assertNull(exitStatus); //intended
        verify(mockStepExecution, times(0)).getSummary();
    }

	@Test
    void testFailedAfterStep() {
        when(mockStepExecution.getExitStatus()).thenReturn(ExitStatus.FAILED);
        final ExitStatus exitStatus = invoiceListener.afterStep(mockStepExecution);
        assertNull(exitStatus); //intended
        verify(mockStepExecution, times(1)).getSummary();
    }

}
