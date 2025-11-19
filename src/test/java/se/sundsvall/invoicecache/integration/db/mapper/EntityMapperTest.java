package se.sundsvall.invoicecache.integration.db.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import se.sundsvall.invoicecache.TestObjectFactory;

class EntityMapperTest {

	private final EntityMapper mapper = new EntityMapper();

	@Test
	void testMapInvoiceEntityToBackupEntity() {
		final var entity = TestObjectFactory.generateInvoiceEntity();
		final var backup = mapper.mapInvoiceEntityToBackupEntity(entity);

		assertThat(backup.getCity()).isEqualTo("ANKEBORG");
		assertThat(backup.getClaimLevel()).isEqualTo(1);
		assertThat(backup.getCustomerId()).isEqualTo("8302311234");
		assertThat(backup.getCustomerName()).isEqualTo("Kalle Anka");
		assertThat(backup.getCustomerName2()).isEqualTo("c/o Knattarna");
		assertThat(backup.getCustomerType()).isEqualTo("KA");
		assertThat(backup.getFileName()).isEqualTo("Filnamn.pdf");
		assertThat(backup.getInvoiceStatus()).isEqualTo("Obetald");
		assertThat(backup.getInvoiceDate()).isEqualTo(LocalDate.of(2022, 1, 2));
		assertThat(backup.getInvoiceDueDate()).isEqualTo(LocalDate.of(2022, 2, 3));
		assertThat(backup.getInvoiceAmount()).isEqualTo(BigDecimal.valueOf(100).setScale(2, RoundingMode.HALF_EVEN));
		assertThat(backup.getInvoiceNumber()).isEqualTo("12345678");
		assertThat(backup.getInvoiceReference()).isEqualTo("Oppfinnarjocke");
		assertThat(backup.getName()).isEqualTo("Kalle Anka");
		assertThat(backup.getStreet()).isEqualTo("Ankeborgsvägen 2");
		assertThat(backup.getOcrNumber()).isEqualTo("5566778899");
		assertThat(backup.getOrganizationNumber()).isEqualTo("5599113214");
		assertThat(backup.getPaidAmount()).isEqualTo(BigDecimal.valueOf(50).setScale(2, RoundingMode.HALF_EVEN));
		assertThat(backup.getPaymentStatus()).isEqualTo("Betald");
		assertThat(backup.getZip()).isEqualTo("123 45");
	}

	@Test
	void testMapBackupEntityToInvoiceEntity() {
		final var entity = TestObjectFactory.generateBackupInvoiceEntity();
		final var invoice = mapper.mapBackupEntityToInvoiceEntity(entity);

		assertThat(invoice.getCity()).isEqualTo("ANKEBORG");
		assertThat(invoice.getCustomerId()).isEqualTo("8302311234");
		assertThat(invoice.getCustomerName()).isEqualTo("Kalle Anka");
		assertThat(invoice.getCustomerName2()).isEqualTo("c/o Knattarna");
		assertThat(invoice.getCustomerType()).isEqualTo("KA");
		assertThat(invoice.getFileName()).isEqualTo("Filnamn.pdf");
		assertThat(invoice.getInvoiceStatus()).isEqualTo("Obetald");
		assertThat(invoice.getInvoiceDate()).isEqualTo(LocalDate.of(2022, 1, 2));
		assertThat(invoice.getInvoiceDueDate()).isEqualTo(LocalDate.of(2022, 2, 3));
		assertThat(invoice.getInvoiceAmount()).isEqualTo(BigDecimal.valueOf(100).setScale(2, RoundingMode.HALF_EVEN));
		assertThat(invoice.getInvoiceNumber()).isEqualTo("12345678");
		assertThat(invoice.getInvoiceReference()).isEqualTo("Oppfinnarjocke");
		assertThat(invoice.getName()).isEqualTo("Kalle Anka");
		assertThat(invoice.getStreet()).isEqualTo("Ankeborgsvägen 2");
		assertThat(invoice.getOcrNumber()).isEqualTo("5566778899");
		assertThat(invoice.getOrganizationNumber()).isEqualTo("5599113214");
		assertThat(invoice.getPaidAmount()).isEqualTo(BigDecimal.valueOf(50).setScale(2, RoundingMode.HALF_EVEN));
		assertThat(invoice.getPaymentStatus()).isEqualTo("Betald");
		assertThat(invoice.getZip()).isEqualTo("123 45");
	}
}
