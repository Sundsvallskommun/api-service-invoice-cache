
    create table backupinvoice (
        claim_level integer,
        invoice_amount decimal(38,2),
        invoice_created_date date,
        invoice_date date,
        invoice_due_date date,
        invoice_reminder_date date,
        paid_amount decimal(38,2),
        vat decimal(38,2),
        id bigint not null auto_increment,
        city varchar(255),
        customer_id varchar(255),
        customer_name varchar(255),
        customer_name2 varchar(255),
        customer_type varchar(255),
        file_name varchar(255),
        invoice_number varchar(255),
        invoice_reference varchar(255),
        invoice_status varchar(255),
        invoice_status2 varchar(255),
        municipality_id varchar(255),
        name varchar(255),
        ocr_number varchar(255),
        organization_number varchar(255),
        payment_status varchar(255),
        street varchar(255),
        zip varchar(255),
        primary key (id)
    ) engine=InnoDB;

    create table invoice (
        claim_level integer,
        invoice_amount decimal(38,2),
        invoice_created_date date,
        invoice_date date,
        invoice_due_date date,
        invoice_reminder_date date,
        paid_amount decimal(38,2),
        vat decimal(38,2),
        id bigint not null auto_increment,
        city varchar(255),
        customer_id varchar(255),
        customer_name varchar(255),
        customer_name2 varchar(255),
        customer_type varchar(255),
        file_name varchar(255),
        invoice_number varchar(255),
        invoice_reference varchar(255),
        invoice_status varchar(255),
        invoice_status2 varchar(255),
        municipality_id varchar(255),
        name varchar(255),
        ocr_number varchar(255),
        organization_number varchar(255),
        payment_status varchar(255),
        street varchar(255),
        zip varchar(255),
        primary key (id)
    ) engine=InnoDB;

    create table invoice_pdf (
        id integer not null auto_increment,
        created datetime(6) not null,
        filename varchar(255),
        invoice_debtor_legal_id varchar(255),
        invoice_id varchar(255),
        invoice_issuer_legal_id varchar(255),
        invoice_number varchar(255),
        municipality_id varchar(255),
        document LONGBLOB,
        invoice_type varchar(24),
        primary key (id)
    ) engine=InnoDB;

    create index backup_municipality_id_index 
       on backupinvoice (municipality_id);

    create index bak_inv_index 
       on invoice (invoice_number);

    create index bak_org_index 
       on invoice (organization_number);

    create index bak_ocr_index 
       on invoice (ocr_number);

    create index bak_cus_index 
       on invoice (customer_id);

    create index bak_mun_index 
       on invoice (municipality_id);

    create index invoice_pdf_municipality_id_index 
       on invoice_pdf (municipality_id);

    alter table if exists invoice_pdf 
       add constraint UK97gdx5bau45snxx119ad6givd unique (filename);
