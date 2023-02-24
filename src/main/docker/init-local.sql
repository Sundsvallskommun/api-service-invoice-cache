-- `ms-invoicecache`.backupinvoice definition
DROP DATABASE `ms-invoicecache`;

CREATE DATABASE `ms-invoicecache`;

CREATE TABLE `ms-invoicecache`.`backupinvoice` (
                                 `id` bigint(20) NOT NULL AUTO_INCREMENT,
                                 `city` varchar(255) DEFAULT NULL,
                                 `claim_level` int(11) DEFAULT NULL,
                                 `customer_id` varchar(255) DEFAULT NULL,
                                 `customer_name` varchar(255) DEFAULT NULL,
                                 `customer_name2` varchar(255) DEFAULT NULL,
                                 `customer_type` varchar(255) DEFAULT NULL,
                                 `file_name` varchar(255) DEFAULT NULL,
                                 `invoice_amount` decimal(19,2) DEFAULT NULL,
                                 `invoice_date` date DEFAULT NULL,
                                 `invoice_due_date` date DEFAULT NULL,
                                 `invoice_number` varchar(255) DEFAULT NULL,
                                 `invoice_reference` varchar(255) DEFAULT NULL,
                                 `invoice_status` varchar(255) DEFAULT NULL,
                                 `name` varchar(255) DEFAULT NULL,
                                 `ocr_number` varchar(255) DEFAULT NULL,
                                 `organization_number` varchar(255) DEFAULT NULL,
                                 `paid_amount` decimal(19,2) DEFAULT NULL,
                                 `payment_status` varchar(255) DEFAULT NULL,
                                 `street` varchar(255) DEFAULT NULL,
                                 `vat` decimal(19,2) DEFAULT NULL,
                                 `zip` varchar(255) DEFAULT NULL,
                                 PRIMARY KEY (`id`),
                                 KEY `inv_index` (`invoice_number`),
                                 KEY `org_index` (`organization_number`),
                                 KEY `ocr_index` (`ocr_number`),
                                 KEY `cus_index` (`customer_id`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4;


-- `ms-invoicecache`.invoice definition

CREATE TABLE `ms-invoicecache`.`invoice` (
                           `id` bigint(20) NOT NULL AUTO_INCREMENT,
                           `city` varchar(255) DEFAULT NULL,
                           `claim_level` int(11) DEFAULT NULL,
                           `customer_id` varchar(255) DEFAULT NULL,
                           `customer_name` varchar(255) DEFAULT NULL,
                           `customer_name2` varchar(255) DEFAULT NULL,
                           `customer_type` varchar(255) DEFAULT NULL,
                           `file_name` varchar(255) DEFAULT NULL,
                           `invoice_amount` decimal(19,2) DEFAULT NULL,
                           `invoice_date` date DEFAULT NULL,
                           `invoice_due_date` date DEFAULT NULL,
                           `invoice_number` varchar(255) DEFAULT NULL,
                           `invoice_reference` varchar(255) DEFAULT NULL,
                           `invoice_status` varchar(255) DEFAULT NULL,
                           `name` varchar(255) DEFAULT NULL,
                           `ocr_number` varchar(255) DEFAULT NULL,
                           `organization_number` varchar(255) DEFAULT NULL,
                           `paid_amount` decimal(19,2) DEFAULT NULL,
                           `payment_status` varchar(255) DEFAULT NULL,
                           `street` varchar(255) DEFAULT NULL,
                           `vat` decimal(19,2) DEFAULT NULL,
                           `zip` varchar(255) DEFAULT NULL,
                           PRIMARY KEY (`id`),
                           KEY `bak_inv_index` (`invoice_number`),
                           KEY `bak_org_index` (`organization_number`),
                           KEY `bak_ocr_index` (`ocr_number`),
                           KEY `bak_cus_index` (`customer_id`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4;
