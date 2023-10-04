package se.sundsvall.invoicecache.service.batch.backup;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import se.sundsvall.invoicecache.integration.db.entity.BackupInvoiceEntity;
import se.sundsvall.invoicecache.integration.db.entity.InvoiceEntity;
import se.sundsvall.invoicecache.integration.db.mapper.EntityMapper;

@Component
public class BackupProcessor implements ItemProcessor<InvoiceEntity, BackupInvoiceEntity> {
    
    private final EntityMapper entityMapper;
    
    public BackupProcessor(EntityMapper entityMapper) {
        this.entityMapper = entityMapper;
    }
    
    @Override
    public BackupInvoiceEntity process(final InvoiceEntity invoiceEntity) {
        return entityMapper.mapInvoiceEntityToBackupEntity(invoiceEntity);
    }
}
