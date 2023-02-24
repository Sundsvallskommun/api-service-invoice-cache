package se.sundsvall.invoicecache.integration.db;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import se.sundsvall.invoicecache.api.model.InvoiceStatus;

class InvoiceStatusTest {
    
    @Test
    void testValuesHaveNotChanged() {
        assertEquals("Betald", InvoiceStatus.PAID.getStatus());
        assertEquals("Obetald", InvoiceStatus.UNPAID.getStatus());
        assertEquals("Delvis betald", InvoiceStatus.PARTIALLY_PAID.getStatus());
        assertEquals("Gått till inkasso", InvoiceStatus.DEBT_COLLECTION.getStatus());
        assertEquals("För mycket betalt", InvoiceStatus.PAID_TOO_MUCH.getStatus());
        assertEquals("Påminnelse", InvoiceStatus.REMINDER.getStatus());
        assertEquals("Skickad", InvoiceStatus.SENT.getStatus());
        assertEquals("Makulerad", InvoiceStatus.VOID.getStatus());
        assertEquals("Okänd", InvoiceStatus.UNKNOWN.getStatus());
        assertEquals(9, InvoiceStatus.values().length);
    }
    
    @Test
    void testUnknownStatusShouldReturnUnknown() {
        assertEquals(InvoiceStatus.UNKNOWN, InvoiceStatus.fromValue("something Else"));
    }
}