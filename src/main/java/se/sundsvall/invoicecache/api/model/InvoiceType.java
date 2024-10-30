package se.sundsvall.invoicecache.api.model;

import java.math.BigDecimal;

public enum InvoiceType {

	INVOICE("Faktura"),
	CREDIT_INVOICE("Kreditfaktura"),
	DIRECT_DEBIT("Autogiro"),
	SELF_INVOICE("Självfaktura"),
	REMINDER("Påminnelse"),
	FINAL_INVOICE("Slutfaktura"),
	CONSOLIDATED_INVOICE("Samlingsfaktura");

	InvoiceType(final String type) {
		this.type = type;
	}

	private final String type;

	public String getType() {
		return type;
	}

	public static InvoiceType fromInvoiceAmount(BigDecimal amount) {
		if (amount.signum() == 1) {
			return InvoiceType.INVOICE;
		} else {
			return InvoiceType.CREDIT_INVOICE;
		}
	}
}
