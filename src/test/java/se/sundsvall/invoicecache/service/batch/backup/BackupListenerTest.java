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

@ExtendWith(MockitoExtension.class)
class BackupListenerTest {

	@Mock
	private BackupInvoiceRepository mockRepository;

	@Mock
	private StepExecution mockStepExecution;

	@InjectMocks
	private BackupListener backupListener;

	@Test
	void testBeforeStep() {
		doNothing().when(mockRepository).deleteAllInBatch();
		backupListener.beforeStep(mockStepExecution);

		verify(mockRepository, times(1)).deleteAllInBatch();
	}

	@Test
	void testSuccessfulAfterStep() {
		when(mockStepExecution.getExitStatus()).thenReturn(ExitStatus.COMPLETED);
		final ExitStatus exitStatus = backupListener.afterStep(mockStepExecution);
		assertNull(exitStatus); // intended
		verify(mockStepExecution, times(0)).getSummary();
	}

	@Test
	void testFailedAfterStep() {
		when(mockStepExecution.getExitStatus()).thenReturn(ExitStatus.FAILED);
		final ExitStatus exitStatus = backupListener.afterStep(mockStepExecution);
		assertNull(exitStatus); // intended
		verify(mockStepExecution, times(1)).getSummary();
	}
}
