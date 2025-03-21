package se.sundsvall.invoicecache.integration.db.specifications;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import se.sundsvall.invoicecache.api.model.InvoiceFilterRequest;
import se.sundsvall.invoicecache.integration.db.entity.InvoiceEntity;
import se.sundsvall.invoicecache.integration.db.entity.InvoiceEntity_;

@Component
public class InvoiceSpecifications {

	public Specification<InvoiceEntity> createInvoicesSpecification(final InvoiceFilterRequest request, final String municipalityId) {
		return (root, query, criteriaBuilder) -> {
			final List<Predicate> predicates = new ArrayList<>();

			if (StringUtils.isNotBlank(municipalityId)) {
				predicates.add(criteriaBuilder.equal(root.get(InvoiceEntity_.MUNICIPALITY_ID), municipalityId));
			}

			if (!CollectionUtils.isEmpty(request.getLegalIds())) {
				predicates.add(criteriaBuilder.in(root.get(InvoiceEntity_.ORGANIZATION_NUMBER)).value(request.getLegalIds()));
			}

			if (StringUtils.isNotBlank(request.getOcrNumber())) {
				predicates.add(criteriaBuilder.equal(root.get(InvoiceEntity_.ocrNumber), request.getOcrNumber()));
			}

			if (!CollectionUtils.isEmpty(request.getInvoiceNumbers())) {
				predicates.add(criteriaBuilder.in(root.get(InvoiceEntity_.INVOICE_NUMBER)).value(request.getInvoiceNumbers()));
			}

			// Not checking for valid dates since it has already been checked if we're here.
			if (request.getInvoiceDateFrom() != null) {
				predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get(InvoiceEntity_.invoiceDate), request.getInvoiceDateFrom()));
			}

			if (request.getInvoiceDateTo() != null) {
				predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get(InvoiceEntity_.invoiceDate), request.getInvoiceDateTo()));
			}

			if (request.getDueDateFrom() != null) {
				predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get(InvoiceEntity_.invoiceDueDate), request.getDueDateFrom()));
			}

			if (request.getDueDateTo() != null) {
				predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get(InvoiceEntity_.invoiceDueDate), request.getDueDateTo()));
			}

			return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		};
	}

}
