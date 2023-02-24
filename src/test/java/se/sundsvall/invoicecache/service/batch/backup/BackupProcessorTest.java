package se.sundsvall.invoicecache.service.batch.backup;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.invoicecache.integration.db.EntityMapper;
import se.sundsvall.invoicecache.integration.db.entity.BackupInvoiceEntity;
import se.sundsvall.invoicecache.integration.db.entity.InvoiceEntity;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BackupProcessorTest {
    
    @Mock
    private EntityMapper mockEntityMapper;
    
    @InjectMocks
    private BackupProcessor backupProcessor;
    
    @Test
    void testProcess() {
        when(mockEntityMapper.mapInvoiceEntityToBackupEntity(any(InvoiceEntity.class))).thenReturn(new BackupInvoiceEntity());
        final BackupInvoiceEntity backupEntity = backupProcessor.process(new InvoiceEntity());
        assertNotNull(backupEntity);
    }
}