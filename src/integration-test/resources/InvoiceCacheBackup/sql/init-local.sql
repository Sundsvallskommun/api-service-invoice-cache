CREATE TABLE BATCH_JOB_INSTANCE  (
                                     JOB_INSTANCE_ID BIGINT  NOT NULL PRIMARY KEY ,
                                     VERSION BIGINT ,
                                     JOB_NAME VARCHAR(100) NOT NULL,
                                     JOB_KEY VARCHAR(32) NOT NULL,
                                     constraint JOB_INST_UN unique (JOB_NAME, JOB_KEY)
) ENGINE=InnoDB;

CREATE TABLE BATCH_JOB_EXECUTION  (
                                      JOB_EXECUTION_ID BIGINT  NOT NULL PRIMARY KEY ,
                                      VERSION BIGINT  ,
                                      JOB_INSTANCE_ID BIGINT NOT NULL,
                                      CREATE_TIME DATETIME(6) NOT NULL,
                                      START_TIME DATETIME(6) DEFAULT NULL ,
                                      END_TIME DATETIME(6) DEFAULT NULL ,
                                      STATUS VARCHAR(10) ,
                                      EXIT_CODE VARCHAR(2500) ,
                                      EXIT_MESSAGE VARCHAR(2500) ,
                                      LAST_UPDATED DATETIME(6),
                                      constraint JOB_INST_EXEC_FK foreign key (JOB_INSTANCE_ID)
                                          references BATCH_JOB_INSTANCE(JOB_INSTANCE_ID)
) ENGINE=InnoDB;

CREATE TABLE BATCH_JOB_EXECUTION_PARAMS  (
                                             JOB_EXECUTION_ID BIGINT NOT NULL ,
                                             PARAMETER_NAME VARCHAR(100) NOT NULL ,
                                             PARAMETER_TYPE VARCHAR(100) NOT NULL ,
                                             PARAMETER_VALUE VARCHAR(2500) ,
                                             IDENTIFYING CHAR(1) NOT NULL ,
                                             constraint JOB_EXEC_PARAMS_FK foreign key (JOB_EXECUTION_ID)
                                                 references BATCH_JOB_EXECUTION(JOB_EXECUTION_ID)
) ENGINE=InnoDB;

CREATE TABLE BATCH_STEP_EXECUTION  (
                                       STEP_EXECUTION_ID BIGINT  NOT NULL PRIMARY KEY ,
                                       VERSION BIGINT NOT NULL,
                                       STEP_NAME VARCHAR(100) NOT NULL,
                                       JOB_EXECUTION_ID BIGINT NOT NULL,
                                       CREATE_TIME DATETIME(6) NOT NULL,
                                       START_TIME DATETIME(6) DEFAULT NULL ,
                                       END_TIME DATETIME(6) DEFAULT NULL ,
                                       STATUS VARCHAR(10) ,
                                       COMMIT_COUNT BIGINT ,
                                       READ_COUNT BIGINT ,
                                       FILTER_COUNT BIGINT ,
                                       WRITE_COUNT BIGINT ,
                                       READ_SKIP_COUNT BIGINT ,
                                       WRITE_SKIP_COUNT BIGINT ,
                                       PROCESS_SKIP_COUNT BIGINT ,
                                       ROLLBACK_COUNT BIGINT ,
                                       EXIT_CODE VARCHAR(2500) ,
                                       EXIT_MESSAGE VARCHAR(2500) ,
                                       LAST_UPDATED DATETIME(6),
                                       constraint JOB_EXEC_STEP_FK foreign key (JOB_EXECUTION_ID)
                                           references BATCH_JOB_EXECUTION(JOB_EXECUTION_ID)
) ENGINE=InnoDB;

CREATE TABLE BATCH_STEP_EXECUTION_CONTEXT  (
                                               STEP_EXECUTION_ID BIGINT NOT NULL PRIMARY KEY,
                                               SHORT_CONTEXT VARCHAR(2500) NOT NULL,
                                               SERIALIZED_CONTEXT TEXT ,
                                               constraint STEP_EXEC_CTX_FK foreign key (STEP_EXECUTION_ID)
                                                   references BATCH_STEP_EXECUTION(STEP_EXECUTION_ID)
) ENGINE=InnoDB;

