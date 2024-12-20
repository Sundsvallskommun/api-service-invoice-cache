package se.sundsvall.invoicecache.integration.db;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import se.sundsvall.invoicecache.integration.db.entity.BackupInvoiceEntity;

@CircuitBreaker(name = "backupInvoiceRepository")
public interface BackupInvoiceRepository extends JpaRepository<BackupInvoiceEntity, Long> {

	@Modifying
	@Transactional
	@Query(value = "truncate table backupinvoice", nativeQuery = true)
	void truncateTable();
}
