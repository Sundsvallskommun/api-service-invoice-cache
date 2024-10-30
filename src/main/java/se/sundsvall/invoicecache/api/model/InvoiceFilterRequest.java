package se.sundsvall.invoicecache.api.model;

import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Invoice filter request model")
public class InvoiceFilterRequest extends ParameterBase {

	@DateTimeFormat(iso = ISO.DATE)
	@Schema(description = "Fetch invoices from (and including) this date.", example = "2022-01-01")
	private LocalDate invoiceDateFrom;

	@DateTimeFormat(iso = ISO.DATE)
	@Schema(description = "Fetch invoices up to (and including) this date, includes invoices to this date", example = "2022-12-31")
	private LocalDate invoiceDateTo;

	@DateTimeFormat(iso = ISO.DATE)
	@Schema(description = "Fetch invoices that are due from (and including) this date.", example = "2022-01-01")
	private LocalDate dueDateFrom;

	@DateTimeFormat(iso = ISO.DATE)
	@Schema(description = "Fetch invoices that are due up to (and including) this date", example = "2022-12-31")
	private LocalDate dueDateTo;

	@Schema(description = "List of partyIds for organizations or private firms", example = "[\"fb2f0290-3820-11ed-a261-0242ac120002\", \"fb2f0290-3820-11ed-a261-0242ac120003\"]")
	private List<String> partyIds;

	@JsonIgnore // Used internally since we store legalIds in the DB
	private List<String> legalIds;

	@Schema(description = "Ocr number for the invoice", example = "8907131234")
	private String ocrNumber;

	@Schema(description = "Invoice numbers to fetch", example = "[\"1234567\", \"2345678\"]")
	private List<String> invoiceNumbers;
}
