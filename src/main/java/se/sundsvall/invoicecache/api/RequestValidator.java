package se.sundsvall.invoicecache.api;

import static org.zalando.problem.Status.BAD_REQUEST;

import java.time.LocalDate;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;
import org.zalando.problem.Problem;
import se.sundsvall.invoicecache.api.model.InvoiceFilterRequest;

public class RequestValidator {

	private RequestValidator() {}

	public static void validateRequest(InvoiceFilterRequest request) {
		checkMandatoryFields(request);
		checkDates(request.getDueDateFrom(), request.getDueDateTo());
		checkDates(request.getInvoiceDateFrom(), request.getInvoiceDateTo());
	}

	static void checkDates(LocalDate from, LocalDate to) {
		// We only check if both are set. if you only set one, that's ok.
		if ((from != null) && (to != null) && to.isBefore(from)) {
			throw Problem.builder()
				.withTitle("To-date is before From-date.")
				.withStatus(BAD_REQUEST)
				.build();
		}
	}

	/**
	 * Make sure that at least one of organizationNumber, ocrNumber or one invoiceNumber is set.
	 */
	static void checkMandatoryFields(InvoiceFilterRequest request) {

		// do we have any of legalId or ocrNumber
		final boolean hasValidOcrNumber = StringUtils.isNotBlank(request.getOcrNumber());
		final boolean hasValidLegalIds = stringListHasValidContent(request.getPartyIds());
		final boolean hasValidInvoiceNumbers = stringListHasValidContent(request.getInvoiceNumbers());

		if (!(hasValidOcrNumber || hasValidLegalIds || hasValidInvoiceNumbers)) {
			throw Problem.builder()
				.withTitle("One of partyIds, invoiceNumbers or ocrNumber needs to be set.")
				.withStatus(BAD_REQUEST)
				.build();
		}
	}

	private static boolean stringListHasValidContent(List<String> listToCheck) {
		if (!CollectionUtils.isEmpty(listToCheck)) {
			return listToCheck.stream()
				.allMatch(StringUtils::isNotBlank);
		}

		return false;
	}
}
