package se.sundsvall.invoicecache.service.scheduler;

import java.time.OffsetDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.zalando.problem.Problem;
import se.sundsvall.invoicecache.integration.db.PdfRepository;

import static org.zalando.problem.Status.INTERNAL_SERVER_ERROR;
import static se.sundsvall.invoicecache.Constant.RAINDANCE_ISSUER_LEGAL_ID;

@Component
public class RemovalWorker {

	private static final Logger LOG = LoggerFactory.getLogger(RemovalWorker.class);
	private final PdfRepository pdfRepository;

	public RemovalWorker(final PdfRepository pdfRepository) {
		this.pdfRepository = pdfRepository;
	}

	@Transactional
	public void removeOldRaindanceInvoices() {
		var cutoff = OffsetDateTime.now().minusMonths(18L);
		var numberOfOldRaindancePdfs = pdfRepository.countAllByInvoiceIssuerLegalIdAndCreatedIsBefore(RAINDANCE_ISSUER_LEGAL_ID, cutoff);
		LOG.info("Found {} old Raindance PDFs to be removed.", numberOfOldRaindancePdfs);

		try {
			var numberOfRemovedPdfs = pdfRepository.deleteAllOldRaindancePdfs(RAINDANCE_ISSUER_LEGAL_ID, cutoff);
			LOG.info("Deleted {} old Raindance PDFs.", numberOfRemovedPdfs);
		} catch (Exception e) {
			LOG.error("Error occurred while removing old Raindance PDFs: {}", e.getMessage());
			throw Problem.valueOf(INTERNAL_SERVER_ERROR, "Failed to remove old Raindance PDFs.");
		}
	}

}
