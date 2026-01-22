package se.sundsvall.invoicecache.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.sundsvall.invoicecache.Constant.RAINDANCE_ISSUER_LEGAL_ID;
import static se.sundsvall.invoicecache.TestObjectFactory.generateInvoiceEntity;
import static se.sundsvall.invoicecache.TestObjectFactory.generatePdfEntity;

import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.zalando.problem.Problem;
import se.sundsvall.invoicecache.api.model.InvoicePdf;
import se.sundsvall.invoicecache.api.model.InvoicePdfFilterRequest;
import se.sundsvall.invoicecache.api.model.InvoicePdfRequest;
import se.sundsvall.invoicecache.integration.db.InvoiceRepository;
import se.sundsvall.invoicecache.integration.db.PdfRepository;
import se.sundsvall.invoicecache.integration.db.entity.PdfEntity;
import se.sundsvall.invoicecache.integration.db.specifications.InvoicePdfSpecifications;
import se.sundsvall.invoicecache.integration.raindance.samba.RaindanceSambaIntegration;
import se.sundsvall.invoicecache.integration.storage.StorageSambaIntegration;
import se.sundsvall.invoicecache.service.mapper.PdfMapper;

@ExtendWith(MockitoExtension.class)
class InvoicePdfServiceTest {

	@Spy
	private InvoicePdfSpecifications specificationsSpy;

	@Mock
	private StorageSambaIntegration storageSambaIntegrationMock;

	@Mock
	private RaindanceSambaIntegration raindanceSambaIntegrationMock;

	@Mock
	private PdfRepository pdfRepositoryMock;

	@Mock
	private InvoiceRepository invoiceRepositoryMock;

	@Mock(answer = Answers.CALLS_REAL_METHODS)
	private PdfMapper pdfMapperMock;

	@InjectMocks
	private InvoicePdfService invoicePdfService;

	@AfterEach
	void tearDown() {
		verifyNoMoreInteractions(storageSambaIntegrationMock, raindanceSambaIntegrationMock, pdfRepositoryMock, invoiceRepositoryMock, specificationsSpy, pdfMapperMock);
	}

	@Test
	void getInvoicePdfByInvoiceNumber_truncated() {
		final var issuerLegalId = "someIssuerLegalId";
		final var invoiceNumber = "someInvoiceNumber";
		final var municipalityId = "2281";
		final var request = new InvoicePdfFilterRequest();
		final var pdfEntity = generatePdfEntity();
		pdfEntity.setTruncatedAt(OffsetDateTime.MIN);
		final var someInputStreamBytes = "someInputStreamBytes".getBytes();
		final Page<PdfEntity> page = new PageImpl<>(List.of(pdfEntity));

		when(pdfRepositoryMock.findAll(Mockito.<Specification<PdfEntity>>any(), Mockito.<Pageable>any())).thenReturn(page);
		when(storageSambaIntegrationMock.readFile(pdfEntity.getFileHash())).thenReturn(someInputStreamBytes);

		final var result = invoicePdfService.getInvoicePdfByInvoiceNumber(issuerLegalId, invoiceNumber, request, municipalityId);

		assertThat(result).isNotNull();
		assertThat(result.content()).isEqualTo(Base64.getEncoder().encodeToString(someInputStreamBytes));
		verify(specificationsSpy).createInvoicesSpecification(request, invoiceNumber, issuerLegalId, municipalityId);
		verify(pdfRepositoryMock).findAll(Mockito.<Specification<PdfEntity>>any(), Mockito.<Pageable>any());
		verify(storageSambaIntegrationMock).readFile(pdfEntity.getFileHash());
		verify(pdfMapperMock).mapToResponse(pdfEntity, someInputStreamBytes);
	}

	@Test
	void getInvoicePdfByInvoiceNumber_notTruncated() {
		final var issuerLegalId = "someIssuerLegalId";
		final var invoiceNumber = "someInvoiceNumber";
		final var municipalityId = "2281";
		final var request = new InvoicePdfFilterRequest();
		final var pdfEntity = generatePdfEntity();
		final Page<PdfEntity> page = new PageImpl<>(List.of(pdfEntity));

		when(pdfRepositoryMock.findAll(Mockito.<Specification<PdfEntity>>any(), Mockito.<Pageable>any())).thenReturn(page);

		final var result = invoicePdfService.getInvoicePdfByInvoiceNumber(issuerLegalId, invoiceNumber, request, municipalityId);

		assertThat(result).isNotNull();
		assertThat(result.content()).isEqualTo(Base64.getEncoder().encodeToString("blobMe".getBytes()));
		verify(specificationsSpy).createInvoicesSpecification(request, invoiceNumber, issuerLegalId, municipalityId);
		verify(pdfRepositoryMock).findAll(Mockito.<Specification<PdfEntity>>any(), Mockito.<Pageable>any());
		verify(pdfMapperMock).mapToResponse(pdfEntity);
	}

