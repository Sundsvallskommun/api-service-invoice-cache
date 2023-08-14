package se.sundsvall.invoicecache.integration.db.entity;

import java.sql.Blob;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import se.sundsvall.invoicecache.api.model.InvoiceType;

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
	@Column(name = "document", columnDefinition = "LONGBLOB")
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
