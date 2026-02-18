package se.sundsvall.invoicecache.service.mapper;

import java.util.Base64;
import org.junit.jupiter.api.Test;
import se.sundsvall.invoicecache.api.model.InvoicePdf;
import se.sundsvall.invoicecache.api.model.InvoicePdfRequest;
import se.sundsvall.invoicecache.api.model.InvoiceType;
import se.sundsvall.invoicecache.util.exception.InvoiceCacheException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mockStatic;
import static se.sundsvall.invoicecache.TestObjectFactory.generatePdfEntity;

class PdfMapperTest {

	private final PdfMapper pdfService = new PdfMapper();

	@Test
	void mapToEntity() {
		final var municipalityId = "2281";
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

		final var entity = pdfService.mapToEntity(request, municipalityId);

		assertThat(entity.getInvoiceIssuerLegalId()).isEqualTo(request.issuerLegalId());
		assertThat(entity.getInvoiceDebtorLegalId()).isEqualTo(request.debtorLegalId());
		assertThat(entity.getInvoiceNumber()).isEqualTo(request.invoiceNumber());
		assertThat(entity.getInvoiceType()).isEqualTo(request.invoiceType());
		assertThat(entity.getFilename()).isEqualTo(request.attachment().name());
		assertThat(entity.getDocument()).isNotNull();
	}

	@Test
	void mapToEntity_throws() {
		final var municipalityId = "2281";
		final var request = InvoicePdfRequest.builder()
			.withIssuerLegalId("someIssuerLegalId")
			.withDebtorLegalId("someDebtorLegalId")
			.withInvoiceNumber("someInvoiceNumber")
			.withInvoiceType(InvoiceType.SELF_INVOICE)
			.withAttachment(InvoicePdf.builder()
				.withContent("dGVzdA==")
				.build())
			.build();

		try (var base64 = mockStatic(Base64.class)) {
			base64.when(Base64::getDecoder).thenThrow(RuntimeException.class);
			assertThatThrownBy(() -> pdfService.mapToEntity(request, municipalityId))
				.isInstanceOf(InvoiceCacheException.class)
				.hasMessageContaining("Unable to map PDF data");
		}

	}

	@Test
	void mapOntoExistingEntity() {
		final var pdfEntity = generatePdfEntity();
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

		pdfService.mapOntoExistingEntity(pdfEntity, request);

		assertThat(pdfEntity.getInvoiceIssuerLegalId()).isEqualTo(request.issuerLegalId());
		assertThat(pdfEntity.getInvoiceDebtorLegalId()).isEqualTo(request.debtorLegalId());
		assertThat(pdfEntity.getInvoiceNumber()).isEqualTo(request.invoiceNumber());
		assertThat(pdfEntity.getInvoiceType()).isEqualTo(request.invoiceType());
		assertThat(pdfEntity.getFilename()).isEqualTo(request.attachment().name());
		assertThat(pdfEntity.getDocument()).isNotNull();
	}

	@Test
	void mapOntoExistingEntity_throws() {
		final var pdfEntity = generatePdfEntity();
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

		try (var base64 = mockStatic(Base64.class)) {
			base64.when(Base64::getDecoder).thenThrow(RuntimeException.class);
			assertThatThrownBy(() -> pdfService.mapOntoExistingEntity(pdfEntity, request))
				.isInstanceOf(InvoiceCacheException.class)
				.hasMessageContaining("Unable to map PDF data");
		}

	}

}