	@Test
	void getInvoicePdfByInvoiceNumberByFilename() {
		// Arrange
		final var issuerLegalId = "someIssuerLegalId";
		final var invoiceNumber = "someInvoiceNumber";
		final var fileName = "someFileName";
		final var municipalityId = "2281";
		final var request = new InvoicePdfFilterRequest();
		final var pdfEntity = generatePdfEntity();
		final Page<PdfEntity> page = new PageImpl<>(List.of(pdfEntity));

		when(pdfRepositoryMock.findAll(Mockito.<Specification<PdfEntity>>any(), Mockito.<Pageable>any())).thenReturn(page);

		// Act
		final var invoicePdf = invoicePdfService.getInvoicePdfByInvoiceNumber(issuerLegalId, invoiceNumber, request, municipalityId);

		// Assert
		assertThat(invoicePdf.name()).isEqualTo(fileName);
		assertThat(invoicePdf.content()).isEqualTo(Base64.getEncoder().encodeToString("blobMe".getBytes()));

		verify(pdfRepositoryMock).findAll(Mockito.<Specification<PdfEntity>>any(), Mockito.<Pageable>any());
		verify(specificationsSpy).createInvoicesSpecification(request, invoiceNumber, issuerLegalId, municipalityId);
		verify(pdfMapperMock).mapToResponse(pdfEntity);
		verifyNoMoreInteractions(pdfRepositoryMock, specificationsSpy);
	}

	@Test
	void getInvoicePdfByInvoiceNumber_throwsExceptionByFilename() {
		// Arrange
		final var request = new InvoicePdfFilterRequest();
		final var issuerLegalId = "someIssuerLegalId";
		final var invoiceNumber = "someInvoiceNumber";
		final var municipalityId = "2281";
		final var exception = new RuntimeException();

		when(pdfRepositoryMock.findAll(Mockito.<Specification<PdfEntity>>any(), Mockito.<Pageable>any())).thenThrow(exception);

		// Act & Assert
		assertThatExceptionOfType(RuntimeException.class).isThrownBy(() -> invoicePdfService.getInvoicePdfByInvoiceNumber(
			issuerLegalId, invoiceNumber, request, municipalityId));

		verify(pdfRepositoryMock).findAll(Mockito.<Specification<PdfEntity>>any(), Mockito.<Pageable>any());
		verify(specificationsSpy).createInvoicesSpecification(request, invoiceNumber, issuerLegalId, municipalityId);
		verifyNoMoreInteractions(pdfRepositoryMock, specificationsSpy);
	}

	@Test
	void test_createOrUpdateInvoiceWhenInvoiceDoesNotExist() {
		// Arrange
		final var municipalityId = "2281";
		final var filename = "someFilename";
		final var pdfEntity = PdfEntity.builder().withFilename(filename).build();

		final var request = InvoicePdfRequest.builder()
			.withInvoiceNumber("someInvoiceNumber")
			.withInvoiceId("someInvoiceId")
			.withAttachment(InvoicePdf.builder()
				.withContent("someContent")
				.build())
			.build();

		when(pdfRepositoryMock.findByInvoiceNumberAndInvoiceIdAndMunicipalityId(request.invoiceNumber(), request.invoiceId(), municipalityId)).thenReturn(Optional.empty());
		when(pdfRepositoryMock.save(any(PdfEntity.class))).thenReturn(pdfEntity);

		// Act
		final var result = invoicePdfService.createOrUpdateInvoice(request, municipalityId);

		// Assert
		assertThat(result).isEqualTo(filename);

		verify(pdfRepositoryMock).findByInvoiceNumberAndInvoiceIdAndMunicipalityId(request.invoiceNumber(), request.invoiceId(), municipalityId);
		verify(pdfRepositoryMock).save(any(PdfEntity.class));
		verify(pdfMapperMock).mapToEntity(request, municipalityId);
		verifyNoInteractions(specificationsSpy);
	}

	@Test
	void getRaindanceInvoicePdf() {
		final var invoiceNumber = "someInvoiceNumber";
		final var municipalityId = "2281";
		final var invoiceEntity = generateInvoiceEntity();
		final var invoicePdf = InvoicePdf.builder()
			.withName(invoiceEntity.getFileName())
			.withContent(Base64.getEncoder().encodeToString("someContent".getBytes()))
			.build();

		when(invoiceRepositoryMock.findFirstByInvoiceNumberAndMunicipalityId(invoiceNumber, municipalityId))
			.thenReturn(Optional.of(invoiceEntity));
		when(raindanceSambaIntegrationMock.fetchInvoiceByFilename(invoiceEntity.getFileName()))
			.thenReturn(invoicePdf);

		final var result = invoicePdfService.getRaindanceInvoicePdf(invoiceNumber, municipalityId);

		assertThat(result).isNotNull().satisfies(invoicePdf1 -> {
			assertThat(invoicePdf1.name()).isEqualTo(invoiceEntity.getFileName());
			assertThat(invoicePdf1.content()).isEqualTo(Base64.getEncoder().encodeToString("someContent".getBytes()));
		});

		verify(invoiceRepositoryMock).findFirstByInvoiceNumberAndMunicipalityId(invoiceNumber, municipalityId);
		verify(raindanceSambaIntegrationMock).fetchInvoiceByFilename(invoiceEntity.getFileName());
	}

