package se.sundsvall.invoicecache.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import se.sundsvall.dept44.common.validators.annotation.ValidMunicipalityId;
import se.sundsvall.dept44.problem.Problem;
import se.sundsvall.dept44.problem.violations.ConstraintViolationProblem;
import se.sundsvall.invoicecache.integration.storage.importer.SambaImportRunner;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;

@Validated
@RestController
@RequestMapping(value = "/{municipalityId}/import")
@Tag(name = "Import Resource")
@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(oneOf = {
	Problem.class, ConstraintViolationProblem.class
})))
@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
class ImportResource {

	private final SambaImportRunner runner;

	ImportResource(final SambaImportRunner runner) {
		this.runner = runner;
	}

	@Operation(
		summary = "Triggers a one-shot background import of historical invoice zips from the configured Samba sibling folder",
		responses = {
			@ApiResponse(responseCode = "202", description = "Accepted - import started"),
			@ApiResponse(responseCode = "409",
				description = "Conflict - another import is already running",
				content = @Content(mediaType = APPLICATION_PROBLEM_JSON_VALUE, schema = @Schema(implementation = Problem.class)))
		})
	@PostMapping("/samba-zips")
	ResponseEntity<Void> importSambaZips(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId) {
		if (runner.tryStartImport(municipalityId)) {
			return ResponseEntity.accepted().build();
		}
		throw Problem.valueOf(CONFLICT, "Samba import already running");
	}
}
