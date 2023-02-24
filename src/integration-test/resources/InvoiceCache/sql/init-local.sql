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
    `invoice_name` VARCHAR(255) DEFAULT NULL,
    `invoice_type` VARCHAR(24) DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `UK_efwb3ex96plme92eseluwks26` (`filename`)
) ENGINE = InnoDB
  DEFAULT CHARSET = latin1;
