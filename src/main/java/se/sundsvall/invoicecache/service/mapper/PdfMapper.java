package se.sundsvall.invoicecache.service.mapper;

import java.util.Base64;
import javax.sql.rowset.serial.SerialBlob;
import org.springframework.stereotype.Component;
import se.sundsvall.invoicecache.api.model.InvoicePdf;
import se.sundsvall.invoicecache.api.model.InvoicePdfRequest;
import se.sundsvall.invoicecache.integration.db.entity.PdfEntity;
import se.sundsvall.invoicecache.util.exception.InvoiceCacheException;

@Component
public class PdfMapper {

	public PdfEntity mapToEntity(final InvoicePdfRequest request, final String municipalityId) {
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
		} catch (final Exception e) {
			throw new InvoiceCacheException("Unable to map PDF data", e);
		}
	}

	public PdfEntity mapOntoExistingEntity(final PdfEntity pdfEntity, final InvoicePdfRequest request) {
		try {
			pdfEntity.setInvoiceIssuerLegalId(request.issuerLegalId());
			pdfEntity.setInvoiceDebtorLegalId(request.debtorLegalId());
			pdfEntity.setInvoiceNumber(request.invoiceNumber());
			pdfEntity.setInvoiceId(request.invoiceId());
			pdfEntity.setInvoiceType(request.invoiceType());
			pdfEntity.setFilename(request.attachment().name());
			pdfEntity.setDocument(new SerialBlob(Base64.getDecoder().decode(request.attachment().content())));

			return pdfEntity;
		} catch (final Exception e) {
			throw new InvoiceCacheException("Unable to map PDF data onto existing invoice", e);
		}
	}

	public InvoicePdf mapToResponse(final PdfEntity entity) {
		try {
			final var bytes = entity.getDocument().getBytes(1, (int) entity.getDocument().length());

			return InvoicePdf.builder()
				.withName(entity.getFilename())
				.withContent(Base64.getEncoder().encodeToString(bytes))
				.build();
		} catch (final Exception e) {
			throw new InvoiceCacheException("Unable to map response", e);
		}
	}

	public InvoicePdf mapToResponse(final PdfEntity entity, final byte[] bytes) {
		return InvoicePdf.builder()
			.withName(entity.getFilename())
			.withContent(Base64.getEncoder().encodeToString(bytes))
			.build();
	}
}
