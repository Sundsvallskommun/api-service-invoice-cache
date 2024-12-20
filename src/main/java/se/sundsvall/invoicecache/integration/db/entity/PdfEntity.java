package se.sundsvall.invoicecache.integration.db.entity;

import static java.time.OffsetDateTime.now;
import static java.time.ZoneId.systemDefault;
import static java.time.temporal.ChronoUnit.MILLIS;
import static org.hibernate.annotations.TimeZoneStorageType.NORMALIZE;

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
import jakarta.persistence.UniqueConstraint;
import java.sql.Blob;
import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.TimeZoneStorage;
import se.sundsvall.invoicecache.api.model.InvoiceType;

@Entity
@Table(name = "invoice_pdf",
	indexes = {
		@Index(name = "idx_invoice_debtor_legal_id", columnList = "invoice_debtor_legal_id"),
		@Index(name = "idx_invoice_issuer_legal_id", columnList = "invoice_issuer_legal_id"),
		@Index(name = "idx_invoice_id", columnList = "invoice_id"),
		@Index(name = "idx_invoice_number", columnList = "invoice_number"),
		@Index(name = "idx_invoice_type", columnList = "invoice_type"),
		@Index(name = "idx_municipality_id", columnList = "municipality_id"),
		@Index(name = "idx_filename", columnList = "filename")
	},
	uniqueConstraints = {
		@UniqueConstraint(name = "uk_filename", columnNames = "filename")
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

	@Column(name = "filename", nullable = false)
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
