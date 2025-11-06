package se.sundsvall.invoicecache.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import se.sundsvall.invoicecache.TestObjectFactory;
import se.sundsvall.invoicecache.api.model.Invoice;
import se.sundsvall.invoicecache.api.model.InvoiceStatus;
import se.sundsvall.invoicecache.api.model.InvoiceType;
import se.sundsvall.invoicecache.integration.db.entity.InvoiceEntity;

@ExtendWith(SoftAssertionsExtension.class)
class InvoiceMapperTest {

	private final InvoiceMapper mapper = new InvoiceMapper();

	@Test
	void testEntityToInvoice(final SoftAssertions softly) {
		final var entity = TestObjectFactory.generateInvoiceEntity();
		final var invoice = mapper.entityToInvoice(entity);

		softly.assertThat(invoice.getAmountVatExcluded()).isEqualByComparingTo(BigDecimal.valueOf(80).setScale(2, RoundingMode.HALF_EVEN));
		softly.assertThat(invoice.getCustomerName()).isEqualTo("Kalle Anka");
		softly.assertThat(invoice.getCustomerType()).isEqualTo("KA");
		softly.assertThat(invoice.getInvoiceDate()).isEqualTo(LocalDate.of(2022, 1, 2));
		softly.assertThat(invoice.getInvoiceDueDate()).isEqualTo(LocalDate.of(2022, 2, 3));
		softly.assertThat(invoice.getInvoiceNumber()).isEqualTo("12345678");
		softly.assertThat(invoice.getInvoiceStatus()).isEqualTo(InvoiceStatus.UNPAID);
		softly.assertThat(invoice.getOcrNumber()).isEqualTo("5566778899");
		softly.assertThat(invoice.getPartyId()).isNull();
		softly.assertThat(invoice.getPaidAmount()).isEqualTo(BigDecimal.valueOf(50).setScale(2, RoundingMode.HALF_EVEN));
		softly.assertThat(invoice.getTotalAmount()).isEqualByComparingTo(BigDecimal.valueOf(100).setScale(2, RoundingMode.HALF_EVEN));
		softly.assertThat(invoice.getInvoiceType()).isEqualTo(InvoiceType.INVOICE);

		softly.assertThat(invoice.getInvoiceAddress().getCareOf()).isEqualTo("c/o Knattarna");
		softly.assertThat(invoice.getInvoiceAddress().getCity()).isEqualTo("ANKEBORG");
		softly.assertThat(invoice.getInvoiceAddress().getStreet()).isEqualTo("Ankeborgsvägen 2");
		softly.assertThat(invoice.getInvoiceAddress().getPostcode()).isEqualTo("123 45");
	}

	@Test
	void testEntityWithNegativeAmount_shouldHaveInvoiceTypeCredit(final SoftAssertions softly) {
		final var entity = TestObjectFactory.generateInvoiceEntity();

		entity.setInvoiceAmount(BigDecimal.valueOf(-1).setScale(2, RoundingMode.HALF_EVEN));
		final var invoice = mapper.entityToInvoice(entity);
		softly.assertThat(invoice.getInvoiceType()).isEqualTo(InvoiceType.CREDIT_INVOICE);
	}

	@Test
	void testPaidNormalInvoice_shouldHavePositiveAmounts(final SoftAssertions softly) {
		final var entity = TestObjectFactory.generateInvoiceEntity();
		entity.setInvoiceAmount(BigDecimal.valueOf(100).setScale(2, RoundingMode.HALF_EVEN));
		entity.setPaidAmount(BigDecimal.valueOf(-100).setScale(2, RoundingMode.HALF_EVEN));
		final var invoice = mapper.entityToInvoice(entity);

		softly.assertThat(invoice.getTotalAmount()).isEqualByComparingTo(BigDecimal.valueOf(100).setScale(2, RoundingMode.HALF_EVEN));
		softly.assertThat(invoice.getPaidAmount()).isEqualByComparingTo(BigDecimal.valueOf(100).setScale(2, RoundingMode.HALF_EVEN));
	}

	@Test
	void testPartiallyPaidNormalInvoice_shouldHavePositiveAmounts(final SoftAssertions softly) {
		final var entity = TestObjectFactory.generateInvoiceEntity();
		entity.setInvoiceAmount(BigDecimal.valueOf(100).setScale(2, RoundingMode.HALF_EVEN));
		entity.setPaidAmount(BigDecimal.valueOf(-50).setScale(2, RoundingMode.HALF_EVEN));
		final var invoice = mapper.entityToInvoice(entity);

		softly.assertThat(invoice.getTotalAmount()).isEqualByComparingTo(BigDecimal.valueOf(100).setScale(2, RoundingMode.HALF_EVEN));
		softly.assertThat(invoice.getPaidAmount()).isEqualByComparingTo(BigDecimal.valueOf(50).setScale(2, RoundingMode.HALF_EVEN));
	}

