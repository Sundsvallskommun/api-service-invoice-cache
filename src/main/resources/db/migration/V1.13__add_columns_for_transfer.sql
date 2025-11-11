ALTER TABLE invoice_pdf
    ADD COLUMN file_hash VARCHAR(64);

ALTER TABLE invoice_pdf
    ADD COLUMN moved_to_samba_at DATETIME(6) DEFAULT NULL;

ALTER TABLE invoice_pdf
    ADD COLUMN blob_truncated_at DATETIME(6) DEFAULT NULL;
