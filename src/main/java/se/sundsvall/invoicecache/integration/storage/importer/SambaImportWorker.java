package se.sundsvall.invoicecache.integration.storage.importer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.xml.parsers.ParserConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;
import se.sundsvall.invoicecache.integration.db.PdfRepository;
import se.sundsvall.invoicecache.integration.storage.importer.model.InvoiceIndex;
import se.sundsvall.invoicecache.integration.storage.importer.model.InvoiceIndexEntry;
import se.sundsvall.invoicecache.util.exception.BlobIntegrityException;
import se.sundsvall.invoicecache.util.exception.BlobWriteException;

import static java.util.Locale.ROOT;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

@Component
public class SambaImportWorker {

	// Zip-bomb / zip-slip guards (sonar java:S5042). Generous limits sized for invoice imports:
	// PDFs are typically <10 MB but allow headroom; zips can be multi-GB total.
	static final int MAX_ENTRIES_PER_ZIP = 100_000;
	static final long MAX_PDF_BYTES = 200L * 1024 * 1024;        // 200 MB per PDF
	static final long MAX_INDEX_BYTES = 64L * 1024 * 1024;       // 64 MB per index.xml
	static final long MAX_TOTAL_BYTES = 50L * 1024 * 1024 * 1024; // 50 GB uncompressed per zip
	private static final Logger LOG = LoggerFactory.getLogger(SambaImportWorker.class);
	private final SambaImportFileSystem fileSystem;
	private final IndexXmlParser indexParser;
	private final SambaImportPdfWriter pdfWriter;
	private final PdfRepository pdfRepository;

	public SambaImportWorker(
		final SambaImportFileSystem fileSystem,
		final IndexXmlParser indexParser,
		final SambaImportPdfWriter pdfWriter,
		final PdfRepository pdfRepository) {
		this.fileSystem = fileSystem;
		this.indexParser = indexParser;
		this.pdfWriter = pdfWriter;
		this.pdfRepository = pdfRepository;
	}

	private static Map<String, InvoiceIndexEntry> toFilenameMap(final InvoiceIndex index) {
		return index.documents().stream()
			.filter(entry -> entry.source() != null)
			.collect(toMap(InvoiceIndexEntry::source, identity(), (a, _) -> a));
	}

	private static InvoiceIndexEntry matchEntry(final ZipEntry entry, final Map<String, InvoiceIndexEntry> byFilename) {
		if (entry.isDirectory() || isUnsafeEntryName(entry.getName())) {
			return null;
		}
		return byFilename.get(baseName(entry.getName()));
	}

	/**
	 * Read the current entry's bytes with both a per-entry cap and a remaining-budget cap to defeat zip-bomb attacks (sonar
	 * java:S5042).
	 */
	private static byte[] readEntryWithLimit(final ZipInputStream zip, final long perEntryMax, final long remainingBudget,
		final String entryName, final String zipName) throws IOException {
		final var ceiling = Math.clamp(remainingBudget, 0L, perEntryMax);
		final var out = new ByteArrayOutputStream();
		final var buffer = new byte[8192];
		var total = 0L;
		int n;
		while ((n = zip.read(buffer)) != -1) {
			total += n;
			if (total > ceiling) {
				throw new IOException("Entry '%s' in zip '%s' exceeds size limit (%d bytes)".formatted(entryName, zipName, ceiling));
			}
			out.write(buffer, 0, n);
		}
		return out.toByteArray();
	}

	/**
	 * Reject zip entry names that try to escape the extraction root via path traversal or absolute paths (sonar java:S5042
	 * / zip-slip). Even though the worker only uses {@link #baseName} when matching, the explicit guard keeps the intent
	 * visible and
	 * short-circuits malicious entries.
	 */
	private static boolean isUnsafeEntryName(final String name) {
		if (name == null || name.isBlank()) {
			return true;
		}
		final var normalized = name.replace('\\', '/');
		if (normalized.startsWith("/") || normalized.contains("../") || normalized.equals("..") || normalized.endsWith("/..")) {
			return true;
		}
		return normalized.contains("\0");
	}

	static String expectedIndexName(final String zipName) {
		final var base = baseName(zipName);
		final var dot = base.lastIndexOf('.');
		final var stem = dot < 0 ? base : base.substring(0, dot);
		return "index_" + stem + ".xml";
	}

	static String baseName(final String entryName) {
		return Optional.ofNullable(entryName)
			.map(name -> Path.of(name).getFileName())
			.map(Path::toString)
			.orElse("");
	}

	private static boolean isAnyIndexXml(final String name) {
		final var lower = name.toLowerCase(ROOT);
		return lower.startsWith("index_") && lower.endsWith(".xml");
	}

	public void importAll(final String municipalityId) {
		final var zipNames = fileSystem.listZipFiles();
		LOG.info("Found {} zip(s) to import", zipNames.size());
		for (final var zipName : zipNames) {
			try {
				importOneZip(zipName, municipalityId);
			} catch (final BlobIntegrityException | BlobWriteException | DataAccessException e) {
				LOG.error("Failed to import zip '{}', leaving it in place and continuing", zipName, e);
			}
		}
		LOG.info("Import pass complete");
	}

	void importOneZip(final String zipName, final String municipalityId) {
		final var indexName = expectedIndexName(zipName);
		LOG.info("Processing zip '{}', expecting index '{}'", zipName, indexName);

		final var index = readIndexQuietly(zipName, indexName);
		if (index == null || index.documents().isEmpty()) {
			LOG.warn("No usable index entries in zip '{}', leaving it in place", zipName);
			return;
		}

		final var byFilename = toFilenameMap(index);
		final var counts = streamAndImportPdfs(zipName, byFilename, municipalityId);

		LOG.info("Zip '{}' done: imported={}, skipped={}, failed={}",
			zipName, counts.imported(), counts.skipped(), counts.failed());
		finalizeZip(zipName, counts);
	}

