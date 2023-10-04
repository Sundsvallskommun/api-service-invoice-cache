package se.sundsvall.invoicecache.integration.db.mapper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import se.sundsvall.invoicecache.TestObjectFactory;
import se.sundsvall.invoicecache.integration.db.entity.BackupInvoiceEntity;
import se.sundsvall.invoicecache.integration.db.entity.InvoiceEntity;

@ExtendWith(SoftAssertionsExtension.class)
class EntityMapperTest {

    private final EntityMapper mapper = new EntityMapper();
    
    @Test
    void testMapInvoiceEntityToBackupEntity(final SoftAssertions softly) {
        InvoiceEntity entity = TestObjectFactory.generateInvoiceEntity();
        final BackupInvoiceEntity backup = mapper.mapInvoiceEntityToBackupEntity(entity);
        
        softly.assertThat(backup.getCity()).isEqualTo("ANKEBORG");
        softly.assertThat(backup.getClaimLevel()).isEqualTo(1);
        softly.assertThat(backup.getCustomerId()).isEqualTo("8302311234");
        softly.assertThat(backup.getCustomerName()).isEqualTo("Kalle Anka");
        softly.assertThat(backup.getCustomerName2()).isEqualTo("c/o Knattarna");
        softly.assertThat(backup.getCustomerType()).isEqualTo("KA");
        softly.assertThat(backup.getFileName()).isEqualTo("Filnamn.pdf");
        softly.assertThat(backup.getInvoiceStatus()).isEqualTo("Obetald");
        softly.assertThat(backup.getInvoiceDate()).isEqualTo(LocalDate.of(2022, 1, 2));
        softly.assertThat(backup.getInvoiceDueDate()).isEqualTo(LocalDate.of(2022, 2, 3));
        softly.assertThat(backup.getInvoiceAmount()).isEqualTo(BigDecimal.valueOf(100).setScale(2, RoundingMode.HALF_EVEN));
        softly.assertThat(backup.getInvoiceNumber()).isEqualTo("12345678");
        softly.assertThat(backup.getInvoiceReference()).isEqualTo("Oppfinnarjocke");
        softly.assertThat(backup.getName()).isEqualTo("Kalle Anka");
        softly.assertThat(backup.getStreet()).isEqualTo("Ankeborgsvägen 2");
        softly.assertThat(backup.getOcrNumber()).isEqualTo("5566778899");
        softly.assertThat(backup.getOrganizationNumber()).isEqualTo("5599113214");
        softly.assertThat(backup.getPaidAmount()).isEqualTo(BigDecimal.valueOf(50).setScale(2, RoundingMode.HALF_EVEN));
        softly.assertThat(backup.getPaymentStatus()).isEqualTo("Betald");
        softly.assertThat(backup.getZip()).isEqualTo("123 45");
    }
    
    @Test
    void testMapBackupEntityToInvoiceEntity(final SoftAssertions softly) {
        BackupInvoiceEntity entity = TestObjectFactory.generateBackupInvoiceEntity();
        final InvoiceEntity invoice = mapper.mapBackupEntityToInvoiceEntity(entity);
    
        softly.assertThat(invoice.getCity()).isEqualTo("ANKEBORG");
        softly.assertThat(invoice.getClaimLevel()).isEqualTo(1);
        softly.assertThat(invoice.getCustomerId()).isEqualTo("8302311234");
        softly.assertThat(invoice.getCustomerName()).isEqualTo("Kalle Anka");
        softly.assertThat(invoice.getCustomerName2()).isEqualTo("c/o Knattarna");
        softly.assertThat(invoice.getCustomerType()).isEqualTo("KA");
        softly.assertThat(invoice.getFileName()).isEqualTo("Filnamn.pdf");
        softly.assertThat(invoice.getInvoiceStatus()).isEqualTo("Obetald");
        softly.assertThat(invoice.getInvoiceDate()).isEqualTo(LocalDate.of(2022, 1, 2));
        softly.assertThat(invoice.getInvoiceDueDate()).isEqualTo(LocalDate.of(2022, 2, 3));
        softly.assertThat(invoice.getInvoiceAmount()).isEqualTo(BigDecimal.valueOf(100).setScale(2, RoundingMode.HALF_EVEN));
        softly.assertThat(invoice.getInvoiceNumber()).isEqualTo("12345678");
        softly.assertThat(invoice.getInvoiceReference()).isEqualTo("Oppfinnarjocke");
        softly.assertThat(invoice.getName()).isEqualTo("Kalle Anka");
        softly.assertThat(invoice.getStreet()).isEqualTo("Ankeborgsvägen 2");
        softly.assertThat(invoice.getOcrNumber()).isEqualTo("5566778899");
        softly.assertThat(invoice.getOrganizationNumber()).isEqualTo("5599113214");
        softly.assertThat(invoice.getPaidAmount()).isEqualTo(BigDecimal.valueOf(50).setScale(2, RoundingMode.HALF_EVEN));
        softly.assertThat(invoice.getPaymentStatus()).isEqualTo("Betald");
        softly.assertThat(invoice.getZip()).isEqualTo("123 45");
    }
}