package se.sundsvall.invoicecache.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;
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

	@Schema(description = "Customer name", examples = "Fritjofs Blommor")
	private String customerName;

	@Schema(description = "Customer type", examples = "XH")
	private String customerType;

	@Schema(description = "Invoice number", examples = "53489546")
	private String invoiceNumber;

	@Schema(description = "Invoice status", examples = "PAID")
	private InvoiceStatus invoiceStatus;

	@Schema(description = "OCR Number", examples = "8907136421")
	private String ocrNumber;

	@Schema(description = "PartyId for organization or private firm", examples = "fb2f0290-3820-11ed-a261-0242ac120002")
	private String partyId;

	@JsonIgnore
	private String legalId;

	@Schema(description = "Invoice description", examples = "Barn och Utbildning")
	private String invoiceDescription;

	@Schema(description = "When the invoice is due to be paid", examples = "2022-05-05")
	private LocalDate invoiceDueDate;

	@Schema(description = "When the invoice was created", examples = "2022-04-05")
	private LocalDate invoiceDate;

	@Schema(description = "When a reminder was sent out", examples = "2022-04-05")
	private LocalDate invoiceReminderDate;

	@Schema(description = "Amount already paid on the invoice.", examples = "4995.00")
	private BigDecimal paidAmount;

	@Schema(description = "Amount to pay including VAT (SEK)", examples = "4995.00")
	private BigDecimal totalAmount;

	@Schema(description = "Filename of the invoice, if it is present", examples = "Faktura_5555555_to_9988776655.pdf")
	private String invoiceFileName;

	@Schema(description = "Vat amount in SEK", examples = "999.00")
	private BigDecimal vat;

	@Schema(description = "Amount without vat", examples = "3996.00")
	private BigDecimal amountVatExcluded;

	@Schema(description = "Invoice type", examples = "NORMAL or CREDIT", implementation = InvoiceType.class)
	private InvoiceType invoiceType;

	@Schema(implementation = Address.class)
	private Address invoiceAddress;
}