	@Test
	void getRaindanceInvoicePdf_fallbackToPdfRepository() {
		final var invoiceNumber = "someInvoiceNumber";
		final var municipalityId = "2281";
		final var invoiceEntity = generateInvoiceEntity();
		final var pdfEntity = generatePdfEntity();

		when(invoiceRepositoryMock.findFirstByInvoiceNumberAndMunicipalityId(invoiceNumber, municipalityId))
			.thenReturn(Optional.of(invoiceEntity));
		when(raindanceSambaIntegrationMock.fetchInvoiceByFilename(invoiceEntity.getFileName()))
			.thenThrow(new RuntimeException("Samba connection failed"));
		when(pdfRepositoryMock.findByInvoiceIdAndInvoiceIssuerLegalIdAndMunicipalityId(invoiceNumber, RAINDANCE_ISSUER_LEGAL_ID, municipalityId))
			.thenReturn(Optional.of(pdfEntity));

		final var result = invoicePdfService.getRaindanceInvoicePdf(invoiceNumber, municipalityId);

		assertThat(result).isNotNull();
		assertThat(result.content()).isEqualTo(Base64.getEncoder().encodeToString("blobMe".getBytes()));

		verify(invoiceRepositoryMock).findFirstByInvoiceNumberAndMunicipalityId(invoiceNumber, municipalityId);
		verify(raindanceSambaIntegrationMock).fetchInvoiceByFilename(invoiceEntity.getFileName());
		verify(pdfRepositoryMock).findByInvoiceIdAndInvoiceIssuerLegalIdAndMunicipalityId(invoiceNumber, RAINDANCE_ISSUER_LEGAL_ID, municipalityId);
		verify(pdfMapperMock).mapToResponse(pdfEntity);
	}

	@Test
	void getRaindanceInvoicePdf_fallbackFails() {
		final var invoiceNumber = "someInvoiceNumber";
		final var municipalityId = "2281";
		final var invoiceEntity = generateInvoiceEntity();

		when(invoiceRepositoryMock.findFirstByInvoiceNumberAndMunicipalityId(invoiceNumber, municipalityId))
			.thenReturn(Optional.of(invoiceEntity));
		when(raindanceSambaIntegrationMock.fetchInvoiceByFilename(invoiceEntity.getFileName()))
			.thenThrow(new RuntimeException("Samba connection failed"));
		when(pdfRepositoryMock.findByInvoiceIdAndInvoiceIssuerLegalIdAndMunicipalityId(invoiceNumber, RAINDANCE_ISSUER_LEGAL_ID, municipalityId))
			.thenReturn(Optional.empty());

		assertThatThrownBy(() -> invoicePdfService.getRaindanceInvoicePdf(invoiceNumber, municipalityId))
			.isInstanceOf(Problem.class)
			.hasMessageContaining("Not Found: PDF not found for invoiceNumber: " + invoiceNumber);

		verify(invoiceRepositoryMock).findFirstByInvoiceNumberAndMunicipalityId(invoiceNumber, municipalityId);
		verify(raindanceSambaIntegrationMock).fetchInvoiceByFilename(invoiceEntity.getFileName());
		verify(pdfRepositoryMock).findByInvoiceIdAndInvoiceIssuerLegalIdAndMunicipalityId(invoiceNumber, RAINDANCE_ISSUER_LEGAL_ID, municipalityId);
	}

	@Test
	void getRaindanceInvoicePdf_invoiceNotFound() {
		final var invoiceNumber = "someInvoiceNumber";
		final var municipalityId = "2281";

		when(invoiceRepositoryMock.findFirstByInvoiceNumberAndMunicipalityId(invoiceNumber, municipalityId))
			.thenReturn(Optional.empty());

		assertThatThrownBy(() -> invoicePdfService.getRaindanceInvoicePdf(invoiceNumber, municipalityId))
			.isInstanceOf(Problem.class)
			.hasMessageContaining("Not Found: No invoice with invoice number '%s' was found".formatted(invoiceNumber));

		verify(invoiceRepositoryMock).findFirstByInvoiceNumberAndMunicipalityId(invoiceNumber, municipalityId);
		verify(raindanceSambaIntegrationMock, never()).fetchInvoiceByFilename(any());
	}

}
