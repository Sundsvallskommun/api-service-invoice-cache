package se.sundsvall.invoicecache.integration.storage.importer;

import java.sql.SQLException;
import java.time.OffsetDateTime;
import javax.sql.rowset.serial.SerialBlob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import se.sundsvall.invoicecache.integration.db.PdfRepository;
import se.sundsvall.invoicecache.integration.db.entity.PdfEntity;
import se.sundsvall.invoicecache.integration.storage.StorageSambaIntegration;
import se.sundsvall.invoicecache.integration.storage.importer.model.InvoiceIndexEntry;

/**
 * Writes a single imported PDF: pushes the bytes to Samba via {@link StorageSambaIntegration} and inserts a
 * {@link PdfEntity} row.
 */
@Component
public class SambaImportPdfWriter {

	static final String IMPORT_ISSUER_LEGAL_ID = "5565027223";
	private static final Logger LOG = LoggerFactory.getLogger(SambaImportPdfWriter.class);
	private final StorageSambaIntegration storage;
	private final PdfRepository pdfRepository;

	public SambaImportPdfWriter(final StorageSambaIntegration storage, final PdfRepository pdfRepository) {
		this.storage = storage;
		this.pdfRepository = pdfRepository;
	}

	@Transactional
	public void writeOnePdf(final byte[] bytes, final InvoiceIndexEntry entry, final String municipalityId) throws SQLException {
		final var blob = new SerialBlob(bytes);
		final var hash = storage.writeFile(blob);
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
			entry.source(), hash, entry.invoiceNumber(), entry.archiveDate());
	}
}
