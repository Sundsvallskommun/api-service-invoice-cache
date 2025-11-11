package se.sundsvall.invoicecache.api.model;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder(setterPrefix = "with")
public record InvoicePdfRequest(

	@Schema(description = "Invoice issuer legal id") String issuerLegalId,

	@Schema(description = "Invoice debtor legal id") String debtorLegalId,

	@Schema(description = "Invoice number") String invoiceNumber,

	@Schema(description = "Invoice id") String invoiceId,

	@Schema(description = "Invoice type", implementation = InvoiceType.class) InvoiceType invoiceType,

	@Valid @NotNull @Schema(description = "Attachment", requiredMode = REQUIRED) InvoicePdf attachment) {
}
