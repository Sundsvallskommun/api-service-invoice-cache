package se.sundsvall.invoicecache.service;

import java.sql.SQLException;
import java.util.Base64;
import javax.sql.rowset.serial.SerialBlob;
import org.springframework.stereotype.Service;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;
import se.sundsvall.invoicecache.api.model.InvoicePdf;
import se.sundsvall.invoicecache.api.model.InvoicePdfFilterRequest;
import se.sundsvall.invoicecache.api.model.InvoicePdfRequest;
import se.sundsvall.invoicecache.integration.db.PdfRepository;
import se.sundsvall.invoicecache.integration.db.entity.PdfEntity;
import se.sundsvall.invoicecache.integration.db.specifications.InvoicePdfSpecifications;
import se.sundsvall.invoicecache.integration.smb.SMBIntegration;
import se.sundsvall.invoicecache.util.exception.InvoiceCacheException;

@Service
public class InvoicePdfService {

	private final PdfRepository pdfRepository;

	private final SMBIntegration smbIntegration;

	private final InvoicePdfSpecifications invoicePdfSpecifications;

	public InvoicePdfService(final PdfRepository pdfRepository,
		final SMBIntegration smbIntegration, final InvoicePdfSpecifications invoicePdfSpecifications) {
		this.pdfRepository = pdfRepository;
		this.smbIntegration = smbIntegration;
		this.invoicePdfSpecifications = invoicePdfSpecifications;
	}

	public InvoicePdf getInvoicePdf(final String filename, final String municipalityId) {

		try {
			final var result = pdfRepository.findByFilenameAndMunicipalityId(filename, municipalityId)
				.map(this::mapToResponse)
				.orElseGet(() -> mapToResponse(smbIntegration.findPdf(filename, municipalityId)));
			if (result == null) {
				throw Problem.valueOf(Status.NOT_FOUND);
			}
			return result;
		} catch (final Exception e) {
			throw new InvoiceCacheException("Unable to get invoice PDF", e);
		}
	}

	public InvoicePdf getInvoicePdfByInvoiceNumber(final String issuerLegalId, final String invoiceNumber,
		final InvoicePdfFilterRequest request, final String municipalityId) {
		return pdfRepository.findAll(invoicePdfSpecifications
			.createInvoicesSpecification(request, invoiceNumber, issuerLegalId, municipalityId))
			.stream().findFirst()
			.map(this::mapToResponse)
			.orElseThrow(() -> Problem.valueOf(Status.NOT_FOUND));

	}

	public String createOrUpdateInvoice(final InvoicePdfRequest request, final String municipalityId) {
		final var pdfEntity = pdfRepository.findByInvoiceNumberAndInvoiceIdAndMunicipalityId(request.invoiceNumber(), request.invoiceId(), municipalityId)
			.map(existingPdfEntity -> mapOntoExistingEntity(existingPdfEntity, request))
			.orElseGet(() -> mapToEntity(request, municipalityId));

		return pdfRepository.save(pdfEntity).getFilename();
	}

	PdfEntity mapToEntity(final InvoicePdfRequest request, final String municipalityId) {
		try {
			final var document = new SerialBlob(Base64.getDecoder().decode(request.attachment().content()));

			return PdfEntity.builder()
				.withMunicipalityId(municipalityId)
				.withInvoiceIssuerLegalId(request.issuerLegalId())
				.withInvoiceDebtorLegalId(request.debtorLegalId())
				.withInvoiceNumber(request.invoiceNumber())
				.withInvoiceId(request.invoiceId())
				.withInvoiceType(request.invoiceType())
				.withFilename(request.attachment().name())
				.withDocument(document)
				.build();
		} catch (final SQLException e) {
			throw new InvoiceCacheException("Unable to map PDF data", e);
		}
	}

	PdfEntity mapOntoExistingEntity(final PdfEntity pdfEntity, final InvoicePdfRequest request) {
		try {
			pdfEntity.setInvoiceIssuerLegalId(request.issuerLegalId());
			pdfEntity.setInvoiceDebtorLegalId(request.debtorLegalId());
			pdfEntity.setInvoiceNumber(request.invoiceNumber());
			pdfEntity.setInvoiceId(request.invoiceId());
			pdfEntity.setInvoiceType(request.invoiceType());
			pdfEntity.setFilename(request.attachment().name());
			pdfEntity.setDocument(new SerialBlob(Base64.getDecoder().decode(request.attachment().content())));

			return pdfEntity;
		} catch (final SQLException e) {
			throw new InvoiceCacheException("Unable to map PDF data onto existing invoice", e);
		}
	}

	InvoicePdf mapToResponse(final PdfEntity entity) {
		try {
			final var bytes = entity.getDocument().getBytes(1, (int) entity.getDocument().length());

			return InvoicePdf.builder()
				.withName(entity.getFilename())
				.withContent(Base64.getEncoder().encodeToString(bytes))
				.build();
		} catch (final SQLException e) {
			throw new InvoiceCacheException("Unable to map response", e);
		}
	}

}
