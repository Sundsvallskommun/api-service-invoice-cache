package se.sundsvall.invoicecache.api.model;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import se.sundsvall.dept44.common.validators.annotation.ValidBase64;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder(setterPrefix = "with")
@Schema(description = "Model for the InvoicePdf")
public record InvoicePdf(

	@ValidBase64 @Schema(description = "The file content as a BASE64-encoded string", example = "aGVsbG8gd29ybGQK", requiredMode = REQUIRED) String content,

	@Schema(description = "The filename", example = "test.pdf", requiredMode = REQUIRED) String name) {}
