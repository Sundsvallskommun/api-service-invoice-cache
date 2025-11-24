package se.sundsvall.invoicecache.integration.raindance.samba;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Base64;
import java.util.Optional;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileInputStream;
import org.hibernate.engine.jdbc.BlobProxy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.zalando.problem.Problem;
import se.sundsvall.dept44.scheduling.health.Dept44HealthUtility;
import se.sundsvall.invoicecache.integration.db.InvoiceRepository;
import se.sundsvall.invoicecache.integration.db.PdfRepository;
import se.sundsvall.invoicecache.integration.db.entity.InvoiceEntity;
import se.sundsvall.invoicecache.integration.db.entity.PdfEntity;

@ExtendWith({
	MockitoExtension.class, OutputCaptureExtension.class
})
class RaindanceSambaIntegrationTest {

	private static final String INVOICE_ISSUER_LEGAL_ID = "2120002411";

	@Mock
	private Dept44HealthUtility dept44HealthUtilityMock;

	@Mock(answer = Answers.CALLS_REAL_METHODS)
	private RaindanceSambaProperties raindanceSambaProperties;

	@Mock
	private PdfRepository pdfRepository;

	@Mock
	private InvoiceRepository invoiceRepository;

	@InjectMocks
	private RaindanceSambaIntegration raindanceSambaIntegration;

	@BeforeEach
	void setup() throws NoSuchFieldException, IllegalAccessException {

		final Field field = RaindanceSambaIntegration.class.getDeclaredField("jobName");
		field.setAccessible(true);
		field.set(raindanceSambaIntegration, "jobName");
	}

	@Test
	void fetchInvoiceByFilename() throws IOException {
		final var filename = "test.pdf";
		final var content = "sample content".getBytes();

		try (final MockedConstruction<SmbFileInputStream> myobjectMockedConstruction = Mockito.mockConstruction(SmbFileInputStream.class,
			(mock, context) -> when(mock.readAllBytes()).thenReturn(content))) {

			final var result = raindanceSambaIntegration.fetchInvoiceByFilename(filename);

			assertThat(result.content()).isEqualTo(Base64.getEncoder().encodeToString(content));
			assertThat(result.name()).isEqualTo(filename);

			assertThat(myobjectMockedConstruction.constructed()).hasSize(1);
			final var mock = myobjectMockedConstruction.constructed().getFirst();
			verify(mock).readAllBytes();
		}
	}

	@Test
	void fetchInvoiceByFilename_throws() {
		final var filename = "test.pdf";

		try (var ignored = mockConstruction(SmbFile.class, (mock, context) -> when(mock.exists()).thenThrow(new SmbException("Random error")))) {

			assertThatThrownBy(() -> raindanceSambaIntegration.fetchInvoiceByFilename(filename))
				.isInstanceOf(Problem.class)
				.hasMessageContaining("Internal Server Error: Something went wrong when trying to fetch invoice by filename");
		}
	}

	@Test
	void findPdf_successfully() throws IOException {

		// Arrange
		final var blob = BlobProxy.generateProxy("blobMe".getBytes());
		final var fileName = "test.pdf";
		final var orgNr = "someOrgNr";
		final var invoiceNumber = "someInvoiceNumber";
		final var municipalityId = "2281";

		when(raindanceSambaProperties.remoteDir()).thenReturn("TEST");

		when(invoiceRepository.findByFileNameAndMunicipalityId(fileName, municipalityId))
			.thenReturn(Optional.ofNullable(InvoiceEntity.builder()
				.withMunicipalityId(municipalityId)
				.withOrganizationNumber(orgNr)
				.withInvoiceNumber(invoiceNumber)
				.build()));

		when(pdfRepository.findByFilenameAndMunicipalityId(fileName, municipalityId)).thenReturn(Optional.empty());
		when(pdfRepository.save(any())).thenReturn(PdfEntity.builder()
			.withFilename(fileName)
			.withDocument(blob)
			.withMunicipalityId(municipalityId)
			.withInvoiceDebtorLegalId(orgNr)
			.withInvoiceIssuerLegalId(INVOICE_ISSUER_LEGAL_ID)
			.withInvoiceNumber(invoiceNumber)
			.withId(1)
			.build());

		// Act
		try (final MockedConstruction<SmbFileInputStream> myobjectMockedConstruction = Mockito.mockConstruction(SmbFileInputStream.class,
			(mock, context) -> {
				when(mock.readAllBytes()).thenReturn(new byte[] {});// any additional mocking
			})) {

			final var result = raindanceSambaIntegration.findPdf(fileName, municipalityId);

			// Assert
			assertThat(result).isNotNull();
			assertThat(result.getFilename()).isEqualTo(fileName);
			assertThat(result.getMunicipalityId()).isEqualTo(municipalityId);
			assertThat(result.getId()).isEqualTo(1);
			assertThat(result.getDocument()).isEqualTo(blob);

			assertThat(result.getInvoiceIssuerLegalId()).isEqualTo(INVOICE_ISSUER_LEGAL_ID);
			assertThat(result.getInvoiceDebtorLegalId()).isEqualTo(orgNr);
			assertThat(result.getInvoiceNumber()).isEqualTo(invoiceNumber);

			assertThat(myobjectMockedConstruction.constructed()).hasSize(1);
			final var mock = myobjectMockedConstruction.constructed().getFirst();
			verify(mock, times(1)).readAllBytes();
			verify(pdfRepository).findByFilenameAndMunicipalityId(fileName, municipalityId);
			verify(pdfRepository, times(1)).save(any());
			verifyNoMoreInteractions(pdfRepository);
			verifyNoInteractions(dept44HealthUtilityMock);
		}
	}

	@Test
	void handleFile_ThrowsError(final CapturedOutput output) {
		// Arrange
		final var file = "";
		final var municipalityId = "2281";
		// Act
		raindanceSambaIntegration.findPdf(file, municipalityId);
		// Assert
		assertThat(output).contains("Something went wrong when trying to save file");
		verify(dept44HealthUtilityMock).setHealthIndicatorUnhealthy("jobName", "Unable to save file when trying to cache pdfs.");
		verifyNoInteractions(pdfRepository);
	}

	@Test
	void tryCache_ThrowsError(final CapturedOutput output) {
		raindanceSambaIntegration.cacheInvoicePdfs();
		assertThat(output).contains("Something went wrong when trying to cache pdf");
		verify(dept44HealthUtilityMock).setHealthIndicatorUnhealthy("jobName", "Something went wrong when trying to cache pdfs");

	}

	@Test
	void isDateAfterYesterday_true() {
		final var result = RaindanceSambaIntegration.isAfterYesterday(System.currentTimeMillis());
		assertThat(result).isTrue();
	}

	@Test
	void isDateAfterYesterday_False() {
		final var testValue = LocalDateTime.now().minusDays(2).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
		final var result = RaindanceSambaIntegration.isAfterYesterday(testValue);
		assertThat(result).isFalse();
	}

}
