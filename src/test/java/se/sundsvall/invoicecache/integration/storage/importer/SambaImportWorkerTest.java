package se.sundsvall.invoicecache.integration.storage.importer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.invoicecache.integration.db.PdfRepository;
import se.sundsvall.invoicecache.integration.storage.importer.model.InvoiceIndex;
import se.sundsvall.invoicecache.integration.storage.importer.model.InvoiceIndexEntry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SambaImportWorkerTest {

	private static final String MUNICIPALITY_ID = "2281";

	@Mock
	private SambaImportFileSystem fileSystem;

	@Mock
	private IndexXmlParser indexParser;

	@Mock
	private SambaImportPdfWriter pdfWriter;

	@Mock
	private PdfRepository pdfRepository;

	@Captor
	private ArgumentCaptor<InvoiceIndexEntry> entryCaptor;

	@InjectMocks
	private SambaImportWorker worker;

	@AfterEach
	void tearDown() {
		verifyNoMoreInteractions(fileSystem, indexParser, pdfWriter, pdfRepository);
	}

	@Test
	void importAll_emptyList_doesNothing() {
		when(fileSystem.listZipFiles()).thenReturn(List.of());

		worker.importAll(MUNICIPALITY_ID);

		verify(fileSystem).listZipFiles();
	}

	@Test
	void importAll_happyPath_importsAndDeletesZip() throws Exception {
		final var pdfBytes = "pdf-content".getBytes();
		final var entry = new InvoiceIndexEntry("invoice.pdf", "INV-1", null, "C-1", "Customer One");
		final var index = new InvoiceIndex("123", List.of(entry));
		final var zipBytes = makeZip("index_123.xml", "ignored", "invoice.pdf", pdfBytes);

		when(fileSystem.listZipFiles()).thenReturn(List.of("123.zip"));
		when(fileSystem.openInputStream("123.zip"))
			.thenReturn(new ByteArrayInputStream(zipBytes), new ByteArrayInputStream(zipBytes));
		when(indexParser.parse(any())).thenReturn(index);
		when(pdfRepository.existsByFilename("invoice.pdf")).thenReturn(false);

		worker.importAll(MUNICIPALITY_ID);

		verify(fileSystem).listZipFiles();
		verify(fileSystem, times(2)).openInputStream("123.zip");
		verify(indexParser).parse(any());
		verify(pdfRepository).existsByFilename("invoice.pdf");
		verify(pdfWriter).writeOnePdf(any(), entryCaptor.capture(), eq(MUNICIPALITY_ID));
		verify(fileSystem).delete("123.zip");

		assertThat(entryCaptor.getValue()).isEqualTo(entry);
	}

	@Test
	void importAll_unparseableIndex_leavesZip() throws Exception {
		final var zipBytes = makeZip("index_42.xml", "<bad/>", "invoice.pdf", "pdf".getBytes());

		when(fileSystem.listZipFiles()).thenReturn(List.of("42.zip"));
		when(fileSystem.openInputStream("42.zip")).thenReturn(new ByteArrayInputStream(zipBytes));
		when(indexParser.parse(any())).thenThrow(new IOException("parse error"));

		worker.importAll(MUNICIPALITY_ID);

		verify(fileSystem).listZipFiles();
		verify(fileSystem).openInputStream("42.zip");
		verify(indexParser).parse(any());
		verify(fileSystem, never()).delete(any());
		verify(pdfWriter, never()).writeOnePdf(any(), any(), any());
	}

	@Test
	void importAll_duplicateFilename_skipsButStillDeletesZip() throws Exception {
		final var entry = new InvoiceIndexEntry("dup.pdf", "INV-D", null, null, null);
		final var index = new InvoiceIndex("dup", List.of(entry));
		final var zipBytes = makeZip("index_dup.xml", "x", "dup.pdf", "pdf".getBytes());

		when(fileSystem.listZipFiles()).thenReturn(List.of("dup.zip"));
		when(fileSystem.openInputStream("dup.zip"))
			.thenReturn(new ByteArrayInputStream(zipBytes), new ByteArrayInputStream(zipBytes));
		when(indexParser.parse(any())).thenReturn(index);
		when(pdfRepository.existsByFilename("dup.pdf")).thenReturn(true);

		worker.importAll(MUNICIPALITY_ID);

		verify(fileSystem).listZipFiles();
		verify(fileSystem, times(2)).openInputStream("dup.zip");
		verify(indexParser).parse(any());
		verify(pdfRepository).existsByFilename("dup.pdf");
		verify(pdfWriter, never()).writeOnePdf(any(), any(), any());
		verify(fileSystem).delete("dup.zip");
	}

	@Test
	void importAll_pdfMissingFromZip_keepsZip() throws Exception {
		final var entry = new InvoiceIndexEntry("missing.pdf", "INV-M", null, null, null);
		final var index = new InvoiceIndex("99", List.of(entry));
		// zip only contains the index, not the PDF referenced by it
		final var zipBytes = makeZip("index_99.xml", "x", null, null);

		when(fileSystem.listZipFiles()).thenReturn(List.of("99.zip"));
		when(fileSystem.openInputStream("99.zip"))
			.thenReturn(new ByteArrayInputStream(zipBytes), new ByteArrayInputStream(zipBytes));
		when(indexParser.parse(any())).thenReturn(index);

		worker.importAll(MUNICIPALITY_ID);

		verify(fileSystem).listZipFiles();
		verify(fileSystem, times(2)).openInputStream("99.zip");
		verify(indexParser).parse(any());
		verify(pdfWriter, never()).writeOnePdf(any(), any(), any());
		verify(fileSystem, never()).delete(any());
	}

	@Test
	void importAll_writeFileFails_keepsZip() throws Exception {
		final var entry = new InvoiceIndexEntry("fail.pdf", "INV-F", null, null, null);
		final var index = new InvoiceIndex("7", List.of(entry));
		final var zipBytes = makeZip("index_7.xml", "x", "fail.pdf", "pdf".getBytes());

		when(fileSystem.listZipFiles()).thenReturn(List.of("7.zip"));
		when(fileSystem.openInputStream("7.zip"))
			.thenReturn(new ByteArrayInputStream(zipBytes), new ByteArrayInputStream(zipBytes));
		when(indexParser.parse(any())).thenReturn(index);
		when(pdfRepository.existsByFilename("fail.pdf")).thenReturn(false);
		doThrow(new se.sundsvall.invoicecache.util.exception.BlobWriteException("samba down", new RuntimeException()))
			.when(pdfWriter).writeOnePdf(any(), any(), any());

		worker.importAll(MUNICIPALITY_ID);

		verify(fileSystem).listZipFiles();
		verify(fileSystem, times(2)).openInputStream("7.zip");
		verify(indexParser).parse(any());
		verify(pdfRepository).existsByFilename("fail.pdf");
		verify(pdfWriter).writeOnePdf(any(), any(), any());
		verify(fileSystem, never()).delete(any());
	}

	@Test
	void importAll_zipWithoutIndex_leavesZip() throws Exception {
		final var zipBytes = makeZip(null, null, "invoice.pdf", "pdf".getBytes());

		when(fileSystem.listZipFiles()).thenReturn(List.of("noindex.zip"));
		when(fileSystem.openInputStream("noindex.zip")).thenReturn(new ByteArrayInputStream(zipBytes));

		worker.importAll(MUNICIPALITY_ID);

		verify(fileSystem).listZipFiles();
		verify(fileSystem).openInputStream("noindex.zip");
		verify(pdfWriter, never()).writeOnePdf(any(), any(), any());
		verify(fileSystem, never()).delete(any());
	}

	private static byte[] makeZip(final String indexName, final String indexContent, final String pdfName, final byte[] pdfBytes) throws IOException {
		final var out = new ByteArrayOutputStream();
		try (final var zip = new ZipOutputStream(out)) {
			if (indexName != null) {
				zip.putNextEntry(new ZipEntry(indexName));
				zip.write(indexContent.getBytes());
				zip.closeEntry();
			}
			if (pdfName != null) {
				zip.putNextEntry(new ZipEntry(pdfName));
				zip.write(pdfBytes);
				zip.closeEntry();
			}
		}
		return out.toByteArray();
	}
}
