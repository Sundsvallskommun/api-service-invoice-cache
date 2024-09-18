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

	public InvoiceEntity mapBackupEntityToInvoiceEntity(final BackupInvoiceEntity invoiceEntity) {
		return InvoiceEntity.builder()
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

}
