package se.sundsvall.invoicecache.integration.db.entity;


import java.sql.Blob;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

import se.sundsvall.invoicecache.api.model.InvoiceType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "invoice_pdf")
@Getter
@Setter
@Builder(setterPrefix = "with")
@NoArgsConstructor
@AllArgsConstructor
public class PdfEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "filename", unique = true)
    private String filename;

    @Lob
    @Column(name = "document")
    private Blob document;

    @Enumerated(EnumType.STRING)
    @Column(name = "invoice_type", length = 24)
    private InvoiceType invoiceType;

    @Column(name = "invoice_issuer_legal_id")
    private String invoiceIssuerLegalId;

    @Column(name = "invoice_debtor_legal_id")
    private String invoiceDebtorLegalId;

    @Column(name = "invoice_number")
    private String invoiceNumber;

    @Column(name = "invoice_id")
    private String invoiceId;
}
