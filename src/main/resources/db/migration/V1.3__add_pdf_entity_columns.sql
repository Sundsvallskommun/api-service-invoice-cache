ALTER TABLE `ms-invoicecache`.invoice_pdf ADD COLUMN `invoice_issuer_legal_id` VARCHAR(255) DEFAULT NULL;
ALTER TABLE `ms-invoicecache`.invoice_pdf ADD COLUMN `invoice_debtor_legal_id` VARCHAR(255) DEFAULT NULL;
ALTER TABLE `ms-invoicecache`.invoice_pdf ADD COLUMN `invoice_number` VARCHAR(255) DEFAULT NULL;
ALTER TABLE `ms-invoicecache`.invoice_pdf ADD COLUMN `invoice_name` VARCHAR(255) DEFAULT NULL;
ALTER TABLE `ms-invoicecache`.invoice_pdf ADD COLUMN `invoice_type` VARCHAR(24) DEFAULT NULL;
