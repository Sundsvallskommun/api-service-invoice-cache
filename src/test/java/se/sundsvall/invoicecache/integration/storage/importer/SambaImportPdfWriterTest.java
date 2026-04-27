package se.sundsvall.invoicecache.integration.storage.importer;

import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.invoicecache.integration.db.PdfRepository;
import se.sundsvall.invoicecache.integration.db.entity.PdfEntity;
import se.sundsvall.invoicecache.integration.storage.StorageSambaIntegration;
import se.sundsvall.invoicecache.integration.storage.importer.model.InvoiceIndexEntry;
import se.sundsvall.invoicecache.util.exception.BlobIntegrityException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SambaImportPdfWriterTest {

	private static final String MUNICIPALITY_ID = "2281";

	@Mock
	private StorageSambaIntegration storage;

	@Mock
	private PdfRepository pdfRepository;

	@Captor
	private ArgumentCaptor<PdfEntity> entityCaptor;

	@InjectMocks
	private SambaImportPdfWriter writer;

	@AfterEach
	void tearDown() {
		verifyNoMoreInteractions(storage, pdfRepository);
	}

	@Test
	void writeOnePdf_happyPath_writesSambaVerifiesAndSavesEntity() throws SQLException {
		final var bytes = pdfBytes("body");
		final var entry = new InvoiceIndexEntry("invoice.pdf", "INV-1", null, "C-1", "Customer One");
		when(storage.writeFile(any())).thenReturn("samba-hash");
		when(storage.verifyBlobIntegrity("samba-hash")).thenReturn("samba-hash");

		writer.writeOnePdf(bytes, entry, MUNICIPALITY_ID);

		verify(storage).writeFile(any());
		verify(storage).verifyBlobIntegrity("samba-hash");
		verify(pdfRepository).save(entityCaptor.capture());

		final var saved = entityCaptor.getValue();
		assertThat(saved.getFilename()).isEqualTo("invoice.pdf");
		assertThat(saved.getDocument()).isNull();
		assertThat(saved.getFileHash()).isEqualTo("samba-hash");
		assertThat(saved.getMovedAt()).isNotNull();
		assertThat(saved.getTruncatedAt()).isNotNull();
		assertThat(saved.getMunicipalityId()).isEqualTo(MUNICIPALITY_ID);
		assertThat(saved.getInvoiceNumber()).isEqualTo("INV-1");
		assertThat(saved.getInvoiceId()).isEqualTo("INV-1");
		assertThat(saved.getInvoiceDebtorLegalId()).isEqualTo("C-1");
		assertThat(saved.getInvoiceIssuerLegalId()).isEqualTo(SambaImportPdfWriter.IMPORT_ISSUER_LEGAL_ID);
		assertThat(saved.getInvoiceType()).isNull();
	}

	@Test
	void writeOnePdf_missingPdfMagic_refusesAndDoesNotTouchSamba() {
		final var entry = new InvoiceIndexEntry("not-a-pdf.pdf", "INV-2", null, null, null);

		assertThatThrownBy(() -> writer.writeOnePdf("not a pdf at all".getBytes(StandardCharsets.UTF_8), entry, MUNICIPALITY_ID))
			.isInstanceOf(BlobIntegrityException.class)
			.hasMessageContaining("does not start with %PDF-");

		verify(storage, never()).writeFile(any());
		verify(storage, never()).verifyBlobIntegrity(any());
		verify(pdfRepository, never()).save(any());
	}

	@Test
	void writeOnePdf_tooShortToBePdf_refusesAndDoesNotTouchSamba() {
		final var entry = new InvoiceIndexEntry("tiny.pdf", "INV-3", null, null, null);

		assertThatThrownBy(() -> writer.writeOnePdf(new byte[] {
			'%', 'P'
		}, entry, MUNICIPALITY_ID))
			.isInstanceOf(BlobIntegrityException.class)
			.hasMessageContaining("too short to be a PDF");

		verify(storage, never()).writeFile(any());
		verify(storage, never()).verifyBlobIntegrity(any());
		verify(pdfRepository, never()).save(any());
	}

	@Test
	void writeOnePdf_roundTripHashMismatch_refusesToSaveEntity() {
		final var bytes = pdfBytes("body");
		final var entry = new InvoiceIndexEntry("flaky.pdf", "INV-4", null, null, null);
		when(storage.writeFile(any())).thenReturn("sent-hash");
		when(storage.verifyBlobIntegrity("sent-hash")).thenReturn("DIFFERENT");

		assertThatThrownBy(() -> writer.writeOnePdf(bytes, entry, MUNICIPALITY_ID))
			.isInstanceOf(BlobIntegrityException.class)
			.hasMessageContaining("Round-trip hash mismatch")
			.hasMessageContaining("flaky.pdf");

		verify(storage).writeFile(any());
		verify(storage).verifyBlobIntegrity("sent-hash");
		verify(pdfRepository, never()).save(any());
	}

	private static byte[] pdfBytes(final String suffix) {
		final var prefix = "%PDF-1.4\n";
		final var combined = (prefix + suffix).getBytes(StandardCharsets.UTF_8);
		return combined;
	}
}
