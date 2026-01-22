package se.sundsvall.invoicecache.integration.db.specifications;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import se.sundsvall.invoicecache.api.model.InvoicePdfFilterRequest;
import se.sundsvall.invoicecache.integration.db.entity.PdfEntity;
import se.sundsvall.invoicecache.integration.db.entity.PdfEntity_;

@Component
public class InvoicePdfSpecifications {

	public Specification<PdfEntity> createInvoicesSpecification(final InvoicePdfFilterRequest request,
		final String invoiceNumber, final String issuerLegalId, final String municipalityId) {
		return (root, query, criteriaBuilder) -> {
			final var predicates = new ArrayList<Predicate>();

			if (StringUtils.isNotBlank(municipalityId)) {
				predicates.add(criteriaBuilder.equal(root.get(PdfEntity_.MUNICIPALITY_ID), municipalityId));
			}

			if (StringUtils.isNotBlank(issuerLegalId)) {
				predicates.add(criteriaBuilder.in(root.get(PdfEntity_.INVOICE_ISSUER_LEGAL_ID)).value(issuerLegalId));
			}

			if (StringUtils.isNotBlank(request.getDebtorLegalId())) {
				predicates.add(criteriaBuilder.equal(root.get(PdfEntity_.INVOICE_DEBTOR_LEGAL_ID), request.getDebtorLegalId()));
			}

			if (StringUtils.isNotBlank(invoiceNumber)) {
				predicates.add(criteriaBuilder.in(root.get(PdfEntity_.INVOICE_NUMBER)).value(invoiceNumber));
			}

			if (StringUtils.isNotBlank(request.getInvoiceId())) {
				predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get(PdfEntity_.INVOICE_ID), request.getInvoiceId()));
			}

			if (StringUtils.isNotBlank(request.getInvoiceFileName())) {
				predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get(PdfEntity_.FILENAME), request.getInvoiceFileName()));
			}

			if (request.getInvoiceType() != null) {
				predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get(PdfEntity_.INVOICE_TYPE), request.getInvoiceType()));
			}

			if (query != null) {
				query.orderBy(criteriaBuilder.desc(root.get(PdfEntity_.CREATED)));
			}

			return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		};
	}

}