CREATE TABLE BATCH_JOB_EXECUTION_CONTEXT  (
                                              JOB_EXECUTION_ID BIGINT NOT NULL PRIMARY KEY,
                                              SHORT_CONTEXT VARCHAR(2500) NOT NULL,
                                              SERIALIZED_CONTEXT TEXT ,
                                              constraint JOB_EXEC_CTX_FK foreign key (JOB_EXECUTION_ID)
                                                  references BATCH_JOB_EXECUTION(JOB_EXECUTION_ID)
) ENGINE=InnoDB;

CREATE SEQUENCE BATCH_STEP_EXECUTION_SEQ START WITH 1 MINVALUE 1 MAXVALUE 9223372036854775806 INCREMENT BY 1 NOCACHE NOCYCLE ENGINE=InnoDB;
CREATE SEQUENCE BATCH_JOB_EXECUTION_SEQ START WITH 1 MINVALUE 1 MAXVALUE 9223372036854775806 INCREMENT BY 1 NOCACHE NOCYCLE ENGINE=InnoDB;
CREATE SEQUENCE BATCH_JOB_SEQ START WITH 1 MINVALUE 1 MAXVALUE 9223372036854775806 INCREMENT BY 1 NOCACHE NOCYCLE ENGINE=InnoDB;

create or replace table `ms-invoicecache`.backupinvoice
(
    id                    bigint auto_increment
        primary key,
    city                  varchar(255)   null,
    claim_level           int            null,
    customer_id           varchar(255)   null,
    customer_name         varchar(255)   null,
    customer_name2        varchar(255)   null,
    customer_type         varchar(255)   null,
    file_name             varchar(255)   null,
    invoice_amount        decimal(19, 2) null,
    invoice_date          date           null,
    invoice_due_date      date           null,
    invoice_number        varchar(255)   null,
    invoice_reference     varchar(255)   null,
    invoice_status        varchar(255)   null,
    name                  varchar(255)   null,
    ocr_number            varchar(255)   null,
    organization_number   varchar(255)   null,
    paid_amount           decimal(19, 2) null,
    payment_status        varchar(255)   null,
    street                varchar(255)   null,
    vat                   decimal(19, 2) null,
    zip                   varchar(255)   null,
    invoice_created_date  date           null,
    invoice_reminder_date date           null,
    invoice_status2       varchar(4)     null
);

create or replace index cus_index
    on `ms-invoicecache`.backupinvoice (customer_id);

create or replace index inv_index
    on `ms-invoicecache`.backupinvoice (invoice_number);

create or replace index ocr_index
    on `ms-invoicecache`.backupinvoice (ocr_number);

create or replace index org_index
    on `ms-invoicecache`.backupinvoice (organization_number);

create or replace table `ms-invoicecache`.flyway_schema_history
(
    installed_rank int                                   not null
        primary key,
    version        varchar(50)                           null,
    description    varchar(200)                          not null,
    type           varchar(20)                           not null,
    script         varchar(1000)                         not null,
    checksum       int                                   null,
    installed_by   varchar(100)                          not null,
    installed_on   timestamp default current_timestamp() not null,
    execution_time int                                   not null,
    success        tinyint(1)                            not null
);

create or replace index flyway_schema_history_s_idx
    on `ms-invoicecache`.flyway_schema_history (success);

create or replace table `ms-invoicecache`.invoice
(
    id                    bigint auto_increment
        primary key,
    city                  varchar(255)   null,
    claim_level           int            null,
    customer_id           varchar(255)   null,
    customer_name         varchar(255)   null,
    customer_name2        varchar(255)   null,
    customer_type         varchar(255)   null,
    file_name             varchar(255)   null,
    invoice_amount        decimal(19, 2) null,
    invoice_date          date           null,
    invoice_due_date      date           null,
    invoice_number        varchar(255)   null,
    invoice_reference     varchar(255)   null,
    invoice_status        varchar(255)   null,
    name                  varchar(255)   null,
    ocr_number            varchar(255)   null,
    organization_number   varchar(255)   null,
    paid_amount           decimal(19, 2) null,
    payment_status        varchar(255)   null,
    street                varchar(255)   null,
    vat                   decimal(19, 2) null,
    zip                   varchar(255)   null,
    invoice_created_date  date           null,
    invoice_reminder_date date           null,
    invoice_status2       varchar(4)     null
);

