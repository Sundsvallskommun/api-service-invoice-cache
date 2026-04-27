package se.sundsvall.invoicecache.integration.storage.importer.model;

import java.time.OffsetDateTime;

public record InvoiceIndexEntry(

	String source,

	String invoiceNumber,

	OffsetDateTime archiveDate,

	String customerNumber,

	String customerName) {
}
