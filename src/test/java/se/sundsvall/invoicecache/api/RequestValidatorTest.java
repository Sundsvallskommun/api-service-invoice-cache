package se.sundsvall.invoicecache.api;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.zalando.problem.ThrowableProblem;

import se.sundsvall.invoicecache.api.model.InvoiceFilterRequest;

class RequestValidatorTest {

	private final List<String> invoiceNumbers = List.of("1234", "2345");
	private final List<String> legalIds = List.of("5591621234", "5591621235");

	@Test
	void testCheckInvoiceDates_fromIsAfterTo() {
		final var request = new InvoiceFilterRequest();
		request.setInvoiceDateFrom(LocalDate.now().plusDays(1L));
		request.setInvoiceDateTo(LocalDate.now());
		request.setInvoiceNumbers(invoiceNumbers);
		assertThatExceptionOfType(ThrowableProblem.class).isThrownBy(() -> RequestValidator.validateRequest(request))
			.withMessage("To-date is before From-date.");
	}

	@Test
	void testOrganizationNumberFieldIsSet() {
		final var request = new InvoiceFilterRequest();
		request.setPartyIds(legalIds);
		RequestValidator.validateRequest(request);
	}

	@Test
	void testInvoiceNumberFieldIsSet() {
		final var request = new InvoiceFilterRequest();
		request.setInvoiceNumbers(invoiceNumbers);
		RequestValidator.validateRequest(request);
	}

	@Test
	void testOcrNumberFieldIsSet() {
		final var request = new InvoiceFilterRequest();
		request.setOcrNumber("testString");
		RequestValidator.validateRequest(request);
	}

	@Test
	void testAllFieldsAreSet() {
		final var request = new InvoiceFilterRequest();
		request.setOcrNumber("testString");
		request.setInvoiceNumbers(invoiceNumbers);
		request.setPartyIds(legalIds);
		RequestValidator.validateRequest(request);
	}

	@Test
	void testNoFieldIsSet() {
		final var request = new InvoiceFilterRequest();
		assertThatExceptionOfType(ThrowableProblem.class).isThrownBy(() -> RequestValidator.validateRequest(request))
			.withMessage("One of partyIds, invoiceNumbers or ocrNumber needs to be set.");
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"", " "
	})
	void testOnlyOrganizationNumberHasFaultyValues(final String testString) {
		final var request = new InvoiceFilterRequest();
		request.setPartyIds(List.of(testString));

		assertThatExceptionOfType(ThrowableProblem.class).isThrownBy(() -> RequestValidator.validateRequest(request))
			.withMessage("One of partyIds, invoiceNumbers or ocrNumber needs to be set.");
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"", " "
	})
	void testOnlyInvoiceNumberHasFaultyValues(final String testString) {
		final var request = new InvoiceFilterRequest();
		request.setInvoiceNumbers(List.of(testString));

		assertThatExceptionOfType(ThrowableProblem.class).isThrownBy(() -> RequestValidator.validateRequest(request))
			.withMessage("One of partyIds, invoiceNumbers or ocrNumber needs to be set.");
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"", " "
	})
	void testOnlyOcrNumberHasFaultyValues(final String testString) {
		final var request = new InvoiceFilterRequest();
		request.setOcrNumber(testString);

		assertThatExceptionOfType(ThrowableProblem.class).isThrownBy(() -> RequestValidator.validateRequest(request))
			.withMessage("One of partyIds, invoiceNumbers or ocrNumber needs to be set.");
	}
}
