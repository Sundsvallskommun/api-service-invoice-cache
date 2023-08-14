package se.sundsvall.invoicecache.integration.db;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import se.sundsvall.invoicecache.integration.db.entity.InvoiceEntity;

@CircuitBreaker(name = "invoiceEntityRepository")
public interface InvoiceEntityRepository extends JpaRepository<InvoiceEntity, Long>, JpaSpecificationExecutor<InvoiceEntity> {

	Optional<InvoiceEntity> findByInvoiceNumber(String invoiceNumber);

	Optional<InvoiceEntity> findByFileName(String fileName);
}
