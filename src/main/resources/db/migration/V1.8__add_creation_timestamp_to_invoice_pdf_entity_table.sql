alter table `ms-invoicecache`.invoice_pdf 
	add column created datetime(6) not null default CURRENT_TIMESTAMP;

alter table `ms-invoicecache`.invoice_pdf 
	alter column created drop default;
