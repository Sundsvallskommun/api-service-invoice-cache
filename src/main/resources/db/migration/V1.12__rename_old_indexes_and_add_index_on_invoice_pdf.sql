-- Drop and recreate/rename indexes in table "invoice"
drop index if exists bak_cus_index on invoice;
drop index if exists bak_inv_index on invoice;
drop index if exists bak_ocr_index on invoice;
drop index if exists bak_org_index on invoice;
drop index if exists bak_mun_index on invoice;

create index if not exists idx_invoice_customer_id on invoice (customer_id);
create index if not exists idx_invoice_invoice_number on invoice (invoice_number);
create index if not exists idx_invoice_ocr_number on invoice (ocr_number);
create index if not exists idx_invoice_organization_number on invoice (organization_number);
create index if not exists idx_invoice_municipality_id on invoice (municipality_id);

-- Drop and recreate/rename indexes in table "backupinvoice"
drop index if exists cus_index on backupinvoice;
drop index if exists inv_index on backupinvoice;
drop index if exists ocr_index on backupinvoice;
drop index if exists org_index on backupinvoice;
drop index if exists backup_municipality_id_index on backupinvoice;

-- We only need invoice_number since we sort on it
create index if not exists idx_backupinvoice_invoice_number on backupinvoice (invoice_number);

-- Drop and recreate/rename indexes in table "invoice_pdf"
drop index if exists invoice_debtor_legal_id_index on invoice_pdf;
drop index if exists invoice_issuer_legal_id_index on invoice_pdf;
drop index if exists invoice_pdf_invoice_id_index on invoice_pdf;
drop index if exists invoice_pdf_invoice_number_index on invoice_pdf;
drop index if exists invoice_pdf_invoice_type_index on invoice_pdf;
drop index if exists invoice_pdf_municipality_id_index on invoice_pdf;

create index if not exists idx_invoice_pdf_invoice_debtor_legal_id on invoice_pdf (invoice_debtor_legal_id);
create index if not exists idx_invoice_pdf_invoice_issuer_legal_id on invoice_pdf (invoice_issuer_legal_id);
create index if not exists idx_invoice_pdf_invoice_id on invoice_pdf (invoice_id);
create index if not exists idx_invoice_pdf_invoice_number on invoice_pdf (invoice_number);
create index if not exists idx_invoice_pdf_invoice_type on invoice_pdf (invoice_type);
create index if not exists idx_invoice_pdf_municipality_id on invoice_pdf (municipality_id);

-- Add new index on file_name in table "invoice_pdf"
create index if not exists idx_invoice_pdf_filename on invoice_pdf (filename);
