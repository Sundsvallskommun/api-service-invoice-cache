package se.sundsvall.invoicecache.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zalando.problem.Problem;
import org.zalando.problem.violations.ConstraintViolationProblem;
import se.sundsvall.dept44.common.validators.annotation.ValidMunicipalityId;
import se.sundsvall.invoicecache.integration.storage.scheduler.StorageSchedulerWorker;

import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;

@Validated
@RestController
@RequestMapping(value = "/{municipalityId}/scheduler")
@Tag(name = "Scheduler Resource")
@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(oneOf = {
	Problem.class, ConstraintViolationProblem.class
})))
@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
class SchedulerResource {

	private final StorageSchedulerWorker storageSchedulerWorker;

	SchedulerResource(final StorageSchedulerWorker storageSchedulerWorker) {
		this.storageSchedulerWorker = storageSchedulerWorker;
	}

	@Operation(
		summary = "Triggers scheduled job that transfers a single file to Samba storage",
		responses = @ApiResponse(responseCode = "200", description = "Successful Operation", useReturnTypeSchema = true))
	@PostMapping("/transfer")
	ResponseEntity<Void> transferFile(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId) {
		storageSchedulerWorker.transferFiles(null);
		return ResponseEntity.ok().build();
	}

	@Operation(
		summary = "Triggers scheduled job that verifies a file is eligible for truncation and truncates the file from database storage",
		responses = @ApiResponse(responseCode = "200", description = "Successful Operation", useReturnTypeSchema = true))
	@PostMapping("/truncate")
	@Transactional
	ResponseEntity<Void> truncateFile(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId) {
		final var ids = storageSchedulerWorker.getFileIdsToTruncate();
		for (final Integer id : ids) {
			storageSchedulerWorker.truncateFile(id, null);
		}
		return ResponseEntity.ok().build();
	}

}
