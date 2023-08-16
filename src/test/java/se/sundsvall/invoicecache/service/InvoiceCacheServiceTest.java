package se.sundsvall.invoicecache.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.sundsvall.invoicecache.TestObjectFactory.generateMinimalInvoiceEntity;
import static se.sundsvall.invoicecache.TestObjectFactory.generateMinimalInvoiceFilterRequest;

import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import se.sundsvall.invoicecache.api.model.Invoice;
import se.sundsvall.invoicecache.api.model.InvoiceFilterRequest;
import se.sundsvall.invoicecache.api.model.InvoiceMapper;
import se.sundsvall.invoicecache.api.model.InvoicesResponse;
import se.sundsvall.invoicecache.api.model.MetaData;
import se.sundsvall.invoicecache.integration.db.InvoiceEntityRepository;
import se.sundsvall.invoicecache.integration.db.entity.InvoiceEntity;
import se.sundsvall.invoicecache.integration.db.specifications.InvoiceSpecifications;
import se.sundsvall.invoicecache.integration.party.PartyClient;

@ExtendWith(MockitoExtension.class)
class InvoiceCacheServiceTest {

	@Mock
	private InvoiceEntityRepository mockRepository;

	@Mock
	private InvoiceMapper mockMapper;

	@Mock
	private InvoiceSpecifications mockInvoiceSpecifications;

	@Mock
	private Specification<InvoiceEntity> mockSpecification;

	@Mock
	private Scheduler mockScheduler;

	@Mock
	private PartyClient mockPartyClient;

	@InjectMocks
	private InvoiceCacheService service;

	private final Page<InvoiceEntity> invoicePage = new PageImpl<>(Arrays.asList(generateMinimalInvoiceEntity("7001011234"), generateMinimalInvoiceEntity("7001011235")));

	@Test
    void testGetInvoices() {
        when(mockPartyClient.getLegalIdsFromParty("ab123")).thenReturn("197001011234");
        when(mockPartyClient.getLegalIdsFromParty("cde345")).thenReturn("197001011235");
        when(mockInvoiceSpecifications.createInvoicesSpecification(any(InvoiceFilterRequest.class))).thenReturn(mockSpecification);
        when(mockRepository.findAll(Mockito.<Specification<InvoiceEntity>>any(), any(Pageable.class))).thenReturn(invoicePage);
        when(mockMapper.entityToInvoice(any(InvoiceEntity.class))).thenReturn(new Invoice());

        final InvoicesResponse response = service.getInvoices(generateMinimalInvoiceFilterRequest());

        assertNotNull(response);
        assertEquals(2, response.getInvoices().size());
        verify(mockPartyClient, times(2)).getLegalIdsFromParty(anyString());
        verify(mockInvoiceSpecifications, times(1)).createInvoicesSpecification(any(InvoiceFilterRequest.class));
        verify(mockRepository, times(1)).findAll(Mockito.<Specification<InvoiceEntity>>any(), any(Pageable.class));
        verify(mockMapper, times(2)).entityToInvoice(any(InvoiceEntity.class));
    }

	@Test
	void testCreateMetadata() {
		final MetaData metadata = service.createMetaData(generateMinimalInvoiceFilterRequest(), invoicePage);
		assertEquals(1, metadata.getPage());
		assertEquals(100, metadata.getLimit());
		assertEquals(2, metadata.getTotalRecords());
		assertEquals(2, metadata.getCount());
		assertEquals(1, metadata.getTotalPages());
	}
}
