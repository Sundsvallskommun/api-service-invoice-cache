package se.sundsvall.invoicecache.integration.db;

import static java.time.OffsetDateTime.now;
import static java.time.ZoneId.systemDefault;
import static java.time.temporal.ChronoUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

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

}
