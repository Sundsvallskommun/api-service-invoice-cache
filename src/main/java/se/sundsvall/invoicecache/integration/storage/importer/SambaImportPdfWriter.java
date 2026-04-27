package se.sundsvall.invoicecache.integration.storage.importer;

import java.sql.SQLException;
import java.time.OffsetDateTime;
import javax.sql.rowset.serial.SerialBlob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import se.sundsvall.dept44.util.LogUtils;
import se.sundsvall.invoicecache.integration.db.PdfRepository;
import se.sundsvall.invoicecache.integration.db.entity.PdfEntity;
import se.sundsvall.invoicecache.integration.storage.StorageSambaIntegration;
import se.sundsvall.invoicecache.integration.storage.importer.model.InvoiceIndexEntry;
import se.sundsvall.invoicecache.util.exception.BlobIntegrityException;

/**
 * Writes a single imported PDF: pushes the bytes to Samba via {@link StorageSambaIntegration} and inserts a
 * {@link PdfEntity} row.
 */
@Component
public class SambaImportPdfWriter {

	static final String IMPORT_ISSUER_LEGAL_ID = "5565027223";
	private static final Logger LOG = LoggerFactory.getLogger(SambaImportPdfWriter.class);
	// PDF magic bytes — every valid PDF starts with "%PDF-".
	private static final byte[] PDF_MAGIC = {
		'%', 'P', 'D', 'F', '-'
	};

	private final StorageSambaIntegration storage;
	private final PdfRepository pdfRepository;

	public SambaImportPdfWriter(final StorageSambaIntegration storage, final PdfRepository pdfRepository) {
		this.storage = storage;
		this.pdfRepository = pdfRepository;
	}

	@Transactional
	public void writeOnePdf(final byte[] bytes, final InvoiceIndexEntry entry, final String municipalityId) throws SQLException {
		final var cleanedSource = LogUtils.sanitizeForLogging(entry.source());
		final var cleanedInvoiceNumber = LogUtils.sanitizeForLogging(entry.invoiceNumber());

		// 1) Magic-number sanity check — refuse anything that isn't a PDF before we touch Samba.
		assertLooksLikePdf(bytes, entry.source());

		// 2) Stream bytes to Samba; samba returns the SHA-256 of what we sent.
		final var blob = new SerialBlob(bytes);
		final var hash = storage.writeFile(blob);

		// 3) Round-trip verify — re-read the file from Samba and recompute the hash. If it differs,
		// the bytes did not survive the trip; refuse to record the row so the source zip stays.
		final var verified = storage.verifyBlobIntegrity(hash);
		if (!hash.equals(verified)) {
			throw new BlobIntegrityException(
				"Round-trip hash mismatch for '" + entry.source() + "' (sent=" + hash + ", stored=" + verified + ")",
				null);
		}

		// 4) Persist the row only after Samba confirms integrity.
		final var now = OffsetDateTime.now();
		final var pdfEntity = PdfEntity.builder()
			.withMunicipalityId(municipalityId)
			.withFilename(entry.source())
			.withDocument(null)
			.withFileHash(hash)
			.withMovedAt(now)
			.withTruncatedAt(now)
			.withInvoiceNumber(entry.invoiceNumber())
			.withInvoiceId(entry.invoiceNumber())
			.withInvoiceDebtorLegalId(entry.customerNumber())
			.withInvoiceIssuerLegalId(IMPORT_ISSUER_LEGAL_ID)
			.withInvoiceType(null)
			.build();
		pdfRepository.save(pdfEntity);

		LOG.info("Imported pdf '{}' (hash={}) for invoiceNumber={}, archiveDate={}",
			cleanedSource, hash, cleanedInvoiceNumber, entry.archiveDate());
	}

	private static void assertLooksLikePdf(final byte[] bytes, final String filename) {
		if (bytes == null || bytes.length < PDF_MAGIC.length) {
			throw new BlobIntegrityException("File '" + filename + "' is too short to be a PDF", null);
		}
		for (var i = 0; i < PDF_MAGIC.length; i++) {
			if (bytes[i] != PDF_MAGIC[i]) {
				throw new BlobIntegrityException(
					"File '" + filename + "' does not start with %PDF- magic; refusing to import",
					null);
			}
		}
	}
}
