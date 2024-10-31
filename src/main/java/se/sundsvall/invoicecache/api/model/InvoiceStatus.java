package se.sundsvall.invoicecache.api.model;

public enum InvoiceStatus {

	PAID("Betald"),
	UNPAID("Obetald"),
	PARTIALLY_PAID("Delvis betald"),
	DEBT_COLLECTION("Gått till inkasso"),
	PAID_TOO_MUCH("För mycket betalt"),
	REMINDER("Påminnelse"),
	SENT("Skickad"),
	VOID("Makulerad"),
	UNKNOWN("Okänd");

	InvoiceStatus(final String status) {
		this.status = status;
	}

	private final String status;

	public String getStatus() {
		return status;
	}

	public static InvoiceStatus fromValue(String value) {
		for (InvoiceStatus status : InvoiceStatus.values()) {
			if (status.status.equals(value)) {
				return status;
			}
		}

		return UNKNOWN;
	}

}
