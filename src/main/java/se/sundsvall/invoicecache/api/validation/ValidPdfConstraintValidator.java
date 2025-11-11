package se.sundsvall.invoicecache.api.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.io.ByteArrayInputStream;
import java.util.Base64;
import org.apache.tika.Tika;
import se.sundsvall.invoicecache.api.model.InvoicePdf;

public class ValidPdfConstraintValidator implements ConstraintValidator<ValidPdf, InvoicePdf> {

	private final Tika tika = new Tika();

	@Override
	public boolean isValid(final InvoicePdf invoicePdf, final ConstraintValidatorContext context) {
		try {
			var decodedContent = Base64.getDecoder().decode(invoicePdf.content());
			var inputStream = new ByteArrayInputStream(decodedContent);
			var detectedType = tika.detect(inputStream, invoicePdf.name());
			return "application/pdf".equals(detectedType);
		} catch (Exception e) {
			return false;
		}
	}
}