create or replace index bak_cus_index
    on `ms-invoicecache`.invoice (customer_id);

create or replace index bak_inv_index
    on `ms-invoicecache`.invoice (invoice_number);

create or replace index bak_ocr_index
    on `ms-invoicecache`.invoice (ocr_number);

create or replace index bak_org_index
    on `ms-invoicecache`.invoice (organization_number);

create or replace table `ms-invoicecache`.invoice_pdf
(
    id                      int auto_increment primary key,
    created datetime(6)     not null,
    document                longblob     null,
    filename                varchar(255) null,
    invoice_issuer_legal_id varchar(255) null,
    invoice_debtor_legal_id varchar(255) null,
    invoice_number          varchar(255) null,
    invoice_type            varchar(24)  null,
    invoice_id              varchar(255) null,
    document_backup         longtext     null,
    constraint UK_efwb3ex96plme92eseluwks26
        unique (filename)
)
    charset = latin1;

create or replace index invoice_debtor_legal_id_index
    on `ms-invoicecache`.invoice_pdf (invoice_debtor_legal_id);

create or replace index invoice_issuer_legal_id_index
    on `ms-invoicecache`.invoice_pdf (invoice_issuer_legal_id);

create or replace index invoice_pdf_invoice_id_index
    on `ms-invoicecache`.invoice_pdf (invoice_id);

create or replace index invoice_pdf_invoice_number_index
    on `ms-invoicecache`.invoice_pdf (invoice_number);

create or replace index invoice_pdf_invoice_type_index
    on `ms-invoicecache`.invoice_pdf (invoice_type);

INSERT INTO `ms-invoicecache`.backupinvoice (city, claim_level, customer_id, customer_name,
                                             customer_name2, customer_type, file_name,
                                             invoice_amount, invoice_date, invoice_due_date,
                                             invoice_number, invoice_reference, invoice_status,
                                             name, ocr_number, organization_number, paid_amount,
                                             payment_status, street, zip, vat, invoice_reminder_date)
