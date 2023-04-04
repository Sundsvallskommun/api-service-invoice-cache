CREATE TABLE BATCH_JOB_INSTANCE
(
    JOB_INSTANCE_ID BIGINT       NOT NULL PRIMARY KEY,
    VERSION         BIGINT,
    JOB_NAME        VARCHAR(100) NOT NULL,
    JOB_KEY         VARCHAR(32)  NOT NULL,
    constraint JOB_INST_UN unique (JOB_NAME, JOB_KEY)
) ENGINE = InnoDB;

CREATE TABLE BATCH_JOB_EXECUTION
(
    JOB_EXECUTION_ID           BIGINT        NOT NULL PRIMARY KEY,
    VERSION                    BIGINT,
    JOB_INSTANCE_ID            BIGINT        NOT NULL,
    CREATE_TIME                DATETIME(6)   NOT NULL,
    START_TIME                 DATETIME(6) DEFAULT NULL,
    END_TIME                   DATETIME(6) DEFAULT NULL,
    STATUS                     VARCHAR(10),
    EXIT_CODE                  VARCHAR(2500),
    EXIT_MESSAGE               VARCHAR(2500),
    LAST_UPDATED               DATETIME(6),
    JOB_CONFIGURATION_LOCATION VARCHAR(2500) NULL,
    constraint JOB_INST_EXEC_FK foreign key (JOB_INSTANCE_ID)
        references BATCH_JOB_INSTANCE (JOB_INSTANCE_ID)
) ENGINE = InnoDB;

CREATE TABLE BATCH_JOB_EXECUTION_PARAMS
(
    JOB_EXECUTION_ID BIGINT       NOT NULL,
    TYPE_CD          VARCHAR(6)   NOT NULL,
    KEY_NAME         VARCHAR(100) NOT NULL,
    STRING_VAL       VARCHAR(250),
    DATE_VAL         DATETIME(6) DEFAULT NULL,
    LONG_VAL         BIGINT,
    DOUBLE_VAL       DOUBLE PRECISION,
    IDENTIFYING      CHAR(1)      NOT NULL,
    constraint JOB_EXEC_PARAMS_FK foreign key (JOB_EXECUTION_ID)
        references BATCH_JOB_EXECUTION (JOB_EXECUTION_ID)
) ENGINE = InnoDB;

CREATE TABLE BATCH_STEP_EXECUTION
(
    STEP_EXECUTION_ID  BIGINT       NOT NULL PRIMARY KEY,
    VERSION            BIGINT       NOT NULL,
    STEP_NAME          VARCHAR(100) NOT NULL,
    JOB_EXECUTION_ID   BIGINT       NOT NULL,
    START_TIME         DATETIME(6)  NOT NULL,
    END_TIME           DATETIME(6) DEFAULT NULL,
    STATUS             VARCHAR(10),
    COMMIT_COUNT       BIGINT,
    READ_COUNT         BIGINT,
    FILTER_COUNT       BIGINT,
    WRITE_COUNT        BIGINT,
    READ_SKIP_COUNT    BIGINT,
    WRITE_SKIP_COUNT   BIGINT,
    PROCESS_SKIP_COUNT BIGINT,
    ROLLBACK_COUNT     BIGINT,
    EXIT_CODE          VARCHAR(2500),
    EXIT_MESSAGE       VARCHAR(2500),
    LAST_UPDATED       DATETIME(6),
    constraint JOB_EXEC_STEP_FK foreign key (JOB_EXECUTION_ID)
        references BATCH_JOB_EXECUTION (JOB_EXECUTION_ID)
) ENGINE = InnoDB;

CREATE TABLE BATCH_STEP_EXECUTION_CONTEXT
(
    STEP_EXECUTION_ID  BIGINT        NOT NULL PRIMARY KEY,
    SHORT_CONTEXT      VARCHAR(2500) NOT NULL,
    SERIALIZED_CONTEXT TEXT,
    constraint STEP_EXEC_CTX_FK foreign key (STEP_EXECUTION_ID)
        references BATCH_STEP_EXECUTION (STEP_EXECUTION_ID)
) ENGINE = InnoDB;

CREATE TABLE BATCH_JOB_EXECUTION_CONTEXT
(
    JOB_EXECUTION_ID   BIGINT        NOT NULL PRIMARY KEY,
    SHORT_CONTEXT      VARCHAR(2500) NOT NULL,
    SERIALIZED_CONTEXT TEXT,
    constraint JOB_EXEC_CTX_FK foreign key (JOB_EXECUTION_ID)
        references BATCH_JOB_EXECUTION (JOB_EXECUTION_ID)
) ENGINE = InnoDB;

CREATE TABLE BATCH_STEP_EXECUTION_SEQ
(
    ID         BIGINT  NOT NULL,
    UNIQUE_KEY CHAR(1) NOT NULL,
    constraint UNIQUE_KEY_UN unique (UNIQUE_KEY)
) ENGINE = InnoDB;

INSERT INTO BATCH_STEP_EXECUTION_SEQ (ID, UNIQUE_KEY)
select *
from (select 0 as ID, '0' as UNIQUE_KEY) as tmp
where not exists(select * from BATCH_STEP_EXECUTION_SEQ);

CREATE TABLE BATCH_JOB_EXECUTION_SEQ
(
    ID         BIGINT  NOT NULL,
    UNIQUE_KEY CHAR(1) NOT NULL,
    constraint UNIQUE_KEY_UN unique (UNIQUE_KEY)
) ENGINE = InnoDB;

