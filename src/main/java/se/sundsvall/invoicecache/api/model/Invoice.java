package se.sundsvall.invoicecache.api.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(setterPrefix = "with")
@Schema(description = "Model for the Invoice")
public class Invoice {

	@Schema(description = "Customer name", example = "Fritjofs Blommor")
	private String customerName;

	@Schema(description = "Customer type", example = "XH")
	private String customerType;

	@Schema(description = "Invoice number", example = "53489546")
	private String invoiceNumber;

	@Schema(description = "Invoice status", example = "PAID")
	private InvoiceStatus invoiceStatus;

	@Schema(description = "OCR Number", example = "8907136421")
	private String ocrNumber;

	@Schema(description = "PartyId for organization or private firm", example = "fb2f0290-3820-11ed-a261-0242ac120002")
	private String partyId;

	@JsonIgnore
	private String legalId;

	@Schema(description = "Invoice description", example = "Barn och Utbildning")
	private String invoiceDescription;

	@Schema(description = "When the invoice is due to be paid", example = "2022-05-05")
	private LocalDate invoiceDueDate;

	@Schema(description = "When the invoice was created", example = "2022-04-05")
	private LocalDate invoiceDate;

	@Schema(description = "When a reminder was sent out", example = "2022-04-05")
	private LocalDate invoiceReminderDate;

	@Schema(description = "Amount already paid on the invoice.", example = "4995.00")
	private BigDecimal paidAmount;

	@Schema(description = "Amount to pay including VAT (SEK)", example = "4995.00")
	private BigDecimal totalAmount;

	@Schema(description = "Filename of the invoice, if it is present", example = "Faktura_5555555_to_9988776655.pdf")
	private String invoiceFileName;

	@Schema(description = "Vat amount in SEK", example = "999.00")
	private BigDecimal vat;

	@Schema(description = "Amount without vat", example = "3996.00")
	private BigDecimal amountVatExcluded;

	@Schema(description = "Invoice type", example = "NORMAL or CREDIT", implementation = InvoiceType.class)
	private InvoiceType invoiceType;

	@Schema(implementation = Address.class)
	private Address invoiceAddress;
}
