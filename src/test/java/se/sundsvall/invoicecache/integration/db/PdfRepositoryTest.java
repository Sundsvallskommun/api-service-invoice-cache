package se.sundsvall.invoicecache.integration.db;

import static java.time.OffsetDateTime.now;
import static java.time.ZoneId.systemDefault;
import static java.time.temporal.ChronoUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static se.sundsvall.invoicecache.Constant.RAINDANCE_ISSUER_LEGAL_ID;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import se.sundsvall.invoicecache.integration.db.entity.PdfEntity;

/**
 * Pdf entity repository tests.
 */
@SpringBootTest
@ActiveProfiles("junit")
class PdfRepositoryTest {

	@Autowired
	private PdfRepository repository;

	@Test
	void create() {
		final var filename = "invoice_000099.pdf";
		final var municipalityId = "2281";
		repository.save(PdfEntity.builder().withMunicipalityId(municipalityId).withFilename(filename).build());

		assertThat(repository.findByFilenameAndMunicipalityId(filename, municipalityId).orElseThrow().getCreated()).isCloseTo(now(systemDefault()), within(2, SECONDS));
	}

	/**
	 * Created 3 pds that match the criteria for transfer, but only 2 should be returned due to maxResults.
	 */
	@Test
	void findPdfsToTransfer() {
		var maxResults = 2;
		var pdfEntity1 = PdfEntity.builder()
			.withFilename("transferFile1.pdf")
			.withInvoiceIssuerLegalId("1234567890")
			.withMovedAt(null)
			.build();
		var pdfEntity2 = PdfEntity.builder()
			.withFilename("transferFile2.pdf")
			.withInvoiceIssuerLegalId("1234567890")
			.withMovedAt(null)
			.build();
		var pdfEntity3 = PdfEntity.builder()
			.withFilename("transferFile3.pdf")
			.withInvoiceIssuerLegalId("1234567890")
			.withMovedAt(null)
			.build();
		repository.saveAll(List.of(pdfEntity1, pdfEntity2, pdfEntity3));

		var result = repository.findPdfsToTransfer(now().plusMonths(6), RAINDANCE_ISSUER_LEGAL_ID, maxResults);

		assertThat(result).hasSize(maxResults);
	}

	/**
	 * Created 3 pds that match the criteria for truncation, but only 2 should be returned due to maxResults.
	 */
	@Test
	void findPdfsToTruncate() {
		var maxResults = 2;
		var pdfEntity1 = PdfEntity.builder()
			.withFilename("truncateFile1.pdf")
			.withMovedAt(now())
			.withTruncatedAt(null)
			.build();
		var pdfEntity2 = PdfEntity.builder()
			.withFilename("truncateFile2.pdf")
			.withMovedAt(now())
			.withTruncatedAt(null)
			.build();
		var pdfEntity3 = PdfEntity.builder()
			.withFilename("truncateFile3.pdf")
			.withMovedAt(now())
			.withTruncatedAt(null)
			.build();
		repository.saveAll(List.of(pdfEntity1, pdfEntity2, pdfEntity3));

		var result = repository.findPdfsToTruncate(maxResults);

		assertThat(result).hasSize(maxResults);
	}

}
