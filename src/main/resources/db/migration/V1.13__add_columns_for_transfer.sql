ALTER TABLE invoice_pdf
    ADD COLUMN file_hash VARCHAR(64);

ALTER TABLE invoice_pdf
    ADD COLUMN moved_to_samba_at DATETIME(6) DEFAULT NULL;

ALTER TABLE invoice_pdf
    ADD COLUMN blob_truncated_at DATETIME(6) DEFAULT NULL;

CREATE INDEX idx_pdf_moved_created_issuer
    ON invoice_pdf (moved_to_samba_at, created, invoice_issuer_legal_id);

CREATE INDEX idx_truncated_moved
    ON invoice_pdf (blob_truncated_at, moved_to_samba_at);