INSERT INTO BATCH_JOB_EXECUTION_SEQ (ID, UNIQUE_KEY)
select *
from (select 0 as ID, '0' as UNIQUE_KEY) as tmp
where not exists(select * from BATCH_JOB_EXECUTION_SEQ);

CREATE TABLE BATCH_JOB_SEQ
(
    ID         BIGINT  NOT NULL,
    UNIQUE_KEY CHAR(1) NOT NULL,
    constraint UNIQUE_KEY_UN unique (UNIQUE_KEY)
) ENGINE = InnoDB;

INSERT INTO BATCH_JOB_SEQ (ID, UNIQUE_KEY)
select *
from (select 0 as ID, '0' as UNIQUE_KEY) as tmp
where not exists(select * from BATCH_JOB_SEQ);

-- Invoice-related tables

CREATE TABLE backupinvoice
(
    `id`                    bigint(20) NOT NULL AUTO_INCREMENT,
    `city`                  varchar(255)   DEFAULT NULL,
    `claim_level`           int(11)        DEFAULT NULL,
    `customer_id`           varchar(255)   DEFAULT NULL,
    `customer_name`         varchar(255)   DEFAULT NULL,
    `customer_name2`        varchar(255)   DEFAULT NULL,
    `customer_type`         varchar(255)   DEFAULT NULL,
    `file_name`             varchar(255)   DEFAULT NULL,
    `invoice_amount`        decimal(19, 2) DEFAULT NULL,
    `invoice_created_date`  date           DEFAULT NULL,
    `invoice_date`          date           DEFAULT NULL,
    `invoice_due_date`      date           DEFAULT NULL,
    `invoice_number`        varchar(255)   DEFAULT NULL,
    `invoice_reference`     varchar(255)   DEFAULT NULL,
    `invoice_reminder_date` date           DEFAULT NULL,
    `invoice_status`        varchar(255)   DEFAULT NULL,
    `invoice_status2`       varchar(255)   DEFAULT NULL,
    `name`                  varchar(255)   DEFAULT NULL,
    `ocr_number`            varchar(255)   DEFAULT NULL,
    `organization_number`   varchar(255)   DEFAULT NULL,
    `paid_amount`           decimal(19, 2) DEFAULT NULL,
    `payment_status`        varchar(255)   DEFAULT NULL,
    `street`                varchar(255)   DEFAULT NULL,
    `vat`                   decimal(19, 2) DEFAULT NULL,
    `zip`                   varchar(255)   DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `inv_index` (`invoice_number`),
    KEY `org_index` (`organization_number`),
    KEY `ocr_index` (`ocr_number`),
    KEY `cus_index` (`customer_id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 18548587
  DEFAULT CHARSET = utf8mb4;


-- `ms-invoicecache`.invoice definition

CREATE TABLE invoice
(
    `id`                    bigint(20) NOT NULL AUTO_INCREMENT,
    `city`                  varchar(255)   DEFAULT NULL,
    `claim_level`           int(11)        DEFAULT NULL,
    `customer_id`           varchar(255)   DEFAULT NULL,
    `customer_name`         varchar(255)   DEFAULT NULL,
    `customer_name2`        varchar(255)   DEFAULT NULL,
    `customer_type`         varchar(255)   DEFAULT NULL,
    `file_name`             varchar(255)   DEFAULT NULL,
    `invoice_amount`        decimal(19, 2) DEFAULT NULL,
    `invoice_created_date`  date           DEFAULT NULL,
    `invoice_date`          date           DEFAULT NULL,
    `invoice_due_date`      date           DEFAULT NULL,
    `invoice_number`        varchar(255)   DEFAULT NULL,
    `invoice_reference`     varchar(255)   DEFAULT NULL,
    `invoice_reminder_date` date           DEFAULT NULL,
    `invoice_status`        varchar(255)   DEFAULT NULL,
    `invoice_status2`       varchar(255)   DEFAULT NULL,
    `name`                  varchar(255)   DEFAULT NULL,
    `ocr_number`            varchar(255)   DEFAULT NULL,
    `organization_number`   varchar(255)   DEFAULT NULL,
    `paid_amount`           decimal(19, 2) DEFAULT NULL,
    `payment_status`        varchar(255)   DEFAULT NULL,
    `street`                varchar(255)   DEFAULT NULL,
    `vat`                   decimal(19, 2) DEFAULT NULL,
    `zip`                   varchar(255)   DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `bak_inv_index` (`invoice_number`),
    KEY `bak_org_index` (`organization_number`),
    KEY `bak_ocr_index` (`ocr_number`),
    KEY `bak_cus_index` (`customer_id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 18391587
  DEFAULT CHARSET = utf8mb4;


-- `ms-invoicecache`.pdf_entity definition

CREATE TABLE `invoice_pdf`
(
    `id`       int(11) NOT NULL AUTO_INCREMENT,
    `document` longblob     DEFAULT NULL,
    `filename` varchar(255) DEFAULT NULL,
    `invoice_issuer_legal_id` VARCHAR(255) DEFAULT NULL,
    `invoice_debtor_legal_id` VARCHAR(255) DEFAULT NULL,
    `invoice_number` VARCHAR(255) DEFAULT NULL,
    `invoice_id` VARCHAR(255) DEFAULT NULL,
    `invoice_type` VARCHAR(24) DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `UK_efwb3ex96plme92eseluwks26` (`filename`)
) ENGINE = InnoDB
  DEFAULT CHARSET = latin1;


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
