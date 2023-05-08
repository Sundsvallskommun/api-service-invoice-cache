package se.sundsvall.invoicecache.integration.smb;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.sql.Blob;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

import org.hibernate.engine.jdbc.BlobProxy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import se.sundsvall.invoicecache.integration.db.InvoiceEntityRepository;
import se.sundsvall.invoicecache.integration.db.PdfEntityRepository;
import se.sundsvall.invoicecache.integration.db.entity.InvoiceEntity;
import se.sundsvall.invoicecache.integration.db.entity.PdfEntity;

import jcifs.smb.SmbFileInputStream;

@ExtendWith({MockitoExtension.class, OutputCaptureExtension.class})
class SMBIntegrationTest {
    
    private final static String INVOICE_ISSUER_LEGAL_ID = "2120002411";
    
    @Mock(answer = Answers.CALLS_REAL_METHODS)
    SMBProperties smbProperties;
    
    @Mock
    private PdfEntityRepository pdfRepository;
    
    @Mock
    private InvoiceEntityRepository invoiceEntityRepository;
    
    @InjectMocks
    private SMBIntegration smbIntegration;
    
    @Test
    void findPdf_successfully() throws IOException {
        
        Blob blob = BlobProxy.generateProxy("blobMe".getBytes());
        when(smbProperties.getRemoteDir()).thenReturn("TEST");
        when(invoiceEntityRepository.findByFileName(any(String.class)))
            .thenReturn(Optional.ofNullable(InvoiceEntity.builder()
                .withOrganizationNumber("someOrgNr")
                .withInvoiceNumber("someInvoiceNumber")
                .build()));
        
        when(pdfRepository.save(any())).thenReturn(PdfEntity.builder()
            .withFilename("test.pdf")
            .withDocument(blob)
            .withInvoiceDebtorLegalId("someOrgnr")
            .withInvoiceIssuerLegalId(INVOICE_ISSUER_LEGAL_ID)
            .withInvoiceNumber("someInvoiceNumber")
            .withId(1)
            .build());
        
        try (MockedConstruction<SmbFileInputStream> myobjectMockedConstruction = Mockito.mockConstruction(SmbFileInputStream.class,
            (mock, context) -> {
                when(mock.readAllBytes()).thenReturn(new byte[]{});//any additional mocking
            })) {
            
            var result = smbIntegration.findPdf("test.pdf");
            
            assertThat(result).isNotNull();
            assertThat(result.getFilename()).isEqualTo("test.pdf");
            assertThat(result.getId()).isEqualTo(1);
            assertThat(result.getDocument()).isEqualTo(blob);
            
            assertThat(result.getInvoiceIssuerLegalId()).isEqualTo(INVOICE_ISSUER_LEGAL_ID);
            assertThat(result.getInvoiceDebtorLegalId()).isEqualTo("someOrgnr");
            assertThat(result.getInvoiceNumber()).isEqualTo("someInvoiceNumber");
            
            
            assertThat(myobjectMockedConstruction.constructed()).hasSize(1);
            SmbFileInputStream mock = myobjectMockedConstruction.constructed().get(0);
            verify(mock, times(1)).readAllBytes();
            verify(pdfRepository, times(1)).save(any());
            verifyNoMoreInteractions(pdfRepository);
        }
    }
    
    @Test
    void handleFile_ThrowsError(CapturedOutput output) {
        smbIntegration.findPdf("");
        assertThat(output).contains("Something went wrong when trying to save file");
        verifyNoInteractions(pdfRepository);
    }
    
    @Test
    void tryCache_ThrowsError(CapturedOutput output) {
        smbIntegration.cacheInvoicePdfs();
        assertThat(output).contains("Something went wrong when trying to cache pdf");
    }
    
    @Test
    void isDateAfterYesterday_true() {
        var result = SMBIntegration.isAfterYesterday(System.currentTimeMillis());
        assertThat(result).isTrue();
    }
    
    @Test
    void isDateAfterYesterday_False() {
        var testValue = LocalDateTime.now().minusDays(2).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        var result = SMBIntegration.isAfterYesterday(testValue);
        assertThat(result).isFalse();
    }
}
