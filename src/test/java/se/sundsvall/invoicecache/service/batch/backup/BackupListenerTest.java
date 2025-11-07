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
		doNothing().when(mockRepository).truncateTable();
		backupListener.beforeStep(mockStepExecution);

		verify(mockRepository, times(1)).truncateTable();
	}

	@Test
	void testSuccessfulAfterStep() {
		when(mockStepExecution.getExitStatus()).thenReturn(ExitStatus.COMPLETED);
		final var exitStatus = backupListener.afterStep(mockStepExecution);
		assertThat(exitStatus).isNull(); // intended
		verify(mockStepExecution, times(0)).getSummary();
	}

	@Test
	void testFailedAfterStep() {
		when(mockStepExecution.getExitStatus()).thenReturn(ExitStatus.FAILED);
		final var exitStatus = backupListener.afterStep(mockStepExecution);
		assertThat(exitStatus).isNull(); // intended
		verify(mockStepExecution, times(1)).getSummary();
	}
}
