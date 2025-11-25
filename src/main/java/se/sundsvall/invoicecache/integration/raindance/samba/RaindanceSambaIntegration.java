package se.sundsvall.invoicecache.integration.raindance.samba;

import static org.zalando.problem.Status.INTERNAL_SERVER_ERROR;

import java.util.Base64;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;
import org.zalando.problem.Problem;
import se.sundsvall.invoicecache.api.model.InvoicePdf;

@Component
@EnableScheduling
public class RaindanceSambaIntegration {

	private static final Logger LOGGER = LoggerFactory.getLogger(RaindanceSambaIntegration.class);
	private final RaindanceSambaProperties raindanceSambaProperties;

	RaindanceSambaIntegration(final RaindanceSambaProperties raindanceSambaProperties) {
		this.raindanceSambaProperties = raindanceSambaProperties;
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
}
