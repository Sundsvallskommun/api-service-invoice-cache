package se.sundsvall.invoicecache.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import se.sundsvall.dept44.common.validators.annotation.ValidBase64;
import se.sundsvall.invoicecache.api.validation.ValidPdf;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Builder(setterPrefix = "with")
@Schema(description = "Model for the InvoicePdf")
@ValidPdf
public record InvoicePdf(

	@ValidBase64 @Schema(description = "The file content as a BASE64-encoded string", examples = "aGVsbG8gd29ybGQK", requiredMode = REQUIRED) String content,

	@Schema(description = "The filename", examples = "test.pdf", requiredMode = REQUIRED) String name) {
}
