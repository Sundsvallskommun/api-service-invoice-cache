package se.sundsvall.invoicecache.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
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
		final var invoicesResponse = new InvoicesResponse();
		invoicesResponse.addInvoice(new Invoice()); // Fake that we got an invoice

		when(mockService.getInvoices(any(InvoiceFilterRequest.class))).thenReturn(invoicesResponse);

		final var response = resource.getInvoices(generateInvoiceFilterRequest());

		verify(mockService, times(1)).getInvoices(any(InvoiceFilterRequest.class));
		verifyNoInteractions(mockPdfService);

		assertThat(response).isNotNull();
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isNotNull();
	}

	@Test
	void testGetInvoicesFailedRequestValidation_shouldThrowException() {
		final var request = new InvoiceFilterRequest();
		assertThatExceptionOfType(ThrowableProblem.class)
			.isThrownBy(() -> resource.getInvoices(request))
			.withMessage("One of legalIds, invoiceNumbers or ocrNumber needs to be set.");

		verify(mockService, times(0)).getInvoices(any(InvoiceFilterRequest.class));
	}

	@Test
    void testGetPdfSuccessfulRequest_shouldReturnResponse() {
        when(mockPdfService.getInvoicePdf(any(String.class))).thenReturn(generateInvoicePdf());

        final var invoicePdfResponse = resource.getInvoicePdf("fileName");

        verify(mockPdfService, times(1)).getInvoicePdf(any(String.class));
        verifyNoInteractions(mockService);
        verifyNoMoreInteractions(mockPdfService);

        assertThat(invoicePdfResponse).isNotNull();
        assertThat(invoicePdfResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(invoicePdfResponse.getBody()).isNotNull();

        final var invoicePdf = invoicePdfResponse.getBody();
        assertThat(invoicePdf.name()).isEqualTo("someName");
        assertThat(invoicePdf.content()).isEqualTo("someContent");
    }

	@Test
    void testImportInvoice() {
        when(mockPdfService.createOrUpdateInvoice(any(InvoicePdfRequest.class))).thenReturn("someFilename");

        final var response = resource.importInvoice(InvoicePdfRequest.builder().build());

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getHeaders()).containsKey(HttpHeaders.LOCATION);

        verify(mockPdfService, times(1)).createOrUpdateInvoice(any(InvoicePdfRequest.class));
    }

	@Test
    void testGetPdfWithSpecificationSuccessfulRequest_shouldReturnResponse() {
        when(mockPdfService.getInvoicePdfByInvoiceNumber(any(String.class),
            any(String.class), any(InvoicePdfFilterRequest.class))).thenReturn(generateInvoicePdf());

        final var invoicePdfResponse = resource
            .getInvoicePdf("issuerlegalid", "invoicenumber", new InvoicePdfFilterRequest());

        verify(mockPdfService, times(1))
            .getInvoicePdfByInvoiceNumber(any(String.class), any(String.class), any(InvoicePdfFilterRequest.class));
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
