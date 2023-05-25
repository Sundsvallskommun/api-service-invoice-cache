package se.sundsvall.invoicecache.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import se.sundsvall.invoicecache.api.model.Invoice;
import se.sundsvall.invoicecache.api.model.InvoiceFilterRequest;
import se.sundsvall.invoicecache.api.model.InvoiceMapper;
import se.sundsvall.invoicecache.api.model.InvoicesResponse;
import se.sundsvall.invoicecache.api.model.MetaData;
import se.sundsvall.invoicecache.integration.db.InvoiceEntityRepository;
import se.sundsvall.invoicecache.integration.db.entity.InvoiceEntity;
import se.sundsvall.invoicecache.integration.db.specifications.InvoiceSpecifications;
import se.sundsvall.invoicecache.integration.party.PartyClient;

@Service
public class InvoiceCacheService {

    private static final Logger LOG = LoggerFactory.getLogger(InvoiceCacheService.class);
    
    private final InvoiceEntityRepository invoiceRepository;
    private final InvoiceMapper mapper;
    private final InvoiceSpecifications invoiceSpecifications;
    private final PartyClient partyClient;

    public InvoiceCacheService(final InvoiceEntityRepository invoiceRepository,
            final InvoiceMapper mapper, final InvoiceSpecifications invoiceSpecifications,
            final PartyClient partyClient) {
        this.invoiceRepository = invoiceRepository;
        this.mapper = mapper;
        this.invoiceSpecifications = invoiceSpecifications;
        this.partyClient = partyClient;
    }
    
    public InvoicesResponse getInvoices(InvoiceFilterRequest request) {
        final InvoicesResponse response = new InvoicesResponse();

        //Key: partyId, Value: legalId. Used later for mapping back which invoice belongs to which partyId
        Map<String, String> legalIdPartyIdMap = new HashMap<>();

        //Fetch legalIds (if any) from party service and set them in the request.
        if(!CollectionUtils.isEmpty(request.getPartyIds())) {
            request.getPartyIds().forEach(partyId -> {
                String legalId = partyClient.getLegalIdsFromParty(partyId);

                //Store each legalId in a hashmap
                legalIdPartyIdMap.put(legalId, partyId);
            });

            //Set the fetched legalIds in the request.
            request.setLegalIds(legalIdPartyIdMap.keySet().stream().toList());
            LOG.info("legalIds to look for: {}", request.getLegalIds());
        }

        //Find all invoices matching the request, map them and add them to the response.
        Page<InvoiceEntity> invoicePage = invoiceRepository.findAll(invoiceSpecifications.createInvoicesSpecification(request), getPagingParameters(request));

        LOG.info("Got {} invoices from the DB.", invoicePage.getTotalElements());
        invoicePage.forEach(entity -> {
                    Invoice invoice = mapper.entityToInvoice(entity);
                    invoice.setPartyId(legalIdPartyIdMap.getOrDefault(invoice.getLegalId(), null));
                    response.addInvoice(invoice);
                });

        //Set pagination data
        response.setMetaData(createMetaData(request, invoicePage));
        return response;
    }

    public Optional<Invoice> getInvoice(String invoiceNumber) {
        return invoiceRepository.findByInvoiceNumber(invoiceNumber)
            .map(mapper::entityToInvoice);
    }

    MetaData createMetaData(InvoiceFilterRequest request, Page<InvoiceEntity> page) {
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
     * @param request
     * @return+
     */
    private Pageable getPagingParameters(InvoiceFilterRequest request) {
        return PageRequest.of(request.getPage() - 1, request.getLimit());
    }
}
