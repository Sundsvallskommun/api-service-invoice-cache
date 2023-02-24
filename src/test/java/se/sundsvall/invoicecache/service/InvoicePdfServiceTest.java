package se.sundsvall.invoicecache.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.sundsvall.invoicecache.TestObjectFactory.generatePdfEntity;

import java.util.Base64;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import se.sundsvall.invoicecache.api.model.InvoicePdf;
import se.sundsvall.invoicecache.api.model.InvoicePdfRequest;
import se.sundsvall.invoicecache.api.model.InvoiceType;
import se.sundsvall.invoicecache.integration.db.PdfEntityRepository;
import se.sundsvall.invoicecache.integration.db.entity.PdfEntity;

@ExtendWith(MockitoExtension.class)
class InvoicePdfServiceTest {

    @Mock
    PdfEntityRepository mockRepository;

    @InjectMocks
    InvoicePdfService pdfService;

    @Test
    void getInvoicePdf() {
        when(mockRepository.findByFilename(any(String.class))).thenReturn(Optional.of(generatePdfEntity()));

        var invoicePdf = pdfService.getInvoicePdf("someFileName");

        assertThat(invoicePdf.name()).isEqualTo("someFileName");
        assertThat(invoicePdf.content()).isEqualTo(Base64.getEncoder().encodeToString("blobMe".getBytes()));
    }

    @Test
    void getInvoicePdf_throwsException() {
        when(mockRepository.findByFilename(any(String.class))).thenThrow(new RuntimeException());

        assertThatExceptionOfType(RuntimeException.class).isThrownBy(() -> pdfService.getInvoicePdf("someFileName"));
    }

    @Test
    void test_createOrUpdateInvoiceWhenInvoiceDoesNotExist() {
        when(mockRepository.save(any(PdfEntity.class)))
            .thenReturn(PdfEntity.builder().withFilename("someFilename").build());

        var request = InvoicePdfRequest.builder()
            .withAttachment(InvoicePdf.builder()
                .withContent("someContent")
                .build())
            .build();
        var result = pdfService.createOrUpdateInvoice(request);

        assertThat(result).isEqualTo("someFilename");

        verify(mockRepository, times(1)).save(any(PdfEntity.class));
    }

    @Test
    void test_mapToEntity() {
        var request = InvoicePdfRequest.builder()
            .withIssuerLegalId("someIssuerLegalId")
            .withDebtorLegalId("someDebtorLegalId")
            .withInvoiceNumber("someInvoiceNumber")
            .withInvoiceName("someInvoiceName")
            .withInvoiceType(InvoiceType.SELF_INVOICE)
            .withAttachment(InvoicePdf.builder()
                .withName("someName")
                .withContent("someContent")
                .build())
            .build();

        var entity = pdfService.mapToEntity(request);

        assertThat(entity.getInvoiceIssuerLegalId()).isEqualTo(request.issuerLegalId());
        assertThat(entity.getInvoiceDebtorLegalId()).isEqualTo(request.debtorLegalId());
        assertThat(entity.getInvoiceNumber()).isEqualTo(request.invoiceNumber());
        assertThat(entity.getInvoiceName()).isEqualTo(request.invoiceName());
        assertThat(entity.getInvoiceType()).isEqualTo(request.invoiceType());
        assertThat(entity.getFilename()).isEqualTo(request.attachment().name());
        assertThat(entity.getDocument()).isNotNull();
    }

    @Test
    void test_mapOntoExistingEntity() {
        var pdfEntity = generatePdfEntity();
        var request = InvoicePdfRequest.builder()
            .withIssuerLegalId("someIssuerLegalId")
            .withDebtorLegalId("someDebtorLegalId")
            .withInvoiceNumber("someInvoiceNumber")
            .withInvoiceName("someInvoiceName")
            .withInvoiceType(InvoiceType.SELF_INVOICE)
            .withAttachment(InvoicePdf.builder()
                .withName("someName")
                .withContent("someContent")
                .build())
            .build();

        pdfEntity = pdfService.mapOntoExistingEntity(pdfEntity, request);

        assertThat(pdfEntity.getInvoiceIssuerLegalId()).isEqualTo(request.issuerLegalId());
        assertThat(pdfEntity.getInvoiceDebtorLegalId()).isEqualTo(request.debtorLegalId());
        assertThat(pdfEntity.getInvoiceNumber()).isEqualTo(request.invoiceNumber());
        assertThat(pdfEntity.getInvoiceName()).isEqualTo(request.invoiceName());
        assertThat(pdfEntity.getInvoiceType()).isEqualTo(request.invoiceType());
        assertThat(pdfEntity.getFilename()).isEqualTo(request.attachment().name());
        assertThat(pdfEntity.getDocument()).isNotNull();
    }
}