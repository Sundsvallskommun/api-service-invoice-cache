package se.sundsvall.invoicecache.integration.raindance.samba;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.sundsvall.invoicecache.TestObjectFactory.generateInvoiceEntity;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Base64;
import java.util.Optional;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileFilter;
import jcifs.smb.SmbFileInputStream;
import org.junit.jupiter.api.AfterEach;
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

@ExtendWith({
	MockitoExtension.class, OutputCaptureExtension.class
})
class RaindanceSambaIntegrationTest {

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

	@AfterEach
	void tearDown() {
		verifyNoMoreInteractions(pdfRepository, invoiceRepository, raindanceSambaProperties, dept44HealthUtilityMock);
	}

	@BeforeEach
	void setup() throws NoSuchFieldException, IllegalAccessException {

		final var field = RaindanceSambaIntegration.class.getDeclaredField("jobName");
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
			verify(raindanceSambaProperties).targetUrl();
			verify(raindanceSambaProperties).cifsContext();
		}
	}

	@Test
	void fetchInvoiceByFilename_throws() {
		final var filename = "test.pdf";

		try (final var construction = mockConstruction(SmbFile.class, (mock, context) -> when(mock.exists()).thenThrow(new SmbException("Random error")))) {

			assertThatThrownBy(() -> raindanceSambaIntegration.fetchInvoiceByFilename(filename))
				.isInstanceOf(Problem.class)
				.hasMessageContaining("Internal Server Error: Something went wrong when trying to fetch invoice");

			verify(raindanceSambaProperties).targetUrl();
			verify(raindanceSambaProperties).cifsContext();
		}
	}

	@Test
	void cacheInvoicePdfs() {

		when(raindanceSambaProperties.remoteDir()).thenReturn("someDir/");
		final var bytes = "sample content".getBytes();

		try (final var smbFileMock = Mockito.mock(SmbFile.class)) {
			when(smbFileMock.getName()).thenReturn("someDir/someFile.pdf");
			final var smbFileArray = new SmbFile[] {
				smbFileMock
			};

			try (final var smbFileConstruction = mockConstruction(SmbFile.class, (mock, context) -> when(mock.listFiles(any(SmbFileFilter.class))).thenReturn(smbFileArray))) {
				try (final var smbInputStreamConstruction = mockConstruction(SmbFileInputStream.class, (mock, ctx) -> when(mock.readAllBytes()).thenReturn(bytes))) {

					when(invoiceRepository.findByFileNameAndMunicipalityId("someFile.pdf", "2281")).thenReturn(Optional.of(generateInvoiceEntity()));
					when(pdfRepository.findByFilenameAndMunicipalityId("someFile.pdf", "2281")).thenReturn(Optional.empty());

					raindanceSambaIntegration.cacheInvoicePdfs();

					verify(invoiceRepository).findByFileNameAndMunicipalityId("someFile.pdf", "2281");
					verify(pdfRepository).findByFilenameAndMunicipalityId("someFile.pdf", "2281");
					verify(pdfRepository).save(any());
					verify(raindanceSambaProperties).targetUrl();
					verify(raindanceSambaProperties).cifsContext();
					verifyNoInteractions(dept44HealthUtilityMock);
				}
			}
		}
	}

	@Test
	void cacheInvoicePdfs_ThrowsError(final CapturedOutput output) {
		raindanceSambaIntegration.cacheInvoicePdfs();
		assertThat(output).contains("Something went wrong when trying to cache pdf");
		verify(dept44HealthUtilityMock).setHealthIndicatorUnhealthy("jobName", "Something went wrong when trying to cache pdfs");
		verify(raindanceSambaProperties).targetUrl();
		verify(raindanceSambaProperties).cifsContext();
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
