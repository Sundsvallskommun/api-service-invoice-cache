package se.sundsvall.invoicecache.api;

import static org.springframework.http.MediaType.ALL_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;
import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.web.util.UriComponentsBuilder.fromPath;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zalando.problem.Problem;

import se.sundsvall.dept44.common.validators.annotation.ValidMunicipalityId;
import se.sundsvall.invoicecache.api.model.InvoiceFilterRequest;
import se.sundsvall.invoicecache.api.model.InvoicePdf;
import se.sundsvall.invoicecache.api.model.InvoicePdfFilterRequest;
import se.sundsvall.invoicecache.api.model.InvoicePdfRequest;
import se.sundsvall.invoicecache.api.model.InvoicesResponse;
import se.sundsvall.invoicecache.service.InvoiceCacheService;
import se.sundsvall.invoicecache.service.InvoicePdfService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping(value = "/{municipalityId}/invoices")
@Tag(name = "Invoice Cache")
@ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(schema = @Schema(implementation = Problem.class)))
@ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(schema = @Schema(implementation = Problem.class)))
class InvoiceCacheResource {

	private final InvoiceCacheService invoiceCacheService;

	private final InvoicePdfService invoicePdfService;

	InvoiceCacheResource(final InvoiceCacheService invoiceCacheService,
		final InvoicePdfService invoicePdfService) {
		this.invoiceCacheService = invoiceCacheService;
		this.invoicePdfService = invoicePdfService;
	}

	@Operation(summary = "Search for and fetch invoices")
	@ApiResponse(responseCode = "200", description = "Successful Operation", useReturnTypeSchema = true)
	@GetMapping(produces = APPLICATION_JSON_VALUE)
	public ResponseEntity<InvoicesResponse> getInvoices(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@Valid final InvoiceFilterRequest request) {
		RequestValidator.validateRequest(request);

		return ok(invoiceCacheService.getInvoices(request, municipalityId));
	}

	@Operation(summary = "Fetch an invoice PDF via filename")
	@ApiResponse(responseCode = "200", description = "Successful Operation", useReturnTypeSchema = true)
	@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(implementation = Problem.class)))
	@GetMapping("/{filename}")
	public ResponseEntity<InvoicePdf> getInvoicePdf(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@PathVariable @NotBlank final String filename) {
		final var invoicePdf = invoicePdfService.getInvoicePdf(filename);

		return ok(invoicePdf);
	}

	@GetMapping("/{issuerlegalid}/{invoicenumber}/pdf")
	@Operation(summary = "Fetch an invoice PDF via issuer legal id and invoice number")
	@ApiResponse(responseCode = "200", description = "Successful Operation", useReturnTypeSchema = true)
	public ResponseEntity<InvoicePdf> getInvoicePdf(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@PathVariable final String issuerlegalid,
		@PathVariable final String invoicenumber,
		@ParameterObject @Valid final InvoicePdfFilterRequest request) {
		final var invoicePdf = invoicePdfService.getInvoicePdfByInvoiceNumber(issuerlegalid,
			invoicenumber, request);

		return ok(invoicePdf);
	}

	@Operation(summary = "Create/import an invoice")
	@ApiResponse(responseCode = "201", description = "Created", content = @Content(mediaType = ALL_VALUE), useReturnTypeSchema = true)
	@PostMapping(consumes = APPLICATION_JSON_VALUE, produces = {ALL_VALUE, APPLICATION_PROBLEM_JSON_VALUE})
	public ResponseEntity<Void> importInvoice(
		@Parameter(name = "municipalityId", description = "Municipality id", example = "2281") @ValidMunicipalityId @PathVariable final String municipalityId,
		@Valid @RequestBody final InvoicePdfRequest request) {
		final var invoiceFilename = invoicePdfService.createOrUpdateInvoice(request);

		return ResponseEntity.created(fromPath("/" + municipalityId + "/invoices/{filename}").buildAndExpand(invoiceFilename).toUri())
			.header(HttpHeaders.CONTENT_TYPE, ALL_VALUE)
			.build();
	}

}
