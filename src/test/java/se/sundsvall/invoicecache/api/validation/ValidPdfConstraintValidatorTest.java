package se.sundsvall.invoicecache.api.validation;

import jakarta.validation.ConstraintValidatorContext;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import se.sundsvall.invoicecache.api.model.InvoicePdf;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class ValidPdfConstraintValidatorTest {

	private static final String PATH = "mimetype_files/";
	private static final String IMG_FILE_NAME = "image.jpg";
	private static final String DOC_FILE_NAME = "document.doc";
	private static final String DOCX_FILE_NAME = "document.docx";
	private static final String PDF_FILE_NAME = "document.pdf";
	private static final String TXT_FILE_NAME = "document.txt";

	@Mock
	private ConstraintValidatorContext constraintValidatorContext;

	@InjectMocks
	private ValidPdfConstraintValidator validator;

	@Test
	void validContentType() throws IOException {
		var content = getStream(PATH + PDF_FILE_NAME).readAllBytes();

		var base64Content = Base64.getEncoder().encodeToString(content);
		var invoicePdf = new InvoicePdf(base64Content, PDF_FILE_NAME);

		assertThat(validator.isValid(invoicePdf, constraintValidatorContext)).isTrue();
	}

	@ParameterizedTest
	@ValueSource(strings = {
		IMG_FILE_NAME, DOC_FILE_NAME, DOCX_FILE_NAME, TXT_FILE_NAME
	})
	void invalidContentType(final String fileName) throws IOException {
		var content = getStream(PATH + fileName).readAllBytes();
		var base64Content = Base64.getEncoder().encodeToString(content);
		var invoicePdf = new InvoicePdf(base64Content, fileName);

		assertThat(validator.isValid(invoicePdf, constraintValidatorContext)).isFalse();
	}

	@Test
	void isValid_throws() {
		var invoicePdf = new InvoicePdf(null, "pdf-invalid.name");
		assertThat(validator.isValid(invoicePdf, constraintValidatorContext)).isFalse();
	}

	private InputStream getStream(String path) throws IOException {
		return new ClassPathResource(path).getInputStream();
	}

}
