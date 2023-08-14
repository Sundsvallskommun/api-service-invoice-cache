package se.sundsvall.invoicecache;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.hibernate.engine.jdbc.BlobProxy;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;

import se.sundsvall.invoicecache.api.batchactuator.JobStatus;
import se.sundsvall.invoicecache.api.model.Address;
import se.sundsvall.invoicecache.api.model.Invoice;
import se.sundsvall.invoicecache.api.model.InvoiceFilterRequest;
import se.sundsvall.invoicecache.api.model.InvoicePdf;
import se.sundsvall.invoicecache.api.model.InvoiceStatus;
import se.sundsvall.invoicecache.api.model.InvoiceType;
import se.sundsvall.invoicecache.integration.db.entity.BackupInvoiceEntity;
import se.sundsvall.invoicecache.integration.db.entity.InvoiceEntity;
import se.sundsvall.invoicecache.integration.db.entity.PdfEntity;
import se.sundsvall.invoicecache.integration.raindance.RaindanceQueryResultDto;

public class TestObjectFactory {

	public static JobStatus generateCompletedJobStatus() {
		final JobStatus jobStatus = JobStatus
			.builder()
			.withStatus(ExitStatus.COMPLETED.toString())
			.withStartTime(LocalDateTime.of(2022, 1, 1, 1, 1, 1))
			.withEndTime(LocalDateTime.of(2022, 1, 1, 1, 2, 2))
			.withStepStatusMap(Map.of("invoiceStep", JobStatus.StepStatus.builder()
				.withStepName("invoiceStep")
				.withStepWriteCount(5)
				.withStepReadCount(6)
				.build()))
			.build();

		return jobStatus;
	}

	public static RaindanceQueryResultDto generateRaindanceQueryResultDto() {
		return RaindanceQueryResultDto.builder()
			.withAdr2("Ankeborgsvägen 2")
			.withBeloppSek(BigDecimal.valueOf(100).setScale(2, RoundingMode.HALF_EVEN))
			.withBetaltSek(BigDecimal.valueOf(50).setScale(2, RoundingMode.HALF_EVEN))
			.withBetpamdatum(Timestamp.valueOf(LocalDateTime.of(2022, 2, 5, 2, 2))) // 1799-12-31 00:00:00.000 is default in raindance
			.withFaktstatus("Betald")
			.withFaktstatus2("U")
			.withFakturadatum(Timestamp.valueOf(LocalDateTime.of(2021, 1, 1, 1, 1)))
			.withFilnamn("Filnamn.pdf")
			.withForfallodatum(Timestamp.valueOf(LocalDateTime.of(2022, 2, 2, 2, 2)))
			.withKravniva(1)
			.withKundid("8302311234")
			.withKundidText("Kalle Anka")
			.withKundrtyp("KA")
			.withNamn2("c/o Knattarna")
			.withNr(12345678)
			.withOcrnr("5566778899")
			.withOrgnr("5599113214")
			.withOrt("123 45 ANKEBORG")
			.withTabBehand("KVIT")
			.withUtskrdatum(Timestamp.valueOf(LocalDateTime.of(2021, 1, 1, 1, 1)))
			.withVREF("Oppfinnarjocke")
			.withZ11_beadat(Timestamp.valueOf(LocalDateTime.of(2022, 2, 2, 2, 2)))
			.withZ11_bearpnr(123)
			.withZ11_beasum1(BigDecimal.valueOf(120).setScale(2, RoundingMode.HALF_EVEN))
			.withZ11_blopnr(12)
			.withZ11_sbnr(76247)
			.withZ21_beabel1(BigDecimal.valueOf(195).setScale(2, RoundingMode.HALF_EVEN))
			.withZ21_beadat(Timestamp.valueOf(LocalDateTime.of(2022, 2, 2, 2, 2)))
			.withZ21_rpnr(155567678)
			.build();
	}

	public static InvoiceEntity generateInvoiceEntity() {
		return InvoiceEntity.builder()
			.withInvoiceReminderDate(null) // 1799-12-31 is default in raindance
			.withCity("ANKEBORG")
			.withClaimLevel(1)
			.withCustomerId("8302311234")
			.withCustomerName("Kalle Anka")
			.withCustomerName2("c/o Knattarna")
			.withCustomerType("KA")
			.withFileName("Filnamn.pdf")
			.withInvoiceCreatedDate(LocalDate.of(2022, 1, 1))
			.withInvoiceStatus("Obetald")
			.withInvoiceDate(LocalDate.of(2022, 1, 2))
			.withInvoiceDueDate(LocalDate.of(2022, 2, 3))
			.withInvoiceAmount(BigDecimal.valueOf(100).setScale(2, RoundingMode.HALF_EVEN))
			.withInvoiceNumber("12345678")
			.withInvoiceReference("Oppfinnarjocke")
			.withInvoiceStatus2("U")
			.withName("Kalle Anka")
			.withStreet("Ankeborgsvägen 2")
			.withOcrNumber("5566778899")
			.withOrganizationNumber("5599113214")
			.withPaidAmount(BigDecimal.valueOf(50.00).setScale(2, RoundingMode.HALF_EVEN))
			.withPaymentStatus("Betald")
			.withVat(BigDecimal.valueOf(20).setScale(2, RoundingMode.HALF_EVEN))
			.withZip("123 45")
			.build();
	}

