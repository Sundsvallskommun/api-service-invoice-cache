package se.sundsvall.invoicecache.api.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class InvoiceTypeTest {

	@Test
	void testValuesHaveNotChanged() {
		assertThat(InvoiceType.values()).hasSize(7);

		assertThat(InvoiceType.INVOICE.getType()).isEqualTo("Faktura");
		assertThat(InvoiceType.CREDIT_INVOICE.getType()).isEqualTo("Kreditfaktura");
		assertThat(InvoiceType.DIRECT_DEBIT.getType()).isEqualTo("Autogiro");
		assertThat(InvoiceType.SELF_INVOICE.getType()).isEqualTo("Självfaktura");
		assertThat(InvoiceType.REMINDER.getType()).isEqualTo("Påminnelse");
		assertThat(InvoiceType.CONSOLIDATED_INVOICE.getType()).isEqualTo("Samlingsfaktura");
		assertThat(InvoiceType.FINAL_INVOICE.getType()).isEqualTo("Slutfaktura");
	}
}
