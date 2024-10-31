package se.sundsvall.invoicecache.api.model;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Generated;

@Generated // To avoid having to create stupid coverage-only tests
@Builder(setterPrefix = "with")
public record InvoicePdfResponse(

	@Schema(description = "Invoice issuer legal id") String issuerLegalId,

	@Schema(description = "Invoice debtor legal id") String debtorLegalId,

	@Schema(description = "Invoice number") String invoiceNumber,

	@Schema(description = "Invoice type", implementation = InvoiceType.class) InvoiceType invoiceType,

	@Schema(description = "Attachment", requiredMode = REQUIRED) InvoicePdf attachment) {}
