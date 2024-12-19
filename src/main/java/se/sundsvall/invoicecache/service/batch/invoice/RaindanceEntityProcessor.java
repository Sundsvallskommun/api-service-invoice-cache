package se.sundsvall.invoicecache.service.batch.invoice;

import jakarta.validation.constraints.NotNull;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;
import se.sundsvall.invoicecache.integration.RaindanceToInvoiceMapper;
import se.sundsvall.invoicecache.integration.db.entity.InvoiceEntity;
import se.sundsvall.invoicecache.integration.raindance.RaindanceQueryResultDto;

@Component
public class RaindanceEntityProcessor implements ItemProcessor<RaindanceQueryResultDto, InvoiceEntity> {

	private final RaindanceToInvoiceMapper mapper;

	public RaindanceEntityProcessor(final RaindanceToInvoiceMapper mapper) {
		this.mapper = mapper;
	}

	@Override
	public InvoiceEntity process(final @NotNull RaindanceQueryResultDto dto) {
		return mapper.mapRaindanceDtoToInvoice(dto);
	}
}
