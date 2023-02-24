package se.sundsvall.invoicecache.integration.db;

import org.springframework.data.jpa.repository.JpaRepository;
import se.sundsvall.invoicecache.integration.db.entity.BackupInvoiceEntity;

public interface BackupInvoiceRepository extends JpaRepository<BackupInvoiceEntity, Long> {
}
