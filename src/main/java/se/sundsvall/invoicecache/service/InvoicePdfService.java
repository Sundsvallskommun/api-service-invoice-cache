package se.sundsvall.invoicecache.service;

import static org.zalando.problem.Status.NOT_FOUND;
import static se.sundsvall.invoicecache.Constant.RAINDANCE_ISSUER_LEGAL_ID;

import org.springframework.stereotype.Service;
import org.zalando.problem.Problem;
import se.sundsvall.invoicecache.api.model.InvoicePdf;
import se.sundsvall.invoicecache.api.model.InvoicePdfFilterRequest;
import se.sundsvall.invoicecache.api.model.InvoicePdfRequest;
import se.sundsvall.invoicecache.integration.db.InvoiceRepository;
import se.sundsvall.invoicecache.integration.db.PdfRepository;
import se.sundsvall.invoicecache.integration.db.specifications.InvoicePdfSpecifications;
import se.sundsvall.invoicecache.integration.raindance.samba.RaindanceSambaIntegration;
import se.sundsvall.invoicecache.integration.storage.StorageSambaIntegration;
import se.sundsvall.invoicecache.service.mapper.PdfMapper;

@Service
public class InvoicePdfService {

	private final InvoiceRepository invoiceRepository;
	private final PdfRepository pdfRepository;
	private final InvoicePdfSpecifications invoicePdfSpecifications;

	private final RaindanceSambaIntegration raindanceSambaIntegration;
	private final StorageSambaIntegration storageSambaIntegration;

	private final PdfMapper pdfMapper;

	public InvoicePdfService(
		final InvoiceRepository invoiceRepository,
		final PdfRepository pdfRepository,
		final PdfMapper pdfMapper,
		final RaindanceSambaIntegration raindanceSambaIntegration,
		final InvoicePdfSpecifications invoicePdfSpecifications,
		final StorageSambaIntegration storageSambaIntegration) {
		this.invoiceRepository = invoiceRepository;
		this.pdfRepository = pdfRepository;
		this.pdfMapper = pdfMapper;
		this.raindanceSambaIntegration = raindanceSambaIntegration;
		this.invoicePdfSpecifications = invoicePdfSpecifications;
		this.storageSambaIntegration = storageSambaIntegration;
	}

	public InvoicePdf getInvoicePdfByInvoiceNumber(final String issuerLegalId, final String invoiceNumber, final InvoicePdfFilterRequest request, final String municipalityId) {
		var pdfEntity = pdfRepository.findOne(invoicePdfSpecifications.createInvoicesSpecification(request, invoiceNumber, issuerLegalId, municipalityId))
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, "PDF not found for invoiceNumber: " + invoiceNumber + ", issuerLegalId: " + issuerLegalId));

		// If a pdf was found, and it has not been truncated, read it from the database.
		if (pdfEntity.getTruncatedAt() == null) {
			return pdfMapper.mapToResponse(pdfEntity);
		}
		// If the pdf has been truncated, read it from storage using the file hash.
		var bytes = storageSambaIntegration.readFile(pdfEntity.getFileHash());
		return pdfMapper.mapToResponse(pdfEntity, bytes);
	}

	public InvoicePdf getRaindanceInvoicePdf(final String invoiceNumber, final String municipalityId) {
		var invoiceEntity = invoiceRepository.findFirstByInvoiceNumberAndMunicipalityId(invoiceNumber, municipalityId)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, "No invoice with invoice number '%s' was found".formatted(invoiceNumber)));

		return pdfRepository.findByInvoiceIdAndInvoiceIssuerLegalIdAndMunicipalityId(invoiceNumber, RAINDANCE_ISSUER_LEGAL_ID, municipalityId)
			.map(pdfMapper::mapToResponse)
			.orElseGet(() -> raindanceSambaIntegration.fetchInvoiceByFilename(invoiceEntity.getFileName()));
	}

	public String createOrUpdateInvoice(final InvoicePdfRequest request, final String municipalityId) {
		final var pdfEntity = pdfRepository.findByInvoiceNumberAndInvoiceIdAndMunicipalityId(request.invoiceNumber(), request.invoiceId(), municipalityId)
			.map(existingPdfEntity -> pdfMapper.mapOntoExistingEntity(existingPdfEntity, request))
			.orElseGet(() -> pdfMapper.mapToEntity(request, municipalityId));

		return pdfRepository.save(pdfEntity).getFilename();
	}

}
