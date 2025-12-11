package se.sundsvall.invoicecache.integration.db;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import se.sundsvall.invoicecache.integration.db.entity.PdfEntity;

@CircuitBreaker(name = "pdfRepository")
public interface PdfRepository extends JpaRepository<PdfEntity, Integer>, JpaSpecificationExecutor<PdfEntity> {

	Optional<PdfEntity> findByFilenameAndMunicipalityId(String filename, String municipalityId);

	Optional<PdfEntity> findByInvoiceNumberAndInvoiceIdAndMunicipalityId(String invoiceNumber, String invoiceId, String municipalityId);

	Optional<PdfEntity> findByInvoiceIdAndInvoiceIssuerLegalIdAndMunicipalityId(String invoiceId, String invoiceIssuerLegalId, String municipalityId);

	int countAllByInvoiceIssuerLegalIdAndCreatedIsBefore(String invoiceIssuerLegalId, OffsetDateTime created);

	@Modifying
	@Query("""
		    DELETE FROM PdfEntity p
		    WHERE p.invoiceIssuerLegalId = :issuerLegalId
		      AND p.created < :before
		""")
	int deleteAllOldRaindancePdfs(
		@Param("issuerLegalId") String issuerLegalId,
		@Param("before") OffsetDateTime before);

	@Query("SELECT pdfentity.id FROM PdfEntity pdfentity WHERE pdfentity.movedAt IS NULL AND pdfentity.created < :created AND pdfentity.invoiceIssuerLegalId <> :issuerLegalId")
	List<Integer> findIdsByMovedAtIsNullAndCreatedIsBeforeAndInvoiceIssuerLegalIdIsNot(
		@Param("created") OffsetDateTime created,
		@Param("issuerLegalId") String issuerLegalId);

	List<PdfEntity> findByTruncatedAtIsNullAndMovedAtIsNotNull(Limit limit);

	default List<PdfEntity> findPdfsToTruncate(final int maxResults) {
		return findByTruncatedAtIsNullAndMovedAtIsNotNull(Limit.of(maxResults));
	}

	default List<Integer> findPdfIdsToTransfer(final OffsetDateTime created, final String issuerLegalId) {
		return findIdsByMovedAtIsNullAndCreatedIsBeforeAndInvoiceIssuerLegalIdIsNot(created, issuerLegalId);
	}

}
