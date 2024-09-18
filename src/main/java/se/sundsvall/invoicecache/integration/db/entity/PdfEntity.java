package se.sundsvall.invoicecache.integration.db.entity;

import static java.time.OffsetDateTime.now;
import static java.time.ZoneId.systemDefault;
import static java.time.temporal.ChronoUnit.MILLIS;
import static org.hibernate.annotations.TimeZoneStorageType.NORMALIZE;

import java.sql.Blob;
import java.time.OffsetDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Lob;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import org.hibernate.annotations.TimeZoneStorage;

import se.sundsvall.invoicecache.api.model.InvoiceType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "invoice_pdf",
	indexes = {
		@Index(name = "invoice_pdf_municipality_id_index", columnList = "municipality_id"),

	})
@Getter
@Setter
@Builder(setterPrefix = "with")
@NoArgsConstructor
@AllArgsConstructor
public class PdfEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(name = "municipality_id")
	private String municipalityId;

	@Column(name = "filename", unique = true)
	private String filename;

	@Lob
	@Column(name = "document", columnDefinition = "LONGBLOB")
	private Blob document;

	@Enumerated(EnumType.STRING)
	@Column(name = "invoice_type", length = 24, columnDefinition = "varchar(24)")
	private InvoiceType invoiceType;

	@Column(name = "invoice_issuer_legal_id")
	private String invoiceIssuerLegalId;

	@Column(name = "invoice_debtor_legal_id")
	private String invoiceDebtorLegalId;

	@Column(name = "invoice_number")
	private String invoiceNumber;

	@Column(name = "invoice_id")
	private String invoiceId;

	@Column(name = "created", nullable = false)
	@TimeZoneStorage(NORMALIZE)
	private OffsetDateTime created;

	@PrePersist
	void onCreate() {
		created = now(systemDefault()).truncatedTo(MILLIS);
	}

}
