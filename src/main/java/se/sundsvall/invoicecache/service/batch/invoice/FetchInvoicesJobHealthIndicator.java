package se.sundsvall.invoicecache.service.batch.invoice;

import org.springframework.stereotype.Component;
import se.sundsvall.invoicecache.service.batch.BatchHealthIndicator;

@Component
class FetchInvoicesJobHealthIndicator extends BatchHealthIndicator {

	static final String NAME = "FetchInvoicesJob";

	FetchInvoicesJobHealthIndicator() {
		super(NAME);
	}
}
