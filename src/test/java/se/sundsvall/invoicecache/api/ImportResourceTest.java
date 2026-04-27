package se.sundsvall.invoicecache.api;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import se.sundsvall.dept44.problem.violations.ConstraintViolationProblem;
import se.sundsvall.invoicecache.Application;
import se.sundsvall.invoicecache.integration.storage.importer.SambaImportRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("junit")
class ImportResourceTest {

	@MockitoBean
	private SambaImportRunner runner;

	@Autowired
	private WebTestClient webTestClient;

	@AfterEach
	void tearDown() {
		verifyNoMoreInteractions(runner);
	}

	@Test
	void importSambaZips_started_returnsAccepted() {
		final var municipalityId = "2281";
		when(runner.tryStartImport(municipalityId)).thenReturn(true);

		webTestClient.post()
			.uri(uriBuilder -> uriBuilder.replacePath("/{municipalityId}/import/samba-zips").build(municipalityId))
			.exchange()
			.expectStatus().isAccepted();

		verify(runner).tryStartImport(municipalityId);
	}

	@Test
	void importSambaZips_alreadyRunning_returnsConflict() {
		final var municipalityId = "2281";
		when(runner.tryStartImport(municipalityId)).thenReturn(false);

		webTestClient.post()
			.uri(uriBuilder -> uriBuilder.replacePath("/{municipalityId}/import/samba-zips").build(municipalityId))
			.exchange()
			.expectStatus().isEqualTo(409);

		verify(runner).tryStartImport(municipalityId);
	}

	@Test
	void importSambaZips_invalidMunicipalityId_returnsBadRequest() {
		final var municipalityId = "invalid-id";

		final var response = webTestClient.post()
			.uri(uriBuilder -> uriBuilder.replacePath("/{municipalityId}/import/samba-zips").build(municipalityId))
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response.getTitle()).isEqualTo("Constraint Violation");
		assertThat(response.getViolations()).hasSize(1).satisfies(v -> assertThat(v).anySatisfy(violation -> {
			assertThat(violation.field()).isEqualTo("importSambaZips.municipalityId");
			assertThat(violation.message()).isEqualTo("not a valid municipality ID");
		}));

		verify(runner, never()).tryStartImport(any());
	}
}
