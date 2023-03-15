package se.sundsvall.invoicecache.integration.db;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import se.sundsvall.invoicecache.integration.db.entity.PdfEntity;

public interface PdfEntityRepository extends JpaRepository<PdfEntity, Long>, JpaSpecificationExecutor<PdfEntity> {
    
    Optional<PdfEntity> findByFilename(String filename);
    
    Optional<PdfEntity> findByInvoiceNumberAndInvoiceId(String invoiceNumber, String invoiceId);
}
