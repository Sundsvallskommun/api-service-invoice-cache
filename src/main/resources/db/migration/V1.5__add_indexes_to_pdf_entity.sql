ALTER TABLE `ms-invoicecache`.invoice_pdf add index invoice_debtor_legal_id_index(invoice_debtor_legal_id);
ALTER TABLE `ms-invoicecache`.invoice_pdf add index invoice_issuer_legal_id_index(invoice_issuer_legal_id);
ALTER TABLE `ms-invoicecache`.invoice_pdf add index invoice_pdf_invoice_id_index(invoice_id);
ALTER TABLE `ms-invoicecache`.invoice_pdf add index invoice_pdf_invoice_name_index(invoice_name);
ALTER TABLE `ms-invoicecache`.invoice_pdf add index invoice_pdf_invoice_number_index(invoice_number);
ALTER TABLE `ms-invoicecache`.invoice_pdf add index invoice_pdf_invoice_type_index(invoice_type);

