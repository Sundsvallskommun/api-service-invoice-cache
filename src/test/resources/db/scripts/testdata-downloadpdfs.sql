-- Test data for downloadInvoicePdfs integration tests

-- Single PDF test: One PDF with invoice_issuer_legal_id='single-issuer', invoice_number='SINGLE123'
INSERT INTO invoice_pdf(id, blob_truncated_at, created, moved_to_samba_at, file_hash, filename, invoice_debtor_legal_id,
                        invoice_id, invoice_issuer_legal_id, invoice_number, municipality_id, document, invoice_type)
VALUES (100, NULL, '2024-01-15 10:00:00.000', NULL, NULL, 'single-invoice.pdf', 'debtor-single', 'inv-single-001',
        'single-issuer', 'SINGLE123', '2281',
        0x255044462D312E340A312030206F626A0A3C3C2F547970652F436174616C6F672F50616765732032203020523E3E0A656E646F626A0A,
        'INVOICE');

-- Multiple PDFs test: Two PDFs with invoice_issuer_legal_id='multi-issuer', invoice_number='MULTI123'
-- First PDF: INVOICE type
INSERT INTO invoice_pdf(id, blob_truncated_at, created, moved_to_samba_at, file_hash, filename, invoice_debtor_legal_id,
                        invoice_id, invoice_issuer_legal_id, invoice_number, municipality_id, document, invoice_type)
VALUES (101, NULL, '2024-01-15 10:00:00.000', NULL, NULL, 'multi-invoice-1.pdf', 'debtor-multi', 'inv-multi-001',
        'multi-issuer', 'MULTI123', '2281',
        0x255044462D312E340A312030206F626A0A3C3C2F547970652F436174616C6F672F50616765732032203020523E3E0A656E646F626A0A,
        'INVOICE');

-- Second PDF: REMINDER type
INSERT INTO invoice_pdf(id, blob_truncated_at, created, moved_to_samba_at, file_hash, filename, invoice_debtor_legal_id,
                        invoice_id, invoice_issuer_legal_id, invoice_number, municipality_id, document, invoice_type)
VALUES (102, NULL, '2024-01-15 10:00:00.000', NULL, NULL, 'multi-invoice-2.pdf', 'debtor-multi', 'inv-multi-002',
        'multi-issuer', 'MULTI123', '2281',
        0x255044462D312E340A312030206F626A0A3C3C2F547970652F436174616C6F672F50616765732032203020523E3E0A656E646F626A0A,
        'REMINDER');
