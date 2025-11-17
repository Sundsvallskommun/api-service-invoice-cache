package se.sundsvall.invoicecache.service.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import se.sundsvall.invoicecache.TestObjectFactory;
import se.sundsvall.invoicecache.api.model.Invoice;
import se.sundsvall.invoicecache.api.model.InvoiceStatus;
import se.sundsvall.invoicecache.api.model.InvoiceType;
import se.sundsvall.invoicecache.integration.db.entity.InvoiceEntity;

class InvoiceMapperTest {

	private final InvoiceMapper mapper = new InvoiceMapper();

	@Test
	void testEntityToInvoice() {
		final var entity = TestObjectFactory.generateInvoiceEntity();
		final var invoice = mapper.entityToInvoice(entity);

		assertThat(invoice.getAmountVatExcluded()).isEqualByComparingTo(BigDecimal.valueOf(80).setScale(2, RoundingMode.HALF_EVEN));
		assertThat(invoice.getCustomerName()).isEqualTo("Kalle Anka");
		assertThat(invoice.getCustomerType()).isEqualTo("KA");
		assertThat(invoice.getInvoiceDate()).isEqualTo(LocalDate.of(2022, 1, 2));
		assertThat(invoice.getInvoiceDueDate()).isEqualTo(LocalDate.of(2022, 2, 3));
		assertThat(invoice.getInvoiceNumber()).isEqualTo("12345678");
		assertThat(invoice.getInvoiceStatus()).isEqualTo(InvoiceStatus.UNPAID);
		assertThat(invoice.getOcrNumber()).isEqualTo("5566778899");
		assertThat(invoice.getPartyId()).isNull();
		assertThat(invoice.getPaidAmount()).isEqualTo(BigDecimal.valueOf(50).setScale(2, RoundingMode.HALF_EVEN));
		assertThat(invoice.getTotalAmount()).isEqualByComparingTo(BigDecimal.valueOf(100).setScale(2, RoundingMode.HALF_EVEN));
		assertThat(invoice.getInvoiceType()).isEqualTo(InvoiceType.INVOICE);

		assertThat(invoice.getInvoiceAddress().getCareOf()).isEqualTo("c/o Knattarna");
		assertThat(invoice.getInvoiceAddress().getCity()).isEqualTo("ANKEBORG");
		assertThat(invoice.getInvoiceAddress().getStreet()).isEqualTo("Ankeborgsvägen 2");
		assertThat(invoice.getInvoiceAddress().getPostcode()).isEqualTo("123 45");
	}

	@Test
	void testEntityWithNegativeAmount_shouldHaveInvoiceTypeCredit() {
		final var entity = TestObjectFactory.generateInvoiceEntity();

		entity.setInvoiceAmount(BigDecimal.valueOf(-1).setScale(2, RoundingMode.HALF_EVEN));
		final var invoice = mapper.entityToInvoice(entity);
		assertThat(invoice.getInvoiceType()).isEqualTo(InvoiceType.CREDIT_INVOICE);
	}

	@Test
	void testPaidNormalInvoice_shouldHavePositiveAmounts() {
		final var entity = TestObjectFactory.generateInvoiceEntity();
		entity.setInvoiceAmount(BigDecimal.valueOf(100).setScale(2, RoundingMode.HALF_EVEN));
		entity.setPaidAmount(BigDecimal.valueOf(-100).setScale(2, RoundingMode.HALF_EVEN));
		final var invoice = mapper.entityToInvoice(entity);

		assertThat(invoice.getTotalAmount()).isEqualByComparingTo(BigDecimal.valueOf(100).setScale(2, RoundingMode.HALF_EVEN));
		assertThat(invoice.getPaidAmount()).isEqualByComparingTo(BigDecimal.valueOf(100).setScale(2, RoundingMode.HALF_EVEN));
	}

	@Test
	void testPartiallyPaidNormalInvoice_shouldHavePositiveAmounts() {
		final var entity = TestObjectFactory.generateInvoiceEntity();
		entity.setInvoiceAmount(BigDecimal.valueOf(100).setScale(2, RoundingMode.HALF_EVEN));
		entity.setPaidAmount(BigDecimal.valueOf(-50).setScale(2, RoundingMode.HALF_EVEN));
		final var invoice = mapper.entityToInvoice(entity);

		assertThat(invoice.getTotalAmount()).isEqualByComparingTo(BigDecimal.valueOf(100).setScale(2, RoundingMode.HALF_EVEN));
		assertThat(invoice.getPaidAmount()).isEqualByComparingTo(BigDecimal.valueOf(50).setScale(2, RoundingMode.HALF_EVEN));
	}

