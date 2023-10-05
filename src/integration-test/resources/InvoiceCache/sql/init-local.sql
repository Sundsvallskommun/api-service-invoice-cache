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