VALUES ('ANKEBORG', 0, '9512245032', 'Jalle Jallesson', NULL, 'KA', NULL, 1740.00, '2022-08-16',
        '2022-09-16', '53626810', 'Something Else', 'Obetald', NULL, '2300122208180015699',
        '5591628137', 0.00, '', 'Ankeborgsv 1A', '987 52', 0.00, '2022-09-21'),
       ('SUNDSVALL', 2, '9512245030', 'Janne Jannesson', 'c/o Kalle', 'AV',
        'Faktura_504013_to_9512245030.pdf', 250.00, '2022-03-14', '2022-03-31', '504013',
        'Something Utbildning', 'Obetald', NULL, '45673456', '5591628135', -250.00, '',
        'Vägen 5 A LGH 1111', '123 45', 0.00, '2022-09-21'),
       ('SUNDSVALL', 2, '9512245030', 'Janne Jannesson', 'c/o Kalle', 'AV',
        'Faktura_504013_to_9512245030.pdf', 250.00, '2022-03-14', '2022-03-31', '504013',
        'Something Utbildning', 'Obetald', NULL, '45673456', '5591628135', -250.00, '',
        'Vägen 5 A LGH 1111', '123 45', 0.00, '2022-09-21'),
       ('ANKEBORG', 0, '9512245031', 'Kalle Kallesson', '', 'BO',
        'Faktura_53626897_to_9512245031.pdf', 1500.00, '2022-08-06', '2022-08-31', '53626897',
        'Something Else', 'Betald', NULL, '34563457', '5591628136', -1500.00, 'JUST',
        'Ankeborgsvägen 5 B LGH 1111', '987 62', 0.00, NULL),
       ('ANKEBORG', 0, '9512245031', 'Kalle Kallesson', '', 'BO',
        'Faktura_53626898_to_9512245031.pdf', 1500.00, '2022-08-06', '2022-08-31', '53626898',
        'Something Else', 'Delvis betald', NULL, '34563458', '5591628136', -740.00, 'DELB',
        'Ankeborgsvägen 5 B LGH 1111', '987 62', 0.00, NULL),
       ('ANKEBORG', 0, '9512245031', 'Kalle Kallesson', '', 'BO',
        'Faktura_53626899_to_9512245031.pdf', 1500.00, '2022-08-06', '2022-08-31', '53626899', '',
        'För mycket betalt', NULL, '34563459', '5591628136', -1600.00, 'ÖVER',
        'Ankeborgsvägen 5 B LGH 1111', '987 62', 0.00, NULL),
       ('ANKEBORG', 0, '9512245031', 'Kalle Kallesson', '', 'BO',
        'Faktura_53626800_to_9512245031.pdf', 1500.00, '2022-08-06', '2022-09-06', '53626800',
        'Something Else', 'Obetald', NULL, '34563460', '5591628136', 0.00, '',
        'Ankeborgsvägen 5 B LGH 1111', '987 62', 300.00, '2022-09-11'),
       ('ANKEBORG', 0, '9512245031', 'Kalle Kallesson', '', 'BO',
        'Faktura_53626801_to_9512245031.pdf', 1500.00, '2022-08-07', '2022-09-07', '53626801',
        'Something Else', 'Obetald', NULL, '34563461', '5591628136', 0.00, '',
        'Ankeborgsvägen 5 B LGH 1111', '987 62', 300.00, '2022-09-12'),
       ('ANKEBORG', 0, '9512245031', 'Kalle Kallesson', '', 'BO',
        'Faktura_53626802_to_9512245031.pdf', 1500.00, '2022-08-08', '2022-09-08', '53626802',
        'Something Else', 'Obetald', NULL, '34563462', '5591628136', 0.00, '',
        'Ankeborgsvägen 5 B LGH 1111', '987 62', 300.00, '2022-09-13'),
       ('ANKEBORG', 0, '9512245031', 'Kalle Kallesson', '', 'BO',
        'Faktura_53626803_to_9512245031.pdf', 1500.00, '2022-08-09', '2022-09-09', '53626803',
        'Something Else', 'Obetald', NULL, '34563463', '5591628136', 0.00, '',
        'Ankeborgsvägen 5 B LGH 1111', '987 62', 300.00, '2022-09-14'),
       ('ANKEBORG', 0, '9512245031', 'Kalle Kallesson', '', 'BO',
        'Faktura_53626804_to_9512245031.pdf', 1500.00, '2022-08-10', '2022-09-10', '53626804', '',
        'Obetald', NULL, '34563464', '5591628136', 0.00, '', 'Ankeborgsvägen 5 B LGH 1111',
        '987 62', 300.00, '2022-09-15'),
       ('ANKEBORG', 0, '9512245031', 'Kalle Kallesson', '', 'BO',
        'Faktura_53626805_to_9512245031.pdf', 1500.00, '2022-08-11', '2022-09-11', '53626805',
        'Something Else', 'Obetald', NULL, '34563465', '5591628136', 0.00, '',
        'Ankeborgsvägen 5 B LGH 1111', '987 62', 0.00, '2022-09-16'),
       ('ANKEBORG', 0, '9512245031', 'Kalle Kallesson', '', 'BO',
        'Faktura_53626806_to_9512245031.pdf', 1500.00, '2022-08-12', '2022-09-12', '53626806',
        'Something Else', 'Obetald', NULL, '34563466', '5591628136', 0.00, '',
        'Ankeborgsvägen 5 B LGH 1111', '987 62', 0.00, '2022-09-17'),
       ('ANKEBORG', 0, '9512245031', 'Kalle Kallesson', '', 'BO',
        'Faktura_53626807_to_9512245031.pdf', 1500.00, '2022-08-13', '2022-09-13', '53626807',
        'Something Else', 'Obetald', NULL, '34563467', '5591628136', 0.00, '',
        'Ankeborgsvägen 5 B LGH 1111', '987 62', 0.00, '2022-09-18'),
       ('ANKEBORG', 0, '9512245031', 'Kalle Kallesson', '', 'BO',
        'Faktura_53626808_to_9512245031.pdf', 1500.00, '2022-08-14', '2022-09-14', '53626808',
        'Something Else', 'Obetald', NULL, '34563468', '5591628136', 0.00, '',
        'Ankeborgsvägen 5 B LGH 1111', '987 62', 0.00, '2022-09-19');