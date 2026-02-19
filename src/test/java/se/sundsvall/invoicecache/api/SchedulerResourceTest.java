package se.sundsvall.invoicecache.api;

import java.util.List;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

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
		final var municipalityId = "2281";

		webTestClient.post()
			.uri(uriBuilder -> uriBuilder.replacePath("/{municipalityId}/scheduler/transfer").build(municipalityId))
			.exchange()
			.expectStatus().isOk();

		verify(storageSchedulerWorker).transferFiles(any());
	}

	@Test
	void transferFile_withInvalidMunicipalityId() {
		final var municipalityId = "invalid-id";

		final var response = webTestClient.post()
			.uri(uriBuilder -> uriBuilder.replacePath("/{municipalityId}/scheduler/transfer").build(municipalityId))
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response.getTitle()).isEqualTo("Constraint Violation");
		assertThat(response.getViolations()).hasSize(1).satisfies(violation -> assertThat(violation).anySatisfy(v -> {
			assertThat(v.getField()).isEqualTo("transferFile.municipalityId");
			assertThat(v.getMessage()).isEqualTo("not a valid municipality ID");
		}));

		verify(storageSchedulerWorker, never()).transferFiles(any());
	}

	@Test
	void truncateFile() {
		final var municipalityId = "2281";

		when(storageSchedulerWorker.getFileIdsToTruncate()).thenReturn(List.of(1));

		webTestClient.post()
			.uri(uriBuilder -> uriBuilder.replacePath("/{municipalityId}/scheduler/truncate").build(municipalityId))
			.exchange()
			.expectStatus().isOk();

		verify(storageSchedulerWorker).getFileIdsToTruncate();
		verify(storageSchedulerWorker).truncateFile(1, null);

	}

	@Test
	void truncateFile_withInvalidMunicipalityId() {
		final var municipalityId = "invalid-id";

		final var response = webTestClient.post()
			.uri(uriBuilder -> uriBuilder.replacePath("/{municipalityId}/scheduler/truncate").build(municipalityId))
			.exchange()
			.expectStatus().isBadRequest()
			.expectBody(ConstraintViolationProblem.class)
			.returnResult()
			.getResponseBody();

		assertThat(response).isNotNull();
		assertThat(response.getTitle()).isEqualTo("Constraint Violation");
		assertThat(response.getViolations()).hasSize(1).satisfies(violation -> assertThat(violation).anySatisfy(v -> {
			assertThat(v.getField()).isEqualTo("truncateFile.municipalityId");
			assertThat(v.getMessage()).isEqualTo("not a valid municipality ID");
		}));

		verify(storageSchedulerWorker, never()).getFileIdsToTruncate();
		verify(storageSchedulerWorker, never()).truncateFile(any(), any());
	}

}