	public static BackupInvoiceEntity generateBackupInvoiceEntity() {
		return BackupInvoiceEntity.builder()
			.withInvoiceReminderDate(null)
			.withCity("ANKEBORG")
			.withClaimLevel(1)
			.withCustomerId("8302311234")
			.withCustomerName("Kalle Anka")
			.withCustomerName2("c/o Knattarna")
			.withCustomerType("KA")
			.withFileName("Filnamn.pdf")
			.withInvoiceCreatedDate(LocalDate.of(2022, 1, 1))
			.withInvoiceStatus("Obetald")
			.withInvoiceDate(LocalDate.of(2022, 1, 2))
			.withInvoiceDueDate(LocalDate.of(2022, 2, 3))
			.withInvoiceAmount(BigDecimal.valueOf(100).setScale(2, RoundingMode.HALF_EVEN))
			.withInvoiceNumber("12345678")
			.withInvoiceReference("Oppfinnarjocke")
			.withInvoiceStatus2("U")
			.withName("Kalle Anka")
			.withStreet("Ankeborgsvägen 2")
			.withOcrNumber("5566778899")
			.withOrganizationNumber("5599113214")
			.withPaidAmount(BigDecimal.valueOf(50).setScale(2, RoundingMode.HALF_EVEN))
			.withPaymentStatus("Betald")
			.withZip("123 45")
			.build();
	}

	public static PdfEntity generatePdfEntity() {
		return PdfEntity.builder()
			.withId(1)
			.withInvoiceNumber("someNumber")
			.withInvoiceIssuerLegalId("someIssuerLegalId")
			.withInvoiceDebtorLegalId("someDebtorLegalId")
			.withInvoiceType(InvoiceType.CONSOLIDATED_INVOICE)
			.withFilename("someFileName")
			.withDocument(BlobProxy.generateProxy("blobMe".getBytes()))
			.build();
	}

	public static Invoice generateInvoice() {
		return Invoice.builder()
			.withCustomerName("Kalle Anka")
			.withCustomerType("XH")
			.withInvoiceNumber("12345678")
			.withInvoiceStatus(InvoiceStatus.PAID)
			.withOcrNumber("5566778899")
			.withPartyId("partyId")
			.withLegalId("198001011234")
			.withInvoiceDescription("invoiceDescription")
			.withInvoiceDueDate(LocalDate.of(2022, 2, 3))
			.withInvoiceDate(LocalDate.of(2022, 1, 2))
			.withPaidAmount(BigDecimal.valueOf(50).setScale(2, RoundingMode.HALF_EVEN))
			.withTotalAmount(BigDecimal.valueOf(100).setScale(2, RoundingMode.HALF_EVEN))
			.withInvoiceFileName("Filnamn.pdf")
			.withVat(BigDecimal.valueOf(20).setScale(2, RoundingMode.HALF_EVEN))
			.withAmountVatExcluded(BigDecimal.valueOf(80).setScale(2, RoundingMode.HALF_EVEN))
			.withInvoiceType(InvoiceType.INVOICE)
			.withInvoiceAddress(Address.builder()
				.withCity("Ankeborg")
				.withStreet("Ankeborgsvägen 2")
				.withPostcode("123 45")
				.withCareOf("c/o Knattarna")
				.build())
			.build();
	}

	public static InvoiceFilterRequest generateInvoiceFilterRequest() {
		final InvoiceFilterRequest request = new InvoiceFilterRequest();
		request.setPartyIds(List.of("5533221234"));
		return request;
	}

	public static InvoicePdf generateInvoicePdf() {
		return InvoicePdf.builder()
			.withContent("someContent")
			.withName("someName")
			.build();
	}

	public static InvoiceFilterRequest generateMinimalInvoiceFilterRequest() {
		final InvoiceFilterRequest request = new InvoiceFilterRequest();
		request.setPartyIds(List.of("ab123", "cde345"));

		return request;
	}

	public static InvoiceEntity generateMinimalInvoiceEntity(final String orgNumber) {
		return InvoiceEntity.builder()
			.withOrganizationNumber(orgNumber)
			.build();
	}

	public static JobExecution createJobExecution(final ExitStatus exitStatus) {
		final JobExecution jobExecution = new JobExecution(1L);
		jobExecution.setExitStatus(exitStatus);
		jobExecution.setEndTime(LocalDateTime.now());

		return jobExecution;
	}
}
