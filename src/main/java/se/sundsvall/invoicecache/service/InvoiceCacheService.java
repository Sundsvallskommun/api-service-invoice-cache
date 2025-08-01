package se.sundsvall.invoicecache.service;

import static se.sundsvall.dept44.util.LogUtils.sanitizeForLogging;

import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import se.sundsvall.dept44.util.LogUtils;
import se.sundsvall.invoicecache.api.model.Invoice;
import se.sundsvall.invoicecache.api.model.InvoiceFilterRequest;
import se.sundsvall.invoicecache.api.model.InvoiceMapper;
import se.sundsvall.invoicecache.api.model.InvoicesResponse;
import se.sundsvall.invoicecache.api.model.MetaData;
import se.sundsvall.invoicecache.integration.db.InvoiceRepository;
import se.sundsvall.invoicecache.integration.db.entity.InvoiceEntity;
import se.sundsvall.invoicecache.integration.db.specifications.InvoiceSpecifications;
import se.sundsvall.invoicecache.integration.party.PartyClient;

@Service
public class InvoiceCacheService {

	private static final Logger LOG = LoggerFactory.getLogger(InvoiceCacheService.class);

	private final InvoiceRepository invoiceRepository;

	private final InvoiceMapper mapper;

	private final InvoiceSpecifications invoiceSpecifications;

	private final PartyClient partyClient;

	public InvoiceCacheService(final InvoiceRepository invoiceRepository,
		final InvoiceMapper mapper, final InvoiceSpecifications invoiceSpecifications,
		final PartyClient partyClient) {
		this.invoiceRepository = invoiceRepository;
		this.mapper = mapper;
		this.invoiceSpecifications = invoiceSpecifications;
		this.partyClient = partyClient;
	}

	public InvoicesResponse getInvoices(final InvoiceFilterRequest request, final String municipalityId) {
		final InvoicesResponse response = new InvoicesResponse();

		// Key: partyId, Value: legalId. Used later for mapping back which invoice belongs to which partyId
		final Map<String, String> legalIdPartyIdMap = new HashMap<>();

		// Fetch legalIds (if any) from party service and set them in the request.
		if (!CollectionUtils.isEmpty(request.getPartyIds())) {
			request.getPartyIds().forEach(partyId -> {
				final String legalId = partyClient.getLegalIdsFromParty(partyId, municipalityId);

				// Store each legalId in a hashmap
				legalIdPartyIdMap.put(legalId, partyId);
			});

			// Set the fetched legalIds in the request.
			request.setLegalIds(legalIdPartyIdMap.keySet().stream().toList());
			LOG.info("legalIds to look for: {}", request.getLegalIds().stream().map(LogUtils::sanitizeForLogging).toList());
		}

		// Find all invoices matching the request, map them and add them to the response.
		final Page<InvoiceEntity> invoicePage = invoiceRepository.findAll(invoiceSpecifications.createInvoicesSpecification(request, municipalityId), getPagingParameters(request));

		LOG.info("Got {} invoices from the DB.", invoicePage.getTotalElements());
		invoicePage.forEach(entity -> {
			final Invoice invoice = mapper.entityToInvoice(entity);
			invoice.setPartyId(legalIdPartyIdMap.getOrDefault(invoice.getLegalId(), null));
			response.addInvoice(invoice);
		});

		// Set pagination data
		response.setMetaData(createMetaData(request, invoicePage));
		return response;
	}

	MetaData createMetaData(final InvoiceFilterRequest request, final Page<InvoiceEntity> page) {
		return MetaData.builder()
			.withPage(request.getPage())
			.withLimit(request.getLimit())
			.withTotalRecords(page.getTotalElements())
			.withCount(page.getNumberOfElements())
			.withTotalPages(page.getTotalPages())
			.build();
	}

	/**
	 * To mimic the api for Invoices, a page starts with "1" and that is also the minimum value in the api.
	 * The PageRequest originally has a 0-based page index, subtract 1 for which page to request.
	 *
	 * @param  request - the request
	 * @return         Pageable
	 */
	private Pageable getPagingParameters(final InvoiceFilterRequest request) {
		return PageRequest.of(request.getPage() - 1, request.getLimit());
	}

}
