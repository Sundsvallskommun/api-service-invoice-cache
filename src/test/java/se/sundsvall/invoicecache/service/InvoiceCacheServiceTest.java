package se.sundsvall.invoicecache.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
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

	private final Page<InvoiceEntity> invoicePage = new PageImpl<>(Arrays.asList(generateMinimalInvoiceEntity("7001011234"), generateMinimalInvoiceEntity("7001011235")));

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

	@Test
	void testGetInvoices() {

		// Arrange
		final var municipalityId = "2281";
		when(mockPartyClient.getLegalIdsFromParty("ab123", municipalityId)).thenReturn("197001011234");
		when(mockPartyClient.getLegalIdsFromParty("cde345", municipalityId)).thenReturn("197001011235");
		when(mockInvoiceSpecifications.createInvoicesSpecification(any(InvoiceFilterRequest.class), eq(municipalityId))).thenReturn(mockSpecification);
		when(mockRepository.findAll(Mockito.<Specification<InvoiceEntity>>any(), any(Pageable.class))).thenReturn(invoicePage);
		when(mockMapper.entityToInvoice(any(InvoiceEntity.class))).thenReturn(new Invoice());

		// Act
		final InvoicesResponse response = service.getInvoices(generateMinimalInvoiceFilterRequest(), municipalityId);

		// Assert
		assertThat(response).isNotNull();
		assertThat(response.getInvoices()).hasSize(2);
		verify(mockPartyClient, times(2)).getLegalIdsFromParty(anyString(), eq(municipalityId));
		verify(mockInvoiceSpecifications, times(1)).createInvoicesSpecification(any(InvoiceFilterRequest.class), eq(municipalityId));
		verify(mockRepository, times(1)).findAll(Mockito.<Specification<InvoiceEntity>>any(), any(Pageable.class));
		verify(mockMapper, times(2)).entityToInvoice(any(InvoiceEntity.class));
	}

	@Test
	void testCreateMetadata() {
		// Act
		final MetaData metadata = service.createMetaData(generateMinimalInvoiceFilterRequest(), invoicePage);

		// Assert
		assertThat(metadata.getPage()).isEqualTo(1);
		assertThat(metadata.getLimit()).isEqualTo(100);
		assertThat(metadata.getTotalRecords()).isEqualTo(2);
		assertThat(metadata.getCount()).isEqualTo(2);
		assertThat(metadata.getTotalPages()).isEqualTo(1);
	}

}
