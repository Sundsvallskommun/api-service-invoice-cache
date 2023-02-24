package se.sundsvall.invoicecache.api.model;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder(setterPrefix = "with")
public record InvoicePdfRequest(

    @Schema(description = "Invoice issuer legal id")
    String issuerLegalId,

    @Schema(description = "Invoice debtor legal id")
    String debtorLegalId,

    @Schema(description = "Invoice number")
    String invoiceNumber,

    @Schema(description = "Invoice id")
    String invoiceId,

    @Schema(description = "Invoice name")
    String invoiceName,

    @Schema(description = "Invoice type")
    InvoiceType invoiceType,

    @Valid
    @NotNull
    @Schema(description = "Attachment", requiredMode = REQUIRED)
    InvoicePdf attachment) { }
