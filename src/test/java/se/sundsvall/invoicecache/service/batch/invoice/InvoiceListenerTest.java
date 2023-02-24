package se.sundsvall.invoicecache.service.batch.invoice;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import se.sundsvall.invoicecache.integration.db.InvoiceEntityRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InvoiceListenerTest {
    
    @Mock
    private InvoiceEntityRepository mockRepository;
    
    @Mock
    private StepExecution mockStepExecution;
    
    private InvoiceListener invoiceListener;
    
    @BeforeEach
    void setup() {
        this.invoiceListener = new InvoiceListener(mockRepository);
        when(mockRepository.count()).thenReturn(1L);
    }
    
    @Test
    void testBeforeStep() {
        
        doNothing().when(mockRepository).deleteAllInBatch();
        invoiceListener.beforeStep(mockStepExecution);
        
        verify(mockRepository, times(1)).count();
        verify(mockRepository, times(1)).deleteAllInBatch();
    }
    
    @Test
    void testSuccessfulAfterStep() {
        when(mockStepExecution.getExitStatus()).thenReturn(ExitStatus.COMPLETED);
        final ExitStatus exitStatus = invoiceListener.afterStep(mockStepExecution);
        assertNull(exitStatus); //intended
        verify(mockStepExecution, times(0)).getSummary();
    }
    
    @Test
    void testFailedAfterStep() {
        when(mockStepExecution.getExitStatus()).thenReturn(ExitStatus.FAILED);
        final ExitStatus exitStatus = invoiceListener.afterStep(mockStepExecution);
        assertNull(exitStatus); //intended
        verify(mockStepExecution, times(1)).getSummary();
    }
    
}