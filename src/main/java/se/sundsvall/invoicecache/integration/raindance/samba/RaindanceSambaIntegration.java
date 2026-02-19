package se.sundsvall.invoicecache.integration.raindance.samba;

import java.io.IOException;
import java.net.MalformedURLException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Base64;
import java.util.Objects;
import java.util.TimeZone;
import jcifs.CIFSException;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileInputStream;
import org.hibernate.engine.jdbc.BlobProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;
import org.zalando.problem.Problem;
import se.sundsvall.dept44.scheduling.Dept44Scheduled;
import se.sundsvall.dept44.scheduling.health.Dept44HealthUtility;
import se.sundsvall.invoicecache.api.model.InvoicePdf;
import se.sundsvall.invoicecache.integration.db.InvoiceRepository;
import se.sundsvall.invoicecache.integration.db.PdfRepository;
import se.sundsvall.invoicecache.integration.db.entity.PdfEntity;

import static org.zalando.problem.Status.INTERNAL_SERVER_ERROR;
import static se.sundsvall.invoicecache.Constant.RAINDANCE_ISSUER_LEGAL_ID;

@Component
@EnableScheduling
public class RaindanceSambaIntegration {

	private static final String MUNICIPALITY_ID = "2281";
	private static final Logger LOGGER = LoggerFactory.getLogger(RaindanceSambaIntegration.class);
	private final PdfRepository pdfRepository;
	private final InvoiceRepository invoiceRepository;
	private final RaindanceSambaProperties raindanceSambaProperties;
	private final Dept44HealthUtility dept44HealthUtility;
	@Value("${integration.raindance.samba.name}")
	private String jobName;

	RaindanceSambaIntegration(
		final PdfRepository pdfRepository,
		final InvoiceRepository invoiceRepository,
		final RaindanceSambaProperties raindanceSambaProperties,
		final Dept44HealthUtility dept44HealthUtility) {
		this.pdfRepository = pdfRepository;
		this.invoiceRepository = invoiceRepository;
		this.raindanceSambaProperties = raindanceSambaProperties;
		this.dept44HealthUtility = dept44HealthUtility;
	}

	static boolean isAfterYesterday(final long lastModified) {
		return LocalDateTime.ofInstant(Instant.ofEpochMilli(lastModified), TimeZone.getDefault().toZoneId()).isAfter(LocalDateTime.now().minusDays(1));
	}

	public InvoicePdf fetchInvoiceByFilename(final String filename) {
		final var path = raindanceSambaProperties.targetUrl() + "/" + filename;
		LOGGER.info("Fetching invoice from Samba with path: {}", path);
		try (final var file = new SmbFile(path, raindanceSambaProperties.cifsContext());
			final var inputStream = new SmbFileInputStream(file)) {
			return InvoicePdf.builder()
				.withName(filename)
				.withContent(Base64.getEncoder().encodeToString(inputStream.readAllBytes()))
				.build();

		} catch (final Exception e) {
			if (e.getMessage().contains("The system cannot find the file specified")) {
				LOGGER.error("Invoice PDF with name {} was not found at path: {}", filename, path);
				throw Problem.valueOf(INTERNAL_SERVER_ERROR, "Invoice PDF with name '%s' was not found".formatted(filename));
			}
			LOGGER.warn("Something went wrong when trying to fetch invoice with filename {} at path {}", filename, path, e);
			throw Problem.valueOf(INTERNAL_SERVER_ERROR, "Something went wrong when trying to fetch invoice");
		}
	}

	@Dept44Scheduled(cron = "${integration.raindance.samba.cron}",
		name = "${integration.raindance.samba.name}",
		lockAtMostFor = "${integration.raindance.samba.shedlock-lock-at-most-for}",
		maximumExecutionTime = "${integration.raindance.samba.maximum-execution-time}")
	void cacheInvoicePdfs() {

		final var start = System.currentTimeMillis();
		LOGGER.info("Starting caching of invoice pdfs");

		try (final var directory = new SmbFile(raindanceSambaProperties.targetUrl(), raindanceSambaProperties.cifsContext())) {
			Arrays.stream(Objects.requireNonNull(directory)
				.listFiles(file -> isAfterYesterday(file.lastModified()))).forEach(this::saveFile);

		} catch (final CIFSException | MalformedURLException e) {
			LOGGER.warn("Something went wrong when trying to cache pdf", e);
			dept44HealthUtility.setHealthIndicatorUnhealthy(jobName, "Something went wrong when trying to cache pdfs");
		}
		final var end = System.currentTimeMillis();
		LOGGER.info("Caching of invoice pdfs completed in {} seconds", (end - start) / 1000);
	}

	private void saveFile(final SmbFile file) {
		try (final var inputStream = new SmbFileInputStream(file)) {
			final var filename = file.getName().replace(raindanceSambaProperties.remoteDir(), "");
			final var entity = invoiceRepository.findByFileNameAndMunicipalityId(filename, RaindanceSambaIntegration.MUNICIPALITY_ID);
			final var pdfEntity = pdfRepository.findByFilenameAndMunicipalityId(filename, RaindanceSambaIntegration.MUNICIPALITY_ID);

			// Only save if entity exists and pdfEntity does not exist (indication that it has already been saved)
			if (entity.isPresent() && pdfEntity.isEmpty()) {
				pdfRepository.save(PdfEntity.builder()
					.withMunicipalityId(RaindanceSambaIntegration.MUNICIPALITY_ID)
					.withFilename(filename)
					.withDocument(BlobProxy.generateProxy(inputStream.readAllBytes()))
					.withInvoiceIssuerLegalId(RAINDANCE_ISSUER_LEGAL_ID)
					.withInvoiceDebtorLegalId(entity.get().getOrganizationNumber())
					.withInvoiceNumber(entity.get().getInvoiceNumber())
					.build());
			}
		} catch (final IOException e) {
			LOGGER.warn("Something went wrong when trying to save file", e);
			dept44HealthUtility.setHealthIndicatorUnhealthy(jobName, "Unable to save file when trying to cache pdfs.");

		}
	}

}