	@Test
	void testNotPaidNormalInvoice_shouldHavePositiveAmount(final SoftAssertions softly) {
		final var entity = TestObjectFactory.generateInvoiceEntity();
		entity.setInvoiceAmount(BigDecimal.valueOf(100).setScale(2, RoundingMode.HALF_EVEN));
		entity.setPaidAmount(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN));
		final var invoice = mapper.entityToInvoice(entity);

		softly.assertThat(invoice.getTotalAmount()).isEqualByComparingTo(BigDecimal.valueOf(100).setScale(2, RoundingMode.HALF_EVEN));
		softly.assertThat(invoice.getPaidAmount()).isEqualByComparingTo(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN));
	}

	@Test
	void testPaidCreditedInvoice_shouldHavePositiveAmounts(final SoftAssertions softly) {
		final var entity = TestObjectFactory.generateInvoiceEntity();
		entity.setInvoiceAmount(BigDecimal.valueOf(-100).setScale(2, RoundingMode.HALF_EVEN));
		entity.setPaidAmount(BigDecimal.valueOf(100).setScale(2, RoundingMode.HALF_EVEN));
		final var invoice = mapper.entityToInvoice(entity);

		softly.assertThat(invoice.getTotalAmount()).isEqualByComparingTo(BigDecimal.valueOf(100).setScale(2, RoundingMode.HALF_EVEN));
		softly.assertThat(invoice.getPaidAmount()).isEqualByComparingTo(BigDecimal.valueOf(100).setScale(2, RoundingMode.HALF_EVEN));
	}

	@Test
	void testNotPaidCreditedInvoice_shouldHavePositiveAmount(final SoftAssertions softly) {
		final var entity = TestObjectFactory.generateInvoiceEntity();
		entity.setInvoiceAmount(BigDecimal.valueOf(-100).setScale(2, RoundingMode.HALF_EVEN));
		entity.setPaidAmount(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN));
		final var invoice = mapper.entityToInvoice(entity);

		softly.assertThat(invoice.getTotalAmount()).isEqualByComparingTo(BigDecimal.valueOf(100).setScale(2, RoundingMode.HALF_EVEN));
		softly.assertThat(invoice.getPaidAmount()).isEqualByComparingTo(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN));
	}

	@Test
	void testDetermineIfValidForReminder(final SoftAssertions softly) {
		for (final var value : InvoiceStatus.values()) {
			final var invoice = Invoice.builder().withInvoiceStatus(value).build();
			// Only UNPAID and PARTIALLY_PAID are valid for reminder status
			if (invoice.getInvoiceStatus() == InvoiceStatus.UNPAID || invoice.getInvoiceStatus() == InvoiceStatus.PARTIALLY_PAID) {
				softly.assertThat(mapper.invoiceHasStatusValidForReminder(invoice)).isTrue();
			} else {
				softly.assertThat(mapper.invoiceHasStatusValidForReminder(invoice)).isFalse();
			}
		}
	}

	@Test
	void testStatusShouldBeReminder_whenUnpaid_andReminderDateIsAfterInvoiceDueDate(final SoftAssertions softly) {
		// Create an invoice that will match the criteria for a reminder
		final var invoice = Invoice.builder()
			.withInvoiceStatus(InvoiceStatus.UNPAID)
			.withInvoiceReminderDate(LocalDate.now())
			.withInvoiceDate(LocalDate.now().minusDays(10L))
			.withInvoiceDueDate(LocalDate.now().minusDays(5L))
			.build();
		mapper.determineIfReminder(invoice);
		softly.assertThat(invoice.getInvoiceStatus()).isEqualTo(InvoiceStatus.REMINDER);
	}

	@Test
	void testStatusShouldBeSent_ifTodayIsBetweenInvoiceDateAndPrintDate(final SoftAssertions softly) {
		// Create an invoice that will match the criteria for a reminder
		final var invoice = Invoice.builder()
			.withInvoiceDate(LocalDate.now().plusDays(1L))
			.build();

		final var entity = InvoiceEntity.builder()
			.withInvoiceCreatedDate(LocalDate.now().minusDays(1L))
			.build();

		mapper.determineIfSent(invoice, entity);
		softly.assertThat(invoice.getInvoiceStatus()).isEqualTo(InvoiceStatus.SENT);
	}

	@Test
	void testStatusShouldNotBeSent_ifTodayIsNotBetweenInvoiceDateAndPrintDate(final SoftAssertions softly) {
		// Create an invoice that will match the criteria for a reminder
		final var invoice = Invoice.builder()
			.withInvoiceDate(LocalDate.now().plusDays(1L))
			.withInvoiceStatus(InvoiceStatus.UNPAID)    // Set to UNPAID to ensure it is not changed
			.build();

		final var entity = InvoiceEntity.builder()
			.withInvoiceCreatedDate(LocalDate.now().plusDays(2L))
			.build();

		mapper.determineIfSent(invoice, entity);
		softly.assertThat(invoice.getInvoiceStatus()).isEqualTo(InvoiceStatus.UNPAID);
	}
}