	@Test
	void testNotPaidNormalInvoice_shouldHavePositiveAmount() {
		final var entity = TestObjectFactory.generateInvoiceEntity();
		entity.setInvoiceAmount(BigDecimal.valueOf(100).setScale(2, RoundingMode.HALF_EVEN));
		entity.setPaidAmount(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN));
		final var invoice = mapper.entityToInvoice(entity);

		assertThat(invoice.getTotalAmount()).isEqualByComparingTo(BigDecimal.valueOf(100).setScale(2, RoundingMode.HALF_EVEN));
		assertThat(invoice.getPaidAmount()).isEqualByComparingTo(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN));
	}

	@Test
	void testPaidCreditedInvoice_shouldHavePositiveAmounts() {
		final var entity = TestObjectFactory.generateInvoiceEntity();
		entity.setInvoiceAmount(BigDecimal.valueOf(-100).setScale(2, RoundingMode.HALF_EVEN));
		entity.setPaidAmount(BigDecimal.valueOf(100).setScale(2, RoundingMode.HALF_EVEN));
		final var invoice = mapper.entityToInvoice(entity);

		assertThat(invoice.getTotalAmount()).isEqualByComparingTo(BigDecimal.valueOf(100).setScale(2, RoundingMode.HALF_EVEN));
		assertThat(invoice.getPaidAmount()).isEqualByComparingTo(BigDecimal.valueOf(100).setScale(2, RoundingMode.HALF_EVEN));
	}

	@Test
	void testNotPaidCreditedInvoice_shouldHavePositiveAmount() {
		final var entity = TestObjectFactory.generateInvoiceEntity();
		entity.setInvoiceAmount(BigDecimal.valueOf(-100).setScale(2, RoundingMode.HALF_EVEN));
		entity.setPaidAmount(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN));
		final var invoice = mapper.entityToInvoice(entity);

		assertThat(invoice.getTotalAmount()).isEqualByComparingTo(BigDecimal.valueOf(100).setScale(2, RoundingMode.HALF_EVEN));
		assertThat(invoice.getPaidAmount()).isEqualByComparingTo(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN));
	}

	@Test
	void testDetermineIfValidForReminder() {
		for (final var value : InvoiceStatus.values()) {
			final var invoice = Invoice.builder().withInvoiceStatus(value).build();
			// Only UNPAID and PARTIALLY_PAID are valid for reminder status
			if (invoice.getInvoiceStatus() == InvoiceStatus.UNPAID || invoice.getInvoiceStatus() == InvoiceStatus.PARTIALLY_PAID) {
				assertThat(mapper.invoiceHasStatusValidForReminder(invoice)).isTrue();
			} else {
				assertThat(mapper.invoiceHasStatusValidForReminder(invoice)).isFalse();
			}
		}
	}

	@Test
	void testStatusShouldBeReminder_whenUnpaid_andReminderDateIsAfterInvoiceDueDate() {
		// Create an invoice that will match the criteria for a reminder
		final var invoice = Invoice.builder()
			.withInvoiceStatus(InvoiceStatus.UNPAID)
			.withInvoiceReminderDate(LocalDate.now())
			.withInvoiceDate(LocalDate.now().minusDays(10L))
			.withInvoiceDueDate(LocalDate.now().minusDays(5L))
			.build();
		mapper.determineIfReminder(invoice);
		assertThat(invoice.getInvoiceStatus()).isEqualTo(InvoiceStatus.REMINDER);
	}

	@Test
	void testStatusShouldBeSent_ifTodayIsBetweenInvoiceDateAndPrintDate() {
		// Create an invoice that will match the criteria for a reminder
		final var invoice = Invoice.builder()
			.withInvoiceDate(LocalDate.now().plusDays(1L))
			.build();

		final var entity = InvoiceEntity.builder()
			.withInvoiceCreatedDate(LocalDate.now().minusDays(1L))
			.build();

		mapper.determineIfSent(invoice, entity);
		assertThat(invoice.getInvoiceStatus()).isEqualTo(InvoiceStatus.SENT);
	}

	@Test
	void testStatusShouldNotBeSent_ifTodayIsNotBetweenInvoiceDateAndPrintDate() {
		// Create an invoice that will match the criteria for a reminder
		final var invoice = Invoice.builder()
			.withInvoiceDate(LocalDate.now().plusDays(1L))
			.withInvoiceStatus(InvoiceStatus.UNPAID)    // Set to UNPAID to ensure it is not changed
			.build();

		final var entity = InvoiceEntity.builder()
			.withInvoiceCreatedDate(LocalDate.now().plusDays(2L))
			.build();

		mapper.determineIfSent(invoice, entity);
		assertThat(invoice.getInvoiceStatus()).isEqualTo(InvoiceStatus.UNPAID);
	}
}
