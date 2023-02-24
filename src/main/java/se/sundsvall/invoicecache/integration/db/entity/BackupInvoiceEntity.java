package se.sundsvall.invoicecache.integration.db.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Basically a copy of InvoiceEntity. Since it's messy with inheritance and JPA
 */
@Entity
@Table(name = "backupinvoice")
@Getter
@Setter
@Builder(setterPrefix = "with")
@NoArgsConstructor
@AllArgsConstructor
public class BackupInvoiceEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "city")
    private String city;
    
    @Column(name = "claim_level")
    private int claimLevel;
    
    @Column(name = "customer_id")
    private String customerId;
    
    @Column(name = "customer_name")
    private String customerName;
    
    @Column(name = "customer_name2")
    private String customerName2;
    
    @Column(name = "customer_type")
    private String customerType;
    
    @Column(name = "fileName")
    private String fileName;
    
    @Column(name = "invoice_amount")
    private BigDecimal invoiceAmount;
    
    @Column(name = "invoice_date")
    private LocalDate invoiceDate;
    
    @Column(name = "invoice_due_date")
    private LocalDate invoiceDueDate;
    
    @Column(name = "invoice_number")
    private String invoiceNumber;
    
    @Column(name = "invoice_reminder_date")
    private LocalDate invoiceReminderDate;

    @Column(name = "invoice_created_date")
    private LocalDate invoiceCreatedDate;

    @Column(name = "invoice_reference")
    private String invoiceReference;
    
    @Column(name = "invoice_status")
    private String invoiceStatus;
    
    @Column(name = "invoice_status2")
    private String invoiceStatus2;

    @Column(name = "name")
    private String name;
    
    @Column(name = "ocr_number")
    private String ocrNumber;
    
    @Column(name = "organization_number")
    private String organizationNumber;
    
    @Column(name = "paid_amount")
    private BigDecimal paidAmount;
    
    @Column(name = "payment_status")
    private String paymentStatus;
    
    @Column(name = "street")
    private String street;
    
    @Column(name = "vat")
    private BigDecimal vat;
    
    @Column(name = "zip")
    private String zip;
}
