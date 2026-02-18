package se.sundsvall.invoicecache.api.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class InvoiceStatusTest {

	@Test
	void testValuesHaveNotChanged() {
		assertThat(InvoiceStatus.PAID.getStatus()).isEqualTo("Betald");
		assertThat(InvoiceStatus.UNPAID.getStatus()).isEqualTo("Obetald");
		assertThat(InvoiceStatus.PARTIALLY_PAID.getStatus()).isEqualTo("Delvis betald");
		assertThat(InvoiceStatus.DEBT_COLLECTION.getStatus()).isEqualTo("Gått till inkasso");
		assertThat(InvoiceStatus.PAID_TOO_MUCH.getStatus()).isEqualTo("För mycket betalt");
		assertThat(InvoiceStatus.REMINDER.getStatus()).isEqualTo("Påminnelse");
		assertThat(InvoiceStatus.SENT.getStatus()).isEqualTo("Skickad");
		assertThat(InvoiceStatus.VOID.getStatus()).isEqualTo("Makulerad");
		assertThat(InvoiceStatus.UNKNOWN.getStatus()).isEqualTo("Okänd");
		assertThat(InvoiceStatus.values()).hasSize(9);
	}

	@Test
	void testUnknownStatusShouldReturnUnknown() {
		assertThat(InvoiceStatus.fromValue("something Else")).isEqualTo(InvoiceStatus.UNKNOWN);
	}
}
