package se.sundsvall.invoicecache.integration.db;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import se.sundsvall.invoicecache.integration.db.entity.InvoiceEntity;

public interface InvoiceEntityRepository extends JpaRepository<InvoiceEntity, Long>, JpaSpecificationExecutor<InvoiceEntity> {

    Optional<InvoiceEntity> findByInvoiceNumber(String invoiceNumber);
}
