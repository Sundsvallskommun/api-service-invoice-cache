package se.sundsvall.invoicecache.service.batch.invoice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.invoicecache.integration.db.entity.InvoiceEntity;
import se.sundsvall.invoicecache.integration.raindance.RaindanceQueryResultDto;
import se.sundsvall.invoicecache.integration.raindance.mapper.RaindanceToInvoiceMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RaindanceEntityProcessorTest {

	@Mock
	private RaindanceToInvoiceMapper mockMapper;

	@InjectMocks
	private RaindanceEntityProcessor processor;

	@Test
	void testProcess() {
		when(mockMapper.mapRaindanceDtoToInvoice(any(RaindanceQueryResultDto.class))).thenReturn(new InvoiceEntity());
		final InvoiceEntity process = processor.process(new RaindanceQueryResultDto());
		assertThat(process).isNotNull();
	}

}
