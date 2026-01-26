package se.sundsvall.invoicecache.service;

import static org.zalando.problem.Status.INTERNAL_SERVER_ERROR;
import static org.zalando.problem.Status.NOT_FOUND;
import static se.sundsvall.invoicecache.Constant.RAINDANCE_ISSUER_LEGAL_ID;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ContentDisposition;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.zalando.problem.Problem;
import se.sundsvall.invoicecache.api.model.InvoicePdf;
import se.sundsvall.invoicecache.api.model.InvoicePdfFilterRequest;
import se.sundsvall.invoicecache.api.model.InvoicePdfRequest;
import se.sundsvall.invoicecache.api.model.InvoicePdfStreamData;
import se.sundsvall.invoicecache.integration.db.InvoiceRepository;
import se.sundsvall.invoicecache.integration.db.PdfRepository;
import se.sundsvall.invoicecache.integration.db.entity.PdfEntity;
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
		// Find all, with paged request, to limit to one result. It automatically sorts by created desc, so we will get the
		// latest.
		final var pdfEntity = pdfRepository.findAll(invoicePdfSpecifications.createInvoicesSpecification(request, invoiceNumber, issuerLegalId, municipalityId), PageRequest.of(0, 1))
			.stream()
			.findFirst()
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, "PDF not found for invoiceNumber: " + invoiceNumber + ", issuerLegalId: " + issuerLegalId));

		// If a pdf was found, and it has not been truncated, read it from the database.
		if (pdfEntity.getTruncatedAt() == null) {
			return pdfMapper.mapToResponse(pdfEntity);
		}
		// If the pdf has been truncated, read it from storage using the file hash.
		final var bytes = storageSambaIntegration.readFile(pdfEntity.getFileHash());
		return pdfMapper.mapToResponse(pdfEntity, bytes);
	}

	public InvoicePdf getRaindanceInvoicePdf(final String invoiceNumber, final String municipalityId) {
		final var invoiceEntity = invoiceRepository.findFirstByInvoiceNumberAndMunicipalityId(invoiceNumber, municipalityId)
			.orElseThrow(() -> Problem.valueOf(NOT_FOUND, "No invoice with invoice number '%s' was found".formatted(invoiceNumber)));

		try {
			return raindanceSambaIntegration.fetchInvoiceByFilename(invoiceEntity.getFileName());
		} catch (final Exception e) {
			return pdfRepository.findByInvoiceIdAndInvoiceIssuerLegalIdAndMunicipalityId(invoiceNumber, RAINDANCE_ISSUER_LEGAL_ID, municipalityId)
				.map(pdfMapper::mapToResponse)
				.orElseThrow(() -> Problem.valueOf(NOT_FOUND, "PDF not found for invoiceNumber: " + invoiceNumber));
		}
	}

	public String createOrUpdateInvoice(final InvoicePdfRequest request, final String municipalityId) {
		final var pdfEntity = pdfRepository.findByInvoiceNumberAndInvoiceIdAndMunicipalityId(request.invoiceNumber(), request.invoiceId(), municipalityId)
			.map(existingPdfEntity -> pdfMapper.mapOntoExistingEntity(existingPdfEntity, request))
			.orElseGet(() -> pdfMapper.mapToEntity(request, municipalityId));

		return pdfRepository.save(pdfEntity).getFilename();
	}

	public InvoicePdfStreamData getInvoicePdfsAsStream(final String issuerLegalId, final String invoiceNumber, final InvoicePdfFilterRequest request, final String municipalityId) {
		final var specification = invoicePdfSpecifications.createInvoicesSpecification(request, invoiceNumber, issuerLegalId, municipalityId);
		final var pdfEntities = pdfRepository.findAll(specification, Sort.by(Sort.Direction.DESC, "created"));

		if (pdfEntities.isEmpty()) {
			throw Problem.valueOf(NOT_FOUND, "No PDFs found for invoiceNumber: " + invoiceNumber + ", issuerLegalId: " + issuerLegalId);
		}

		if (pdfEntities.size() == 1) {
			return createSinglePdfStream(pdfEntities.getFirst(), invoiceNumber);
		}

		return createZipStream(pdfEntities, invoiceNumber);
	}

	private InvoicePdfStreamData createSinglePdfStream(final PdfEntity pdfEntity, final String invoiceNumber) {
		final var bytes = getPdfBytes(pdfEntity);
		final var filename = pdfEntity.getFilename() != null ? pdfEntity.getFilename() : "invoice_" + invoiceNumber + ".pdf";

		return new InvoicePdfStreamData(
			outputStream -> outputStream.write(bytes),
			MediaType.APPLICATION_PDF,
			ContentDisposition.attachment().filename(filename).build());
	}

	private InvoicePdfStreamData createZipStream(final List<PdfEntity> pdfEntities, final String invoiceNumber) {
		return new InvoicePdfStreamData(
			outputStream -> {
				try (final var zipOut = new ZipOutputStream(outputStream)) {
					final var counter = new AtomicInteger(0);
					for (final var pdfEntity : pdfEntities) {
						final var bytes = getPdfBytes(pdfEntity);
						final var filename = getUniqueFilename(pdfEntity, invoiceNumber, counter.getAndIncrement());
						final var entry = new ZipEntry(filename);
						zipOut.putNextEntry(entry);
						zipOut.write(bytes);
						zipOut.closeEntry();
					}
				} catch (final IOException e) {
					throw Problem.valueOf(INTERNAL_SERVER_ERROR, "Failed to create zip file for invoiceNumber: " + invoiceNumber);
				}
			},
			MediaType.parseMediaType("application/zip"),
			ContentDisposition.attachment().filename("invoices_" + invoiceNumber + ".zip").build());
	}

	private byte[] getPdfBytes(final PdfEntity pdfEntity) {
		if (pdfEntity.getTruncatedAt() == null) {
			return pdfMapper.extractBytes(pdfEntity);
		}
		return storageSambaIntegration.readFile(pdfEntity.getFileHash());
	}

	private String getUniqueFilename(final PdfEntity pdfEntity, final String invoiceNumber, final int index) {
		if (pdfEntity.getFilename() != null) {
			if (index == 0) {
				return pdfEntity.getFilename();
			}
			final var lastDot = pdfEntity.getFilename().lastIndexOf('.');
			if (lastDot > 0) {
				return pdfEntity.getFilename().substring(0, lastDot) + "_" + index + pdfEntity.getFilename().substring(lastDot);
			}
			return pdfEntity.getFilename() + "_" + index;
		}
		return "invoice_" + invoiceNumber + "_" + index + ".pdf";
	}

}
