package se.sundsvall.invoicecache.service.batch.backup;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;
import se.sundsvall.invoicecache.integration.db.EntityMapper;
import se.sundsvall.invoicecache.integration.db.entity.BackupInvoiceEntity;
import se.sundsvall.invoicecache.integration.db.entity.InvoiceEntity;

@Component
public class RestoreBackupProcessor implements ItemProcessor<BackupInvoiceEntity, InvoiceEntity> {
    
    private final EntityMapper mapper;
    
    public RestoreBackupProcessor(final EntityMapper mapper) {
        this.mapper = mapper;
    }
    
    @Override
    public InvoiceEntity process(final BackupInvoiceEntity backupEntity) {
        return mapper.mapBackupEntityToInvoiceEntity(backupEntity);
    }
}
