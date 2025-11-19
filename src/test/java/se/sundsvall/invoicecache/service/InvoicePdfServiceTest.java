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

import java.sql.Blob;
import java.sql.SQLException;
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
import org.springframework.data.jpa.domain.Specification;
import se.sundsvall.invoicecache.api.model.InvoicePdf;
import se.sundsvall.invoicecache.api.model.InvoicePdfFilterRequest;
import se.sundsvall.invoicecache.api.model.InvoicePdfRequest;
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

	@Mock(answer = Answers.CALLS_REAL_METHODS)
	private PdfMapper pdfMapperMock;

	@InjectMocks
	private InvoicePdfService invoicePdfService;

	@AfterEach
	void tearDown() {
		verifyNoMoreInteractions(storageSambaIntegrationMock, raindanceSambaIntegrationMock, pdfRepositoryMock);
	}

	@Test
	void getInvoicePdfByFilename_foundInDatabaseNotTruncated() throws SQLException {
		final var filename = "someFileName";
		final var municipalityId = "2281";
		final var blobMock = Mockito.mock(Blob.class);
		final var someBlobBytes = "someBlob".getBytes();
		final var pdfEntity = PdfEntity.builder()
			.withTruncatedAt(null)
			.withDocument(blobMock)
			.build();

		when(blobMock.length()).thenReturn(123L);
		when(blobMock.getBytes(1, 123)).thenReturn(someBlobBytes);
		when(pdfRepositoryMock.findByFilenameAndMunicipalityId(filename, municipalityId)).thenReturn(Optional.of(pdfEntity));

		final var result = invoicePdfService.getInvoicePdfByFilename(filename, municipalityId);

		assertThat(result).isNotNull();
		assertThat(result.content()).isEqualTo(Base64.getEncoder().encodeToString(someBlobBytes));

		verify(pdfRepositoryMock).findByFilenameAndMunicipalityId(filename, municipalityId);
		verify(pdfMapperMock).mapToResponse(pdfEntity);
		verifyNoInteractions(storageSambaIntegrationMock, raindanceSambaIntegrationMock);
	}

	@Test
	void getInvoicePdfByFilename_foundInDatabaseTruncated() {
		final var filename = "someFileName";
		final var municipalityId = "2281";
		final var someInputStreamBytes = "someInputStreamBytes".getBytes();
		final var pdfEntity = PdfEntity.builder()
			.withTruncatedAt(OffsetDateTime.MIN)
			.withFileHash("someFileHash")
			.withDocument(null)
			.build();

		when(pdfRepositoryMock.findByFilenameAndMunicipalityId(filename, municipalityId)).thenReturn(Optional.of(pdfEntity));
		when(storageSambaIntegrationMock.readFile("someFileHash")).thenReturn(someInputStreamBytes);

		final var result = invoicePdfService.getInvoicePdfByFilename(filename, municipalityId);

		assertThat(result).isNotNull();
		assertThat(result.content()).isEqualTo(Base64.getEncoder().encodeToString(someInputStreamBytes));

		verify(pdfRepositoryMock).findByFilenameAndMunicipalityId(filename, municipalityId);
		verify(storageSambaIntegrationMock).readFile("someFileHash");

		verify(pdfMapperMock).mapToResponse(pdfEntity, someInputStreamBytes);
		verifyNoInteractions(raindanceSambaIntegrationMock);
	}

	@Test
	void getInvoicePdfByFilename_notFoundInDatabase() throws SQLException {
		final var filename = "someFileName";
		final var municipalityId = "2281";
		final var blobMock = Mockito.mock(Blob.class);
		final var someBlobBytes = "someBlob".getBytes();
		final var pdfEntity = PdfEntity.builder()
			.withTruncatedAt(null)
			.withDocument(blobMock)
			.build();

		when(blobMock.length()).thenReturn(123L);
		when(blobMock.getBytes(1, 123)).thenReturn(someBlobBytes);
		when(pdfRepositoryMock.findByFilenameAndMunicipalityId(filename, municipalityId)).thenReturn(Optional.empty());
		when(raindanceSambaIntegrationMock.findPdf(filename, municipalityId)).thenReturn(pdfEntity);

		final var result = invoicePdfService.getInvoicePdfByFilename(filename, municipalityId);

		assertThat(result).isNotNull();
		assertThat(result.content()).isEqualTo(Base64.getEncoder().encodeToString(someBlobBytes));

		verify(pdfRepositoryMock).findByFilenameAndMunicipalityId(filename, municipalityId);
		verify(raindanceSambaIntegrationMock).findPdf(filename, municipalityId);
		verify(pdfMapperMock).mapToResponse(pdfEntity);
		verifyNoInteractions(storageSambaIntegrationMock);
	}

	@Test
	void getInvoicePdfByFilename() {
		// Arrange
		final var filename = "someFileName";
		final var municipalityId = "2281";
		final var pdfEntity = generatePdfEntity();
		when(pdfRepositoryMock.findByFilenameAndMunicipalityId(filename, municipalityId)).thenReturn(Optional.of(pdfEntity));

		// Act
		final var invoicePdf = invoicePdfService.getInvoicePdfByFilename(filename, municipalityId);

		// Assert
		assertThat(invoicePdf.name()).isEqualTo("someFileName");
		assertThat(invoicePdf.content()).isEqualTo(Base64.getEncoder().encodeToString("blobMe".getBytes()));
		verify(pdfRepositoryMock, times(1)).findByFilenameAndMunicipalityId(filename, municipalityId);
		verifyNoInteractions(specificationsSpy);
		verifyNoMoreInteractions(pdfRepositoryMock);
	}

	@Test
	void getInvoicePdf_ByFilename_throwsException() {
		// Arrange
		final var filename = "someFileName";
		final var municipalityId = "2281";

		when(pdfRepositoryMock.findByFilenameAndMunicipalityId(filename, municipalityId)).thenThrow(new RuntimeException());

		// Act & Assert
		assertThatExceptionOfType(RuntimeException.class).isThrownBy(() -> invoicePdfService.getInvoicePdfByFilename(filename, municipalityId));
		verify(pdfRepositoryMock, times(1)).findByFilenameAndMunicipalityId(filename, municipalityId);
		verifyNoInteractions(specificationsSpy);
		verifyNoMoreInteractions(pdfRepositoryMock);
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

		when(pdfRepositoryMock.findAll(Mockito.<Specification<PdfEntity>>any())).thenReturn(List.of(pdfEntity));
		when(storageSambaIntegrationMock.readFile(pdfEntity.getFileHash())).thenReturn(someInputStreamBytes);

		final var result = invoicePdfService.getInvoicePdfByInvoiceNumber(issuerLegalId, invoiceNumber, request, municipalityId);

		assertThat(result).isNotNull();
		assertThat(result.content()).isEqualTo(Base64.getEncoder().encodeToString(someInputStreamBytes));
		verify(specificationsSpy).createInvoicesSpecification(request, invoiceNumber, issuerLegalId, municipalityId);
		verify(pdfRepositoryMock).findAll(Mockito.<Specification<PdfEntity>>any());
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

		when(pdfRepositoryMock.findAll(Mockito.<Specification<PdfEntity>>any())).thenReturn(List.of(pdfEntity));

		final var result = invoicePdfService.getInvoicePdfByInvoiceNumber(issuerLegalId, invoiceNumber, request, municipalityId);

		assertThat(result).isNotNull();
		assertThat(result.content()).isEqualTo(Base64.getEncoder().encodeToString("blobMe".getBytes()));
		verify(specificationsSpy).createInvoicesSpecification(request, invoiceNumber, issuerLegalId, municipalityId);
		verify(pdfRepositoryMock).findAll(Mockito.<Specification<PdfEntity>>any());
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

		when(pdfRepositoryMock.findAll(Mockito.<Specification<PdfEntity>>any())).thenReturn(List.of(generatePdfEntity()));

		// Act
		final var invoicePdf = invoicePdfService.getInvoicePdfByInvoiceNumber(issuerLegalId, invoiceNumber, request, municipalityId);

		// Assert
		assertThat(invoicePdf.name()).isEqualTo(fileName);
		assertThat(invoicePdf.content()).isEqualTo(Base64.getEncoder().encodeToString("blobMe".getBytes()));

		verify(pdfRepositoryMock, times(1)).findAll(Mockito.<Specification<PdfEntity>>any());
		verify(specificationsSpy, times(1)).createInvoicesSpecification(request, invoiceNumber, issuerLegalId, municipalityId);
		verifyNoMoreInteractions(pdfRepositoryMock);
		verifyNoMoreInteractions(specificationsSpy);
	}

	@Test
	void getInvoicePdfByInvoiceNumber_throwsExceptionByFilename() {
		// Arrange
		final var request = new InvoicePdfFilterRequest();
		final var issuerLegalId = "someIssuerLegalId";
		final var invoiceNumber = "someInvoiceNumber";
		final var municipalityId = "2281";
		final var exception = new RuntimeException();

		when(pdfRepositoryMock.findAll(Mockito.<Specification<PdfEntity>>any())).thenThrow(exception);

		// Act & Assert
		assertThatExceptionOfType(RuntimeException.class).isThrownBy(() -> invoicePdfService.getInvoicePdfByInvoiceNumber(
			issuerLegalId, invoiceNumber, request, municipalityId));

		verify(pdfRepositoryMock, times(1)).findAll(Mockito.<Specification<PdfEntity>>any());
		verify(specificationsSpy, times(1))
			.createInvoicesSpecification(request, invoiceNumber, issuerLegalId, municipalityId);
		verifyNoMoreInteractions(pdfRepositoryMock);
		verifyNoMoreInteractions(specificationsSpy);
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
		verifyNoInteractions(specificationsSpy);
	}

}
