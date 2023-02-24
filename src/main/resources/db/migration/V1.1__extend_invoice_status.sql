ALTER TABLE `ms-invoicecache`.invoice ADD invoice_created_date DATE NULL;
ALTER TABLE `ms-invoicecache`.invoice ADD invoice_reminder_date DATE NULL;
ALTER TABLE `ms-invoicecache`.invoice ADD invoice_status2 VARCHAR(4) NULL;

ALTER TABLE `ms-invoicecache`.backupinvoice ADD invoice_created_date DATE NULL;
ALTER TABLE `ms-invoicecache`.backupinvoice ADD invoice_reminder_date DATE NULL;
ALTER TABLE `ms-invoicecache`.backupinvoice ADD invoice_status2 VARCHAR(4) NULL;