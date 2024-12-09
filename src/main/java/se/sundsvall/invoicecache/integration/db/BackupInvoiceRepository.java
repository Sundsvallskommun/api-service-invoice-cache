package se.sundsvall.invoicecache.integration.db;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.data.jpa.repository.JpaRepository;
import se.sundsvall.invoicecache.integration.db.entity.BackupInvoiceEntity;

@CircuitBreaker(name = "backupInvoiceRepository")
public interface BackupInvoiceRepository extends JpaRepository<BackupInvoiceEntity, Long> {
}
