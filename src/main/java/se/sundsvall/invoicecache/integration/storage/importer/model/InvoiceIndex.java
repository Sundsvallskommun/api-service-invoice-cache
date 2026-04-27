package se.sundsvall.invoicecache.integration.storage.importer.model;

import java.util.List;

public record InvoiceIndex(

	String orderNumber,

	List<InvoiceIndexEntry> documents) {
}
