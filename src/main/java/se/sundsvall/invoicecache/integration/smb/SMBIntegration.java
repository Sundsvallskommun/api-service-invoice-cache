package se.sundsvall.invoicecache.integration.smb;

import java.io.IOException;
import java.net.MalformedURLException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Objects;
import java.util.TimeZone;
import jcifs.CIFSException;
import jcifs.context.SingletonContext;
import jcifs.smb.NtlmPasswordAuthenticator;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileInputStream;
import org.hibernate.engine.jdbc.BlobProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;
import se.sundsvall.dept44.scheduling.Dept44Scheduled;
import se.sundsvall.invoicecache.integration.db.InvoiceRepository;
import se.sundsvall.invoicecache.integration.db.PdfRepository;
import se.sundsvall.invoicecache.integration.db.entity.PdfEntity;

@Component
@EnableScheduling
public class SMBIntegration {

	private static final String INVOICE_ISSUER_LEGAL_ID = "2120002411";

	private static final String MUNICIPALITY_ID = "2281";

	private static final Logger logger = LoggerFactory.getLogger(SMBIntegration.class);

	private final PdfRepository pdfRepository;

	private final InvoiceRepository invoiceRepository;

	private final SMBProperties properties;

	private final String sourceUrl;

	SMBIntegration(final PdfRepository pdfRepository, final InvoiceRepository invoiceRepository, final SMBProperties properties) {
		this.pdfRepository = pdfRepository;
		this.invoiceRepository = invoiceRepository;
		this.properties = properties;
		sourceUrl = "smb://" + properties.getDomain() +
			properties.getShareAndDir() + properties.getRemoteDir();
	}

	static boolean isAfterYesterday(final long lastModified) {
		return LocalDateTime.ofInstant(Instant.ofEpochMilli(lastModified), TimeZone.getDefault().toZoneId()).isAfter(LocalDateTime.now().minusDays(1));
	}

	public PdfEntity findPdf(final String filename, final String municipalityId) {
		try (final var file = createSmbFile(sourceUrl + "/" + filename)) {
			return saveFile(file, municipalityId);
		} catch (final MalformedURLException e) {
			logger.warn("Something went wrong when trying to find pdf", e);
			return null;
		}
	}

	@Dept44Scheduled(cron = "${integration.smb.cron}",
		name = "${integration.smb.name}",
		lockAtMostFor = "${integration.smb.shedlock-lock-at-most-for}",
		maximumExecutionTime = "${integration.smb.maximum-execution-time}")
	void cacheInvoicePdfs() {

		final long start = System.currentTimeMillis();
		logger.info("Starting caching of invoice pdfs");

		try (final var directory = createSmbFile(sourceUrl)) {
			Arrays.stream(Objects.requireNonNull(directory)
				.listFiles(file -> isAfterYesterday(file.lastModified()))).forEach(file1 -> saveFile(file1, MUNICIPALITY_ID));

		} catch (final CIFSException | MalformedURLException e) {
			logger.warn("Something went wrong when trying to cache pdf", e);
		}
		final long end = System.currentTimeMillis();
		logger.info("Caching of invoice pdfs completed in {} seconds", (end - start) / 1000);
	}

	private SmbFile createSmbFile(final String sourceUrl) throws MalformedURLException {
		final var base = SingletonContext.getInstance();
		final var cifsContext = base.withCredentials(new NtlmPasswordAuthenticator(properties.getUserDomain(),
			properties.getUser(), properties.getPassword()));
		try (final var directory = new SmbFile(sourceUrl, cifsContext)) {
			return directory;
		}
	}

	private PdfEntity saveFile(final SmbFile file, final String municipalityId) {
		try (final var inputStream = new SmbFileInputStream(file)) {
			final var filename = file.getName().replace(properties.getRemoteDir(), "");
			final var entity = invoiceRepository.findByFileNameAndMunicipalityId(filename, municipalityId).orElse(null);
			final var pdfEntity = pdfRepository.findByFilenameAndMunicipalityId(filename, municipalityId);

			// Only save if entity exists and pdfEntity does not exist (indication that it has already been saved)
			if (entity != null && pdfEntity.isEmpty()) {
				return pdfRepository.save(PdfEntity.builder()
					.withMunicipalityId(municipalityId)
					.withFilename(filename)
					.withDocument(BlobProxy.generateProxy(inputStream.readAllBytes()))
					.withInvoiceIssuerLegalId(INVOICE_ISSUER_LEGAL_ID)
					.withInvoiceDebtorLegalId(entity.getOrganizationNumber())
					.withInvoiceNumber(entity.getInvoiceNumber())
					.build());
			}
		} catch (final IOException e) {
			logger.warn("Something went wrong when trying to save file", e);
		}
		return null;
	}

}
