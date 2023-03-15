package se.sundsvall.invoicecache.api.model;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Invoice pdf filter request model")
public class InvoicePdfFilterRequest {
    
    @Schema(description = "Invoice debtor legal id")
    String debtorLegalId;
    
    @Schema(description = "Invoice id")
    String invoiceId;
    
    @Schema(description = "Invoice name")
    String invoiceName;
    
    @Schema(description = "Invoice type")
    InvoiceType invoiceType;
    
}
