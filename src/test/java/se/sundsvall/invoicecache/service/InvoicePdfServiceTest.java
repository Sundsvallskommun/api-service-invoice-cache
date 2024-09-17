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
		// Arrange
		final var filename = "someFileName";
		final var pdfEntity = generatePdfEntity();
		when(mockRepository.findByFilename(filename)).thenReturn(Optional.of(pdfEntity));

		// Act

		final var invoicePdf = pdfService.getInvoicePdf(filename);

		// Assert
		assertThat(invoicePdf.name()).isEqualTo("someFileName");
		assertThat(invoicePdf.content()).isEqualTo(Base64.getEncoder().encodeToString("blobMe".getBytes()));
		verify(mockRepository, times(1)).findByFilename(filename);
		verifyNoInteractions(specifications);
		verifyNoMoreInteractions(mockRepository);
	}

	@Test
	void getInvoicePdf_throwsException() {

		// Arrange
		final var filename = "someFileName";

		when(mockRepository.findByFilename(filename)).thenThrow(new RuntimeException());

		// Act & Assert
		assertThatExceptionOfType(RuntimeException.class).isThrownBy(() -> pdfService.getInvoicePdf(filename));
		verify(mockRepository, times(1)).findByFilename(filename);
		verifyNoInteractions(specifications);
		verifyNoMoreInteractions(mockRepository);
	}

	@Test
	void getInvoicePdfByInvoiceNumber() {

		// Arrange
		final var issuerLegalId = "someIssuerLegalId";
		final var invoiceNumber = "someInvoiceNumber";
		final var fileName = "someFileName";
		final var request = new InvoicePdfFilterRequest();

		when(mockRepository.findAll(Mockito.<Specification<PdfEntity>>any())).thenReturn(List.of(generatePdfEntity()));

		// Act
		final var invoicePdf = pdfService
			.getInvoicePdfByInvoiceNumber(issuerLegalId, invoiceNumber, request);

		// Assert
		assertThat(invoicePdf.name()).isEqualTo(fileName);
		assertThat(invoicePdf.content()).isEqualTo(Base64.getEncoder().encodeToString("blobMe".getBytes()));

		verify(mockRepository, times(1)).findAll(Mockito.<Specification<PdfEntity>>any());
		verify(specifications, times(1))
			.createInvoicesSpecification(request, invoiceNumber, issuerLegalId);
		verifyNoMoreInteractions(mockRepository);
		verifyNoMoreInteractions(specifications);
	}

	@Test
	void getInvoicePdfByInvoiceNumber_throwsException() {

		// Arrange
		final var request = new InvoicePdfFilterRequest();
		final var issuerLegalId = "someIssuerLegalId";
		final var invoiceNumber = "someInvoiceNumber";
		final var exception = new RuntimeException();

		when(mockRepository.findAll(Mockito.<Specification<PdfEntity>>any())).thenThrow(exception);

		// Act & Assert
		assertThatExceptionOfType(RuntimeException.class).isThrownBy(() -> pdfService.getInvoicePdfByInvoiceNumber(
			issuerLegalId, invoiceNumber, request));

		verify(mockRepository, times(1)).findAll(Mockito.<Specification<PdfEntity>>any());
		verify(specifications, times(1))
			.createInvoicesSpecification(request, invoiceNumber, issuerLegalId);
		verifyNoMoreInteractions(mockRepository);
		verifyNoMoreInteractions(specifications);
	}

	@Test
	void test_createOrUpdateInvoiceWhenInvoiceDoesNotExist() {

		// Arrange
		final var filename = "someFilename";
		final var pdfEntity = PdfEntity.builder().withFilename(filename).build();
		when(mockRepository.save(any(PdfEntity.class)))
			.thenReturn(pdfEntity);

		final var request = InvoicePdfRequest.builder()
			.withAttachment(InvoicePdf.builder()
				.withContent("someContent")
				.build())
			.build();

		// Act
		final var result = pdfService.createOrUpdateInvoice(request);

		// Assert
		assertThat(result).isEqualTo(filename);

		verify(mockRepository, times(1)).save(any(PdfEntity.class));
		verifyNoInteractions(specifications);
	}

	@Test
	void test_mapToEntity() {

		// Arrange
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

		// Act
		final var entity = pdfService.mapToEntity(request);

		// Assert
		assertThat(entity.getInvoiceIssuerLegalId()).isEqualTo(request.issuerLegalId());
		assertThat(entity.getInvoiceDebtorLegalId()).isEqualTo(request.debtorLegalId());
		assertThat(entity.getInvoiceNumber()).isEqualTo(request.invoiceNumber());
		assertThat(entity.getInvoiceType()).isEqualTo(request.invoiceType());
		assertThat(entity.getFilename()).isEqualTo(request.attachment().name());
		assertThat(entity.getDocument()).isNotNull();
	}

	@Test
	void test_mapOntoExistingEntity() {

		// Arrange
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

		// Act
		pdfEntity = pdfService.mapOntoExistingEntity(pdfEntity, request);

		// Assert
		assertThat(pdfEntity.getInvoiceIssuerLegalId()).isEqualTo(request.issuerLegalId());
		assertThat(pdfEntity.getInvoiceDebtorLegalId()).isEqualTo(request.debtorLegalId());
		assertThat(pdfEntity.getInvoiceNumber()).isEqualTo(request.invoiceNumber());
		assertThat(pdfEntity.getInvoiceType()).isEqualTo(request.invoiceType());
		assertThat(pdfEntity.getFilename()).isEqualTo(request.attachment().name());
		assertThat(pdfEntity.getDocument()).isNotNull();
	}

}