	private InvoiceIndex readIndexQuietly(final String zipName, final String indexName) {
		try {
			return readIndex(zipName, indexName);
		} catch (final IOException | SAXException | ParserConfigurationException | BlobIntegrityException e) {
			LOG.warn("Could not read or parse index for zip '{}', leaving it in place", zipName, e);
			return null;
		}
	}

	private ImportCounts streamAndImportPdfs(final String zipName, final Map<String, InvoiceIndexEntry> byFilename, final String municipalityId) {
		var imported = 0;
		var skipped = 0;
		var failed = byFilename.size();

		try (final var in = fileSystem.openInputStream(zipName);
			final var zip = new ZipInputStream(in)) {
			ZipEntry entry;
			var entryCount = 0;
			var totalBytes = 0L;
			while ((entry = zip.getNextEntry()) != null) {
				if (++entryCount > MAX_ENTRIES_PER_ZIP) {
					throw new IOException("Zip '%s' exceeds entry limit (%d) — refusing to continue".formatted(zipName, MAX_ENTRIES_PER_ZIP));
				}
				final var meta = matchEntry(entry, byFilename);
				if (meta == null) {
					continue;
				}
				final var bytesBefore = totalBytes;
				final var outcome = processEntry(zip, meta, municipalityId, zipName, totalBytes);
				if (outcome.bytesRead() > 0) {
					totalBytes = bytesBefore + outcome.bytesRead();
					if (totalBytes > MAX_TOTAL_BYTES) {
						throw new IOException("Zip '%s' exceeds total uncompressed size (%d bytes) — refusing to continue".formatted(zipName, MAX_TOTAL_BYTES));
					}
				}
				switch (outcome.status()) {
					case IMPORTED -> {
						imported++;
						failed--;
					}
					case SKIPPED -> {
						skipped++;
						failed--;
					}
					case FAILED -> { /* keep failed count */ }
				}
			}
		} catch (final IOException | BlobIntegrityException e) {
			LOG.error("Failed while streaming zip '{}', leaving it in place", zipName, e);
		}
		return new ImportCounts(imported, skipped, failed);
	}

	private EntryResult processEntry(final ZipInputStream zip, final InvoiceIndexEntry meta, final String municipalityId, final String zipName, final long totalSoFar) {
		final var name = meta.source();
		try {
			if (pdfRepository.existsByFilename(name)) {
				LOG.info("Skipping duplicate filename '{}' from zip '{}'", name, zipName);
				return new EntryResult(EntryOutcome.SKIPPED, 0L);
			}
			final var bytes = readEntryWithLimit(zip, MAX_PDF_BYTES, MAX_TOTAL_BYTES - totalSoFar, name, zipName);
			pdfWriter.writeOnePdf(bytes, meta, municipalityId);
			return new EntryResult(EntryOutcome.IMPORTED, bytes.length);
		} catch (final IOException | SQLException | BlobWriteException | DataAccessException e) {
			LOG.error("Failed to import pdf '{}' from zip '{}'", name, zipName, e);
			return new EntryResult(EntryOutcome.FAILED, 0L);
		}
	}

	private void finalizeZip(final String zipName, final ImportCounts counts) {
		if (counts.failed() != 0) {
			LOG.warn("Leaving zip '{}' in place because failed={}", zipName, counts.failed());
			return;
		}
		try {
			fileSystem.delete(zipName);
			LOG.info("Deleted source zip '{}' after successful import", zipName);
		} catch (final BlobWriteException e) {
			LOG.error("Import succeeded but failed to delete zip '{}'", zipName, e);
		}
	}

	InvoiceIndex readIndex(final String zipName, final String expectedIndexName)
		throws IOException, SAXException, ParserConfigurationException {
		try (final var in = fileSystem.openInputStream(zipName);
			final var zip = new ZipInputStream(in)) {
			return findAndParseIndex(zip, expectedIndexName);
		}
	}

	private InvoiceIndex findAndParseIndex(final ZipInputStream zip, final String expectedIndexName)
		throws IOException, SAXException, ParserConfigurationException {
		ZipEntry entry;
		InvoiceIndex fallback = null;
		var entryCount = 0;
		while ((entry = zip.getNextEntry()) != null) {
			if (++entryCount > MAX_ENTRIES_PER_ZIP) {
				throw new IOException("Zip exceeds entry limit (" + MAX_ENTRIES_PER_ZIP + ") while scanning for index");
			}
			if (entry.isDirectory() || isUnsafeEntryName(entry.getName())) {
				continue;
			}
			final var name = baseName(entry.getName());
			if (expectedIndexName.equalsIgnoreCase(name)) {
				return parseIndexBytes(zip, name);
			}
			if (fallback == null && isAnyIndexXml(name)) {
				fallback = parseIndexBytes(zip, name);
			}
		}
		return fallback;
	}

	private InvoiceIndex parseIndexBytes(final ZipInputStream zip, final String entryName)
		throws IOException, SAXException, ParserConfigurationException {
		final var bytes = readEntryWithLimit(zip, MAX_INDEX_BYTES, MAX_INDEX_BYTES, entryName, "<index>");
		return indexParser.parse(new ByteArrayInputStream(bytes));
	}

	private enum EntryOutcome {
		IMPORTED, SKIPPED, FAILED
	}

	private record ImportCounts(int imported, int skipped, int failed) {
	}

	private record EntryResult(EntryOutcome status, long bytesRead) {
	}
}
