-- Since invoice_name is the same as filename it's not needed anymore
alter table `ms-invoicecache`.invoice_pdf drop column `invoice_name`;

-- Drop the index as well
drop index if exists invoice_pdf_invoice_name_index on `ms-invoicecache`.invoice_pdf;