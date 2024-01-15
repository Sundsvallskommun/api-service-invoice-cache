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
class PdfEntityRepositoryTest {

	@Autowired
	private PdfEntityRepository repository;

	@Test
	void create() {
		final var filename = "invoice_000099.pdf";
		repository.save(PdfEntity.builder().withFilename(filename).build());

		assertThat(repository.findByFilename(filename).get().getCreated()).isCloseTo(now(systemDefault()), within(2, SECONDS));
	}
}
