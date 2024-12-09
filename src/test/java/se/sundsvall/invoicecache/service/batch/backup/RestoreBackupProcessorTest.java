package se.sundsvall.invoicecache.service.batch.backup;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.invoicecache.integration.db.entity.BackupInvoiceEntity;
import se.sundsvall.invoicecache.integration.db.entity.InvoiceEntity;
import se.sundsvall.invoicecache.integration.db.mapper.EntityMapper;

@ExtendWith(MockitoExtension.class)
class RestoreBackupProcessorTest {

	@Mock
	private EntityMapper mockEntityMapper;

	@InjectMocks
	private RestoreBackupProcessor restoreBackupProcessor;

	@Test
	void testProcess() {
		when(mockEntityMapper.mapBackupEntityToInvoiceEntity(any(BackupInvoiceEntity.class))).thenReturn(new InvoiceEntity());
		final InvoiceEntity process = restoreBackupProcessor.process(new BackupInvoiceEntity());
		assertNotNull(process);
	}
}
