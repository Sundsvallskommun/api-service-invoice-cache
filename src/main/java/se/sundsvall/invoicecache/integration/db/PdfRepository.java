package se.sundsvall.invoicecache.integration.db;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import se.sundsvall.invoicecache.integration.db.entity.PdfEntity;

@CircuitBreaker(name = "pdfRepository")
public interface PdfRepository extends JpaRepository<PdfEntity, Long>, JpaSpecificationExecutor<PdfEntity> {

	Optional<PdfEntity> findByFilenameAndMunicipalityId(String filename, String municipalityId);

	Optional<PdfEntity> findByInvoiceNumberAndInvoiceIdAndMunicipalityId(String invoiceNumber, String invoiceId, String municipalityId);

	Optional<PdfEntity> findByInvoiceIdAndInvoiceIssuerLegalIdAndMunicipalityId(String invoiceId, String invoiceIssuerLegalId, String municipalityId);

	List<PdfEntity> findByMovedAtIsNullAndCreatedIsBeforeAndInvoiceIssuerLegalIdIsNot(OffsetDateTime created, String issuerLegalId, Limit limit);

	List<PdfEntity> findByTruncatedAtIsNullAndMovedAtIsNotNull(Limit limit);

	default List<PdfEntity> findPdfsToTruncate(final int maxResults) {
		return findByTruncatedAtIsNullAndMovedAtIsNotNull(Limit.of(maxResults));
	}

	default List<PdfEntity> findPdfsToTransfer(final OffsetDateTime created, final String issuerLegalId, final int maxResults) {
		return findByMovedAtIsNullAndCreatedIsBeforeAndInvoiceIssuerLegalIdIsNot(created, issuerLegalId, Limit.of(maxResults));
	}

}
