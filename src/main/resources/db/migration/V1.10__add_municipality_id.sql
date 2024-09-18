alter table backupinvoice
    add column municipality_id varchar(255);

alter table invoice
    add column municipality_id varchar(255);

alter table invoice_pdf
    add column municipality_id varchar(255);

create index bak_mun_index
    on invoice (municipality_id);

create index backup_municipality_id_index
    on backupinvoice (municipality_id);
