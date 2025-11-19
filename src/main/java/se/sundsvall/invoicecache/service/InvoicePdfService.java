package se.sundsvall.invoicecache.service;

import org.springframework.stereotype.Service;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;
import se.sundsvall.invoicecache.api.model.InvoicePdf;
import se.sundsvall.invoicecache.api.model.InvoicePdfFilterRequest;
import se.sundsvall.invoicecache.api.model.InvoicePdfRequest;
import se.sundsvall.invoicecache.integration.db.PdfRepository;
import se.sundsvall.invoicecache.integration.db.specifications.InvoicePdfSpecifications;
import se.sundsvall.invoicecache.integration.raindance.samba.RaindanceSambaIntegration;
import se.sundsvall.invoicecache.integration.storage.StorageSambaIntegration;
import se.sundsvall.invoicecache.service.mapper.PdfMapper;
import se.sundsvall.invoicecache.util.exception.InvoiceCacheException;

@Service
public class InvoicePdfService {

	private final PdfRepository pdfRepository;
	private final InvoicePdfSpecifications invoicePdfSpecifications;

	private final RaindanceSambaIntegration raindanceSambaIntegration;
	private final StorageSambaIntegration storageSambaIntegration;

	private final PdfMapper pdfMapper;

	public InvoicePdfService(final PdfRepository pdfRepository,
		final PdfMapper pdfMapper,
		final RaindanceSambaIntegration raindanceSambaIntegration,
		final InvoicePdfSpecifications invoicePdfSpecifications,
		final StorageSambaIntegration storageSambaIntegration) {
		this.pdfRepository = pdfRepository;
		this.pdfMapper = pdfMapper;
		this.raindanceSambaIntegration = raindanceSambaIntegration;
		this.invoicePdfSpecifications = invoicePdfSpecifications;
		this.storageSambaIntegration = storageSambaIntegration;
	}

	public InvoicePdf getInvoicePdfByFilename(final String filename, final String municipalityId) {
		try {
			return pdfRepository.findByFilenameAndMunicipalityId(filename, municipalityId)
				.map(entity -> {
					// If a pdf was found, and it has not been truncated, read it from the database.
					if (entity.getTruncatedAt() == null) {
						return pdfMapper.mapToResponse(entity);
					}
					// If the pdf has been truncated, read it from storage using the file hash.
					var bytes = storageSambaIntegration.readFile(entity.getFileHash());
					return pdfMapper.mapToResponse(entity, bytes);
				})
				// If no pdf was found in the database, try to find it in Raindance Samba.
				.orElseGet(() -> pdfMapper.mapToResponse(raindanceSambaIntegration.findPdf(filename, municipalityId)));
		} catch (final Exception e) {
			throw new InvoiceCacheException("Unable to get invoice PDF", e);
		}
	}

	public InvoicePdf getInvoicePdfByInvoiceNumber(final String issuerLegalId, final String invoiceNumber, final InvoicePdfFilterRequest request, final String municipalityId) {
		var pdfEntity = pdfRepository.findAll(invoicePdfSpecifications.createInvoicesSpecification(request, invoiceNumber, issuerLegalId, municipalityId))
			.stream().findFirst()
			.orElseThrow(() -> Problem.valueOf(Status.NOT_FOUND));

		// If a pdf was found, and it has not been truncated, read it from the database.
		if (pdfEntity.getTruncatedAt() == null) {
			return pdfMapper.mapToResponse(pdfEntity);
		}
		// If the pdf has been truncated, read it from storage using the file hash.
		var bytes = storageSambaIntegration.readFile(pdfEntity.getFileHash());
		return pdfMapper.mapToResponse(pdfEntity, bytes);
	}

	public String createOrUpdateInvoice(final InvoicePdfRequest request, final String municipalityId) {
		final var pdfEntity = pdfRepository.findByInvoiceNumberAndInvoiceIdAndMunicipalityId(request.invoiceNumber(), request.invoiceId(), municipalityId)
			.map(existingPdfEntity -> pdfMapper.mapOntoExistingEntity(existingPdfEntity, request))
			.orElseGet(() -> pdfMapper.mapToEntity(request, municipalityId));

		return pdfRepository.save(pdfEntity).getFilename();
	}

}
