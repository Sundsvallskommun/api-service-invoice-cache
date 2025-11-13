package se.sundsvall.invoicecache.integration.db;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.time.OffsetDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import se.sundsvall.invoicecache.integration.db.entity.PdfEntity;

@CircuitBreaker(name = "pdfRepository")
public interface PdfRepository extends JpaRepository<PdfEntity, Long>, JpaSpecificationExecutor<PdfEntity> {

	Optional<PdfEntity> findByFilenameAndMunicipalityId(String filename, String municipalityId);

	Optional<PdfEntity> findByInvoiceNumberAndInvoiceIdAndMunicipalityId(String invoiceNumber, String invoiceId, String municipalityId);

	Optional<PdfEntity> findFirstByMovedAtIsNullAndCreatedIsBeforeAndInvoiceIssuerLegalIdIsNot(OffsetDateTime created, String issuerLegalId);

	Optional<PdfEntity> findFirstByTruncatedAtIsNullAndMovedAtIsNotNull();

	default Optional<PdfEntity> findPdfToTransfer(final OffsetDateTime created, final String issuerLegalId) {
		return findFirstByMovedAtIsNullAndCreatedIsBeforeAndInvoiceIssuerLegalIdIsNot(created, issuerLegalId);
	}

	default Optional<PdfEntity> findPdfToTruncate() {
		return findFirstByTruncatedAtIsNullAndMovedAtIsNotNull();
	}

}
