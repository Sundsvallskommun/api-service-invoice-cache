package se.sundsvall.invoicecache.api.model;

import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_ONLY;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Response with invoices")
public class InvoicesResponse {

    @JsonProperty("_meta")
    @Schema(implementation = MetaData.class, accessMode = READ_ONLY)
    private MetaData metaData;
    
    @ArraySchema(schema = @Schema(implementation = Invoice.class))
    private final List<Invoice> invoices = new ArrayList<>();

    public void addInvoice(final Invoice invoice) {
        this.invoices.add(invoice);
    }
}
