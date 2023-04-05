package se.sundsvall.invoicecache.api;

import static org.springframework.http.MediaType.ALL_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;
import static se.sundsvall.invoicecache.api.InvoiceCacheResource.PATH;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

import org.springdoc.api.annotations.ParameterObject;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;

import se.sundsvall.invoicecache.InvoiceCache;
import se.sundsvall.invoicecache.api.model.InvoiceFilterRequest;
import se.sundsvall.invoicecache.api.model.InvoicePdf;
import se.sundsvall.invoicecache.api.model.InvoicePdfFilterRequest;
import se.sundsvall.invoicecache.api.model.InvoicePdfRequest;
import se.sundsvall.invoicecache.api.model.InvoicePdfResponse;
import se.sundsvall.invoicecache.api.model.InvoicesResponse;
import se.sundsvall.invoicecache.service.InvoiceCacheService;
import se.sundsvall.invoicecache.service.InvoicePdfService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping(value = PATH)
@Tag(name = "Invoice Cache")
@ApiResponses({
    @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content(schema = @Schema(implementation = Problem.class))),
    @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content(schema = @Schema(implementation = Problem.class)))
})
public class InvoiceCacheResource {

    static final String PATH = "/invoices";
    static final String FILENAME_PATH = "/{filename}";

    private final InvoiceCacheService invoiceCacheService;
    private final InvoicePdfService invoicePdfService;

    public InvoiceCacheResource(final InvoiceCacheService invoiceCacheService,
            final InvoicePdfService invoicePdfService) {
        this.invoiceCacheService = invoiceCacheService;
        this.invoicePdfService = invoicePdfService;
    }

    @Operation(
        summary = "Search for and fetch invoices",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Successful Operation",
                content = @Content(schema = @Schema(implementation = InvoicesResponse.class)))
        }
    )
    @GetMapping(produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<InvoicesResponse> getInvoices(@Valid InvoiceFilterRequest request) {
        RequestValidator.validateRequest(request);

        return ResponseEntity.ok(invoiceCacheService.getInvoices(request));
    }

    @Operation(
        summary = "Fetch an invoice PDF via filename",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Successful Operation",
                content = @Content(schema = @Schema(implementation = InvoicePdfResponse.class))),
            @ApiResponse(
                responseCode = "404",
                description = "Not Found",
                content = @Content(schema = @Schema(implementation = Problem.class)))
        }
    )
    @GetMapping(FILENAME_PATH)
    public ResponseEntity<InvoicePdf> getInvoicePdf(@PathVariable @NotBlank String filename) {
        var invoicePdf = invoicePdfService.getInvoicePdf(filename);
    
        if (invoicePdf == null) {
            throw Problem.valueOf(Status.NOT_FOUND);
        }
        return ResponseEntity.ok(invoicePdf);
    }
    
    @GetMapping("/{issuerlegalid}/{invoicenumber}/pdf")
    public ResponseEntity<InvoicePdf> getInvoicePdf(@PathVariable String issuerlegalid,
        @PathVariable String invoicenumber,
        @ParameterObject @Valid InvoicePdfFilterRequest request) {
        var invoicePdf = invoicePdfService.getInvoicePdfByInvoiceNumber(issuerlegalid,
            invoicenumber, request);
        if (invoicePdf == null) {
            throw Problem.valueOf(Status.NOT_FOUND);
        }
        return ResponseEntity.ok(invoicePdf);
    }
    
    @Operation(
        summary = "Create/import an invoice",
        responses = {
            @ApiResponse(
                responseCode = "201",
                description = "Successful Operation",
                headers = @Header(name = HttpHeaders.LOCATION, schema = @Schema(type = "string")),
                content = @Content(mediaType = ALL_VALUE)
            )
        }
    )
    @PostMapping(consumes = APPLICATION_JSON_VALUE, produces = { ALL_VALUE, APPLICATION_PROBLEM_JSON_VALUE })
    public ResponseEntity<Void> importInvoice(@Valid @RequestBody final InvoicePdfRequest request) {
        var invoiceFilename = invoicePdfService.createOrUpdateInvoice(request);

        var uri = UriComponentsBuilder.newInstance()
            .path(PATH + FILENAME_PATH)
            .buildAndExpand(invoiceFilename)
            .toUri();

        return ResponseEntity.created(uri)
                //Work around that the API-manager sets the content-type to "application/octet-stream" when no content-type is set.
                .header(HttpHeaders.CONTENT_TYPE, ALL_VALUE)
                .build();
    }
}
