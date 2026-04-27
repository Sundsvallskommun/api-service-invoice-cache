package se.sundsvall.invoicecache.integration.storage.importer;

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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
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
	void writeOnePdf_writesSambaThenSavesEntity() throws SQLException {
		final var bytes = "pdf-content".getBytes();
		final var entry = new InvoiceIndexEntry("invoice.pdf", "INV-1", null, "C-1", "Customer One");
		when(storage.writeFile(any())).thenReturn("samba-hash");

		writer.writeOnePdf(bytes, entry, MUNICIPALITY_ID);

		verify(storage).writeFile(any());
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
}
