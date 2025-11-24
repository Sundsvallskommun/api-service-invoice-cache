package se.sundsvall.invoicecache.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.sundsvall.invoicecache.TestObjectFactory.generateInvoiceFilterRequest;
import static se.sundsvall.invoicecache.TestObjectFactory.generateInvoicePdf;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.zalando.problem.ThrowableProblem;
import se.sundsvall.invoicecache.api.model.Invoice;
import se.sundsvall.invoicecache.api.model.InvoiceFilterRequest;
import se.sundsvall.invoicecache.api.model.InvoicePdfFilterRequest;
import se.sundsvall.invoicecache.api.model.InvoicePdfRequest;
import se.sundsvall.invoicecache.api.model.InvoicesResponse;
import se.sundsvall.invoicecache.service.InvoiceCacheService;
import se.sundsvall.invoicecache.service.InvoicePdfService;

@ExtendWith(MockitoExtension.class)
class InvoiceCacheResourceTest {

	@Mock
	private InvoiceCacheService mockService;

	@Mock
	private InvoicePdfService mockPdfService;

	@InjectMocks
	private InvoiceCacheResource resource;

	@Test
	void testGetInvoicesSuccessfulRequest_shouldReturnResponse() {

		// Arrange
		final var municipalityId = "2281";
		final var invoicesResponse = new InvoicesResponse();
		invoicesResponse.addInvoice(new Invoice()); // Fake that we got an invoice
		final var request = generateInvoiceFilterRequest();

		when(mockService.getInvoices(request, municipalityId)).thenReturn(invoicesResponse);

		// Act
		final var response = resource.getInvoices(municipalityId, request);

		// Assert
		verify(mockService, times(1)).getInvoices(request, municipalityId);
		verifyNoInteractions(mockPdfService);

		assertThat(response).isNotNull();
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isNotNull();
	}

	@Test
	void testGetInvoicesFailedRequestValidation_shouldThrowException() {

		// Arrange
		final var municipalityId = "2281";
		final var request = new InvoiceFilterRequest();

		// Act
		assertThatExceptionOfType(ThrowableProblem.class)
			.isThrownBy(() -> resource.getInvoices(municipalityId, request))
			.withMessage("One of partyIds, invoiceNumbers or ocrNumber needs to be set.");

		// Assert
		verify(mockService, times(0)).getInvoices(any(InvoiceFilterRequest.class), eq(municipalityId));
	}

	@Test
	void testImportInvoice() {

		// Arrange
		final var municipalityId = "2281";
		final var request = InvoicePdfRequest.builder().build();
		when(mockPdfService.createOrUpdateInvoice(request, municipalityId)).thenReturn("someFilename");

		// Act
		final var response = resource.importInvoice(municipalityId, request);

		// Assert
		assertThat(response).isNotNull();
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		assertThat(response.getHeaders()).containsKey(HttpHeaders.LOCATION);

		verify(mockPdfService, times(1)).createOrUpdateInvoice(request, municipalityId);
	}

	@Test
	void testGetPdfWithSpecificationSuccessfulRequest_shouldReturnResponse() {

		// Arrange
		final var municipalityId = "2281";
		final var request = new InvoicePdfFilterRequest();
		final var invoiceNumber = "invoicenumber";
		final var issuerLegalId = "issuerlegalid";

		when(mockPdfService.getInvoicePdfByInvoiceNumber(issuerLegalId, invoiceNumber, request, municipalityId)).thenReturn(generateInvoicePdf());

		// Act

		final var invoicePdfResponse = resource.getInvoicePdf(municipalityId, issuerLegalId, invoiceNumber, request);

		// Assert
		verify(mockPdfService, times(1))
			.getInvoicePdfByInvoiceNumber(issuerLegalId, invoiceNumber, request, municipalityId);
		verifyNoInteractions(mockService);
		verifyNoMoreInteractions(mockPdfService);

		assertThat(invoicePdfResponse).isNotNull();
		assertThat(invoicePdfResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(invoicePdfResponse.getBody()).isNotNull();

		final var invoicePdf = invoicePdfResponse.getBody();
		assertThat(invoicePdf.name()).isEqualTo("someName");
		assertThat(invoicePdf.content()).isEqualTo("someContent");

	}

}
