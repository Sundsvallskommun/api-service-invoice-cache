package se.sundsvall.invoicecache.api.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class InvoiceTypeTest {

    @Test
    void testValuesHaveNotChanged() {
        assertEquals(7, InvoiceType.values().length);

        assertEquals("Faktura", InvoiceType.INVOICE.getType());
        assertEquals("Kreditfaktura", InvoiceType.CREDIT_INVOICE.getType());
        assertEquals("Autogiro", InvoiceType.DIRECT_DEBIT.getType());
        assertEquals("Självfaktura", InvoiceType.SELF_INVOICE.getType());
        assertEquals("Påminnelse", InvoiceType.REMINDER.getType());
        assertEquals("Samlingsfaktura", InvoiceType.CONSOLIDATED_INVOICE.getType());
        assertEquals("Slutfaktura", InvoiceType.FINAL_INVOICE.getType());
    }
}
