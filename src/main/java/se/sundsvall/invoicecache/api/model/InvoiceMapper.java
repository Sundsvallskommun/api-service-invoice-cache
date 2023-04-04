package se.sundsvall.invoicecache.api.model;

import java.math.RoundingMode;
import java.time.LocalDate;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import se.sundsvall.invoicecache.integration.db.entity.InvoiceEntity;

@Component
public class InvoiceMapper {

    /**
     * Map entity to response. Since we don't want to expose legalId, it will be fetched from
     * the service layer and inserted here.
     * @param entity
     * @return
     */
    public Invoice entityToInvoice(final InvoiceEntity entity) {
        //Don't map legalId!
        Invoice invoice = Invoice
                .builder()
                .withCustomerName(entity.getCustomerName())
                .withCustomerType(entity.getCustomerType())
                .withInvoiceAddress(entityToAddress(entity))
                .withInvoiceDate(entity.getInvoiceDate())
                .withInvoiceDescription(entity.getInvoiceReference())
                .withInvoiceDueDate(entity.getInvoiceDueDate())
                .withInvoiceFileName(entity.getFileName())
                .withInvoiceNumber(entity.getInvoiceNumber())
                .withInvoiceReminderDate(entity.getInvoiceReminderDate())
                .withInvoiceStatus(InvoiceStatus.fromValue(entity.getInvoiceStatus()))
                .withInvoiceType(InvoiceType.fromInvoiceAmount(entity.getInvoiceAmount()))
                .withLegalId(entity.getOrganizationNumber())
                .withOcrNumber(entity.getOcrNumber())
                .withPaidAmount(entity.getPaidAmount().setScale(2, RoundingMode.HALF_EVEN))
                .withTotalAmount(entity.getInvoiceAmount().setScale(2, RoundingMode.HALF_EVEN))
                .withVat(entity.getVat().setScale(2, RoundingMode.HALF_EVEN))
                .build();

        //Important that these are done in this order!
        calculateInvoiceAndPaidAmount(invoice);
        calculateAmountExcludingVat(invoice);
        determineInvoiceStatus(invoice, entity);


        return invoice;
    }

    private Address entityToAddress(final InvoiceEntity entity) {
        return Address.builder()
                .withCareOf(entity.getCustomerName2())
                .withCity(entity.getCity())
                .withStreet(entity.getStreet())
                .withPostcode(entity.getZip())
                .build();
    }

    /**
     * Calculates the amount without VAT, calculation is done on the invoice object since the entity
     * probably contains negative values for totalAmount.
     * @param invoice
     */
    private void calculateAmountExcludingVat(Invoice invoice) {
        //Subtract the vat amount from total amount.
        invoice.setAmountVatExcluded(invoice.getTotalAmount().subtract(invoice.getVat()).abs().setScale(2, RoundingMode.HALF_EVEN));
    }

    private void calculateInvoiceAndPaidAmount(Invoice invoice) {
        //If invoice amount is (+) and paid amount is (-), it's paid, set the paid amount to (+)
        if(invoice.getTotalAmount().signum() == 1 && invoice.getPaidAmount().signum() == -1) {
            invoice.setPaidAmount(invoice.getPaidAmount().abs());
        }
        //If invoice amount is (-) and paid amount is (+), it's credited. Set all amounts to (+)
        if(invoice.getTotalAmount().signum() == -1 && invoice.getPaidAmount().signum() == 1) {
            invoice.setPaidAmount(invoice.getPaidAmount().abs());
            invoice.setTotalAmount(invoice.getTotalAmount().abs());
        }
        //Check if it's a credited invoice and not paid, only set the total amount to (+).
        if(invoice.getTotalAmount().signum() == -1 && invoice.getPaidAmount().signum() == 0) {
            invoice.setTotalAmount(invoice.getTotalAmount().abs());
        }
    }

    void determineInvoiceStatus(Invoice invoice, InvoiceEntity entity) {
        determineIfReminder(invoice);
        determineIfSent(invoice, entity);
    }

    /**
     * If an invoice has a reminder date after its invoice due date, and
     * it has a valid status for e reminder, it's a reminder.
     * @param invoice
     */
    void determineIfReminder(Invoice invoice) {
        if (invoiceReminderDateIsAfterInvoiceDueDate(invoice) && invoiceHasStatusValidForReminder(invoice)) {
            invoice.setInvoiceStatus(InvoiceStatus.REMINDER);
        }
    }

    /**
     * If reminder date is after invoice due date, it's a reminder
     * @param invoice
     * @return
     */
    boolean invoiceReminderDateIsAfterInvoiceDueDate(Invoice invoice) {
        if(invoice.getInvoiceReminderDate() == null) {
            return false;
        } else {
            return invoice.getInvoiceReminderDate().isAfter(invoice.getInvoiceDueDate());
        }
    }

    /**
     * Determine if the invoice has a status that is valid for a reminder.
     * If it's not paid in full, it's valid for the "REMINDER" status.
     * E.g if it's a debt collection etc it's not ok to set it as a reminder.
     * @param invoice
     * @return
     */
    boolean invoiceHasStatusValidForReminder(Invoice invoice) {
        return invoice.getInvoiceStatus() == InvoiceStatus.UNPAID
                || invoice.getInvoiceStatus() == InvoiceStatus.PARTIALLY_PAID;
    }

    /**
     * Check if we should set the "SENT" status on the invoice.
     * It is sent only if the invoice has a print date that's before the invoice date, and the invoice date haven't passed.
     * @param invoice to compare against the invoice date
     * @param entity to get hold of the print date for the invoice
     */
    void determineIfSent(Invoice invoice, InvoiceEntity entity) {
        //Check if todays date is between the invoicedate and print date.
        if(entity.getInvoiceCreatedDate() != null) {    //Invoicedate is never null in raindance
            if(LocalDate.now().isBefore(invoice.getInvoiceDate()) && LocalDate.now().isAfter(entity.getInvoiceCreatedDate())) {
                invoice.setInvoiceStatus(InvoiceStatus.SENT);
            }
        }
    }
}
