package se.sundsvall.invoicecache.integration.db.mapper;

import org.springframework.stereotype.Component;
import se.sundsvall.invoicecache.integration.db.entity.BackupInvoiceEntity;
import se.sundsvall.invoicecache.integration.db.entity.InvoiceEntity;

@Component
public class EntityMapper {

	public BackupInvoiceEntity mapInvoiceEntityToBackupEntity(final InvoiceEntity invoiceEntity) {
		return BackupInvoiceEntity.builder()
			.withMunicipalityId(invoiceEntity.getMunicipalityId())
			.withCity(invoiceEntity.getCity())
			.withClaimLevel(invoiceEntity.getClaimLevel())
			.withCustomerId(invoiceEntity.getCustomerId())
			.withCustomerName(invoiceEntity.getCustomerName())
			.withCustomerName2(invoiceEntity.getCustomerName2())
			.withCustomerType(invoiceEntity.getCustomerType())
			.withFileName(invoiceEntity.getFileName())
			.withInvoiceStatus(invoiceEntity.getInvoiceStatus())
			.withInvoiceDate(invoiceEntity.getInvoiceDate())
			.withInvoiceDueDate(invoiceEntity.getInvoiceDueDate())
			.withInvoiceAmount(invoiceEntity.getInvoiceAmount())
			.withInvoiceNumber(invoiceEntity.getInvoiceNumber())
			.withInvoiceReference(invoiceEntity.getInvoiceReference())
			.withName(invoiceEntity.getName())
			.withStreet(invoiceEntity.getStreet())
			.withOcrNumber(invoiceEntity.getOcrNumber())
			.withOrganizationNumber(invoiceEntity.getOrganizationNumber())
			.withPaidAmount(invoiceEntity.getPaidAmount())
			.withPaymentStatus(invoiceEntity.getPaymentStatus())
			.withVat(invoiceEntity.getVat())
			.withZip(invoiceEntity.getZip())
			.build();
	}

	public InvoiceEntity mapBackupEntityToInvoiceEntity(final BackupInvoiceEntity backupInvoiceEntity) {
		return InvoiceEntity.builder()
			.withMunicipalityId(backupInvoiceEntity.getMunicipalityId())
			.withCity(backupInvoiceEntity.getCity())
			.withClaimLevel(backupInvoiceEntity.getClaimLevel())
			.withCustomerId(backupInvoiceEntity.getCustomerId())
			.withCustomerName(backupInvoiceEntity.getCustomerName())
			.withCustomerName2(backupInvoiceEntity.getCustomerName2())
			.withCustomerType(backupInvoiceEntity.getCustomerType())
			.withFileName(backupInvoiceEntity.getFileName())
			.withInvoiceStatus(backupInvoiceEntity.getInvoiceStatus())
			.withInvoiceDate(backupInvoiceEntity.getInvoiceDate())
			.withInvoiceDueDate(backupInvoiceEntity.getInvoiceDueDate())
			.withInvoiceAmount(backupInvoiceEntity.getInvoiceAmount())
			.withInvoiceNumber(backupInvoiceEntity.getInvoiceNumber())
			.withInvoiceReference(backupInvoiceEntity.getInvoiceReference())
			.withName(backupInvoiceEntity.getName())
			.withStreet(backupInvoiceEntity.getStreet())
			.withOcrNumber(backupInvoiceEntity.getOcrNumber())
			.withOrganizationNumber(backupInvoiceEntity.getOrganizationNumber())
			.withPaidAmount(backupInvoiceEntity.getPaidAmount())
			.withPaymentStatus(backupInvoiceEntity.getPaymentStatus())
			.withVat(backupInvoiceEntity.getVat())
			.withZip(backupInvoiceEntity.getZip())
			.build();
	}

}
