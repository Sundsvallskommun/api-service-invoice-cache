package se.sundsvall.invoicecache.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.zalando.problem.violations.ConstraintViolationProblem;
import se.sundsvall.invoicecache.Application;
import se.sundsvall.invoicecache.integration.storage.scheduler.StorageSchedulerWorker;

@SpringBootTest(classes = Application.class, webEnvironment = RANDOM_PORT)
@ActiveProfiles("junit")
class SchedulerResourceTest {

	@MockitoBean
	private StorageSchedulerWorker storageSchedulerWorker;

	@Autowired
	private WebTestClient webTestClient;

	@AfterEach
	void tearDown() {
		verifyNoMoreInteractions(storageSchedulerWorker);
	}

	@Test
	void transferFile() {
		var municipalityId = "2281";

		webTestClient.post()
			.uri(uriBuilder -> uriBuilder.replacePath("/{municipalityId}/scheduler/transfer").build(municipalityId))
			.exchange()
			.expectStatus().isOk();

		verify(storageSchedulerWorker).transferFiles();
	}

	@Test
	void transferFile_withInvalidMunicipalityId() {
		var municipalityId = "invalid-id";

		var response = webTestClient.post()
			.uri(uriBuilder -> uriBuilder.replacePath("/{municipalityId}/scheduler/transfer").build(municipalityId))
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response.getTitle()).isEqualTo("Constraint Violation");
		assertThat(response.getViolations()).hasSize(1).satisfies(violation -> assertThat(violation).anySatisfy(v -> {
			assertThat(v.getField()).isEqualTo("transferFile.municipalityId");
			assertThat(v.getMessage()).isEqualTo("not a valid municipality ID");
		}));

		verify(storageSchedulerWorker, never()).transferFiles();
	}

	@Test
	void truncateFile() {
		var municipalityId = "2281";

		webTestClient.post()
			.uri(uriBuilder -> uriBuilder.replacePath("/{municipalityId}/scheduler/truncate").build(municipalityId))
			.exchange()
			.expectStatus().isOk();

		verify(storageSchedulerWorker).truncateFiles();
	}

	@Test
	void truncateFile_withInvalidMunicipalityId() {
		var municipalityId = "invalid-id";

		var response = webTestClient.post()
			.uri(uriBuilder -> uriBuilder.replacePath("/{municipalityId}/scheduler/truncate").build(municipalityId))
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response.getTitle()).isEqualTo("Constraint Violation");
		assertThat(response.getViolations()).hasSize(1).satisfies(violation -> assertThat(violation).anySatisfy(v -> {
			assertThat(v.getField()).isEqualTo("truncateFile.municipalityId");
			assertThat(v.getMessage()).isEqualTo("not a valid municipality ID");
		}));

		verify(storageSchedulerWorker, never()).truncateFiles();
	}

}
