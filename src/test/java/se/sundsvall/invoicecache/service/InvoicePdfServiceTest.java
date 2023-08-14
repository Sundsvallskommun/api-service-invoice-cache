package se.sundsvall.invoicecache.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.sundsvall.invoicecache.TestObjectFactory.generatePdfEntity;

import java.util.Base64;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import se.sundsvall.invoicecache.api.model.InvoicePdf;
import se.sundsvall.invoicecache.api.model.InvoicePdfFilterRequest;
import se.sundsvall.invoicecache.api.model.InvoicePdfRequest;
import se.sundsvall.invoicecache.api.model.InvoiceType;
import se.sundsvall.invoicecache.integration.db.PdfEntityRepository;
import se.sundsvall.invoicecache.integration.db.entity.PdfEntity;
import se.sundsvall.invoicecache.integration.db.specifications.InvoicePdfSpecifications;

@ExtendWith(MockitoExtension.class)
class InvoicePdfServiceTest {

	@Spy
	private InvoicePdfSpecifications specifications;

	@Mock
	private PdfEntityRepository mockRepository;

	@InjectMocks
	private InvoicePdfService pdfService;

	@Test
    void getInvoicePdf() {
        when(mockRepository.findByFilename(any(String.class))).thenReturn(Optional.of(generatePdfEntity()));

        final var invoicePdf = pdfService.getInvoicePdf("someFileName");

        assertThat(invoicePdf.name()).isEqualTo("someFileName");
        assertThat(invoicePdf.content()).isEqualTo(Base64.getEncoder().encodeToString("blobMe".getBytes()));

        verify(mockRepository, times(1)).findByFilename(any(String.class));
        verifyNoInteractions(specifications);
        verifyNoMoreInteractions(mockRepository);
    }

	@Test
    void getInvoicePdf_throwsException() {
        when(mockRepository.findByFilename(any(String.class))).thenThrow(new RuntimeException());

        assertThatExceptionOfType(RuntimeException.class).isThrownBy(() -> pdfService.getInvoicePdf("someFileName"));
        verify(mockRepository, times(1)).findByFilename(any(String.class));
        verifyNoInteractions(specifications);
        verifyNoMoreInteractions(mockRepository);
    }

	@Test
    void getInvoicePdfByInvoiceNumber() {
        when(mockRepository.findAll(Mockito.<Specification<PdfEntity>>any())).thenReturn(List.of(generatePdfEntity()));

        final var invoicePdf = pdfService
            .getInvoicePdfByInvoiceNumber("someIssuerLegalId", "someInvoiceNumber", new InvoicePdfFilterRequest());

        assertThat(invoicePdf.name()).isEqualTo("someFileName");
        assertThat(invoicePdf.content()).isEqualTo(Base64.getEncoder().encodeToString("blobMe".getBytes()));

        verify(mockRepository, times(1)).findAll(Mockito.<Specification<PdfEntity>>any());
        verify(specifications, times(1))
            .createInvoicesSpecification(any(InvoicePdfFilterRequest.class), any(String.class), any(String.class));
        verifyNoMoreInteractions(mockRepository);
        verifyNoMoreInteractions(specifications);
    }

	@Test
	void getInvoicePdfByInvoiceNumber_throwsException() {
		final var request = new InvoicePdfFilterRequest();
		when(mockRepository.findAll(Mockito.<Specification<PdfEntity>>any())).thenThrow(new RuntimeException());

		assertThatExceptionOfType(RuntimeException.class).isThrownBy(() -> pdfService.getInvoicePdfByInvoiceNumber(
			"someIssuerLegalId", "someInvoiceNumber", request));

		verify(mockRepository, times(1)).findAll(Mockito.<Specification<PdfEntity>>any());
		verify(specifications, times(1))
			.createInvoicesSpecification(any(InvoicePdfFilterRequest.class), any(String.class), any(String.class));
		verifyNoMoreInteractions(mockRepository);
		verifyNoMoreInteractions(specifications);
	}

	@Test
    void test_createOrUpdateInvoiceWhenInvoiceDoesNotExist() {
        when(mockRepository.save(any(PdfEntity.class)))
            .thenReturn(PdfEntity.builder().withFilename("someFilename").build());

        final var request = InvoicePdfRequest.builder()
            .withAttachment(InvoicePdf.builder()
                .withContent("someContent")
                .build())
            .build();
        final var result = pdfService.createOrUpdateInvoice(request);

        assertThat(result).isEqualTo("someFilename");

        verify(mockRepository, times(1)).save(any(PdfEntity.class));
        verifyNoInteractions(specifications);
    }

	@Test
	void test_mapToEntity() {
		final var request = InvoicePdfRequest.builder()
			.withIssuerLegalId("someIssuerLegalId")
			.withDebtorLegalId("someDebtorLegalId")
			.withInvoiceNumber("someInvoiceNumber")
			.withInvoiceType(InvoiceType.SELF_INVOICE)
			.withAttachment(InvoicePdf.builder()
				.withName("someName")
				.withContent("someContent")
				.build())
			.build();

		final var entity = pdfService.mapToEntity(request);

		assertThat(entity.getInvoiceIssuerLegalId()).isEqualTo(request.issuerLegalId());
		assertThat(entity.getInvoiceDebtorLegalId()).isEqualTo(request.debtorLegalId());
		assertThat(entity.getInvoiceNumber()).isEqualTo(request.invoiceNumber());
		assertThat(entity.getInvoiceType()).isEqualTo(request.invoiceType());
		assertThat(entity.getFilename()).isEqualTo(request.attachment().name());
		assertThat(entity.getDocument()).isNotNull();
	}

	@Test
	void test_mapOntoExistingEntity() {
		var pdfEntity = generatePdfEntity();
		final var request = InvoicePdfRequest.builder()
			.withIssuerLegalId("someIssuerLegalId")
			.withDebtorLegalId("someDebtorLegalId")
			.withInvoiceNumber("someInvoiceNumber")
			.withInvoiceType(InvoiceType.SELF_INVOICE)
			.withAttachment(InvoicePdf.builder()
				.withName("someName.pdf")
				.withContent("someContent")
				.build())
			.build();

		pdfEntity = pdfService.mapOntoExistingEntity(pdfEntity, request);

		assertThat(pdfEntity.getInvoiceIssuerLegalId()).isEqualTo(request.issuerLegalId());
		assertThat(pdfEntity.getInvoiceDebtorLegalId()).isEqualTo(request.debtorLegalId());
		assertThat(pdfEntity.getInvoiceNumber()).isEqualTo(request.invoiceNumber());
		assertThat(pdfEntity.getInvoiceType()).isEqualTo(request.invoiceType());
		assertThat(pdfEntity.getFilename()).isEqualTo(request.attachment().name());
		assertThat(pdfEntity.getDocument()).isNotNull();
	}
}
