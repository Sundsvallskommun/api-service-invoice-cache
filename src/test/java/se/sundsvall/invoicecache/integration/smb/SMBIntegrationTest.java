package se.sundsvall.invoicecache.integration.smb;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
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
import se.sundsvall.dept44.scheduling.health.Dept44HealthUtility;
import se.sundsvall.invoicecache.integration.db.InvoiceRepository;
import se.sundsvall.invoicecache.integration.db.PdfRepository;
import se.sundsvall.invoicecache.integration.db.entity.InvoiceEntity;
import se.sundsvall.invoicecache.integration.db.entity.PdfEntity;

@ExtendWith({
	MockitoExtension.class, OutputCaptureExtension.class
})
class SMBIntegrationTest {

	private static final String INVOICE_ISSUER_LEGAL_ID = "2120002411";

	@Mock
	private Dept44HealthUtility dept44HealthUtilityMock;

	@Mock(answer = Answers.CALLS_REAL_METHODS)
	private SMBProperties smbProperties;

	@Mock
	private PdfRepository pdfRepository;

	@Mock
	private InvoiceRepository invoiceRepository;

	@InjectMocks
	private SMBIntegration smbIntegration;

	@BeforeEach
	void setup() throws NoSuchFieldException, IllegalAccessException {

		final Field field = SMBIntegration.class.getDeclaredField("jobName");
		field.setAccessible(true);
		field.set(smbIntegration, "jobName");
	}

	@Test
	void findPdf_successfully() throws IOException {

		// Arrange
		final var blob = BlobProxy.generateProxy("blobMe".getBytes());
		final var fileName = "test.pdf";
		final var orgNr = "someOrgNr";
		final var invoiceNumber = "someInvoiceNumber";
		final var municipalityId = "2281";

		when(smbProperties.getRemoteDir()).thenReturn("TEST");

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

			final var result = smbIntegration.findPdf(fileName, municipalityId);

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
		smbIntegration.findPdf(file, municipalityId);
		// Assert
		assertThat(output).contains("Something went wrong when trying to save file");
		verify(dept44HealthUtilityMock).setHealthIndicatorUnhealthy("jobName", "Unable to save file when trying to cache pdfs.");
		verifyNoInteractions(pdfRepository);
	}

	@Test
	void tryCache_ThrowsError(final CapturedOutput output) {
		smbIntegration.cacheInvoicePdfs();
		assertThat(output).contains("Something went wrong when trying to cache pdf");
		verify(dept44HealthUtilityMock).setHealthIndicatorUnhealthy("jobName", "Something went wrong when trying to cache pdfs");

	}

	@Test
	void isDateAfterYesterday_true() {
		final var result = SMBIntegration.isAfterYesterday(System.currentTimeMillis());
		assertThat(result).isTrue();
	}

	@Test
	void isDateAfterYesterday_False() {
		final var testValue = LocalDateTime.now().minusDays(2).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
		final var result = SMBIntegration.isAfterYesterday(testValue);
		assertThat(result).isFalse();
	}

}
