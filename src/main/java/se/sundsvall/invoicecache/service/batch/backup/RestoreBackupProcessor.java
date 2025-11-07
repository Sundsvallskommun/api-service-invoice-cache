package se.sundsvall.invoicecache.service.batch.backup;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import se.sundsvall.invoicecache.integration.db.entity.BackupInvoiceEntity;
import se.sundsvall.invoicecache.integration.db.entity.InvoiceEntity;
import se.sundsvall.invoicecache.integration.db.mapper.EntityMapper;

@Component
public class RestoreBackupProcessor implements ItemProcessor<BackupInvoiceEntity, InvoiceEntity> {

	private final EntityMapper mapper;

	public RestoreBackupProcessor(final EntityMapper mapper) {
		this.mapper = mapper;
	}

	@Override
	public InvoiceEntity process(final @NonNull BackupInvoiceEntity backupEntity) {
		return mapper.mapBackupEntityToInvoiceEntity(backupEntity);
	}
}
