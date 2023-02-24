DROP DATABASE `raindance`;

CREATE DATABASE `raindance`;

-- raindance.inkasso definition
CREATE TABLE `raindance`.`inkasso` (
                                       `RPNR` bigint(20) DEFAULT NULL,
                                       `BLOPNR` int(11) DEFAULT NULL,
                                       `BEABEL1` decimal(15,2) DEFAULT NULL,
                                       `BEADAT` datetime DEFAULT NULL,
                                       `BEATYP` varchar(6) DEFAULT NULL,
                                       `FR` smallint(6) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- raindance.inkassotransaktion definition
CREATE TABLE `raindance`.`inkassotransaktion` (
                                                  `FR` smallint(6) NOT NULL,
                                                  `BEADAT` datetime DEFAULT NULL,
                                                  `BLOPNR` int(11) DEFAULT NULL,
                                                  `BEASUM1` decimal(15,2) DEFAULT NULL,
                                                  `SBNR` bigint(20) DEFAULT NULL,
                                                  `BEARPNR` bigint(20) DEFAULT NULL,
                                                  `BEATYP` varchar(6) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- raindance.kund definition
CREATE TABLE `raindance`.`kund` (
                                    `id` mediumtext DEFAULT NULL,
                                    `KUNDID_TEXT` varchar(40) DEFAULT NULL,
                                    `KUNDID` varchar(16) DEFAULT NULL,
                                    `ORGNR` varchar(40) DEFAULT NULL,
                                    `NAMN2` varchar(40) DEFAULT NULL,
                                    `ADR2` varchar(40) DEFAULT NULL,
                                    `ORT` varchar(40) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- raindance.kundfaktura definition
CREATE TABLE `raindance`.`kundfaktura` (
                                           `id` mediumtext DEFAULT NULL,
                                           `NR` varchar(16) DEFAULT NULL,
                                           `KUNDID` varchar(16) DEFAULT NULL,
                                           `KUNDRTYP` varchar(2) DEFAULT NULL,
                                           `BELOPP_SEK` decimal(15,2) DEFAULT NULL,
                                           `BETALT_SEK` decimal(15,2) DEFAULT NULL,
                                           `MOMS_VAL` decimal(15,2) DEFAULT NULL,
                                           `FAKTURADATUM` datetime DEFAULT NULL,
                                           `FORFALLODATUM` datetime DEFAULT NULL,
                                           `OCRNR` varchar(40) DEFAULT NULL,
                                           `VREF` varchar(40) DEFAULT NULL,
                                           `TAB_BEHÄND` varchar(4) DEFAULT NULL,
                                           `FAKTSTATUS` varchar(4) DEFAULT NULL,
                                           `KRAVNIVA` decimal(1,0) DEFAULT NULL,
                                           `BETPAMDATUM` datetime DEFAULT NULL,
                                           `UTSKRDATUM` datetime DEFAULT NULL,
                                           `FAKTSTATUS2` varchar(4) DEFAULT NULL

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Insert data
INSERT INTO raindance.inkasso (RPNR,BLOPNR,BEABEL1,BEADAT,BEATYP,FR) VALUES
    (150053626810,165,430.00,'2013-02-01 00:00:00.0','BP2',300);

INSERT INTO raindance.inkassotransaktion (FR,BEADAT,BLOPNR,BEASUM1,SBNR,BEARPNR,BEATYP) VALUES
    (300,'2022-08-18 00:00:00.0',156,240.00,65245,1553626810,'BP2');

INSERT INTO raindance.kund (id,KUNDID_TEXT,KUNDID,ORGNR,NAMN2,ADR2,ORT) VALUES
    ('1','Janne Jannesson','9512245030','5591628135','c/o Kalle','Vägen 5 A LGH 1111','123 45 SUNDSVALL'),
    ('2','Kalle Kallesson','9512245031','5591628136','','Ankeborgsvägen 5 B LGH 1111','987 62 ANKEBORG'),
    ('3','Jalle Jallesson','9512245032','5591628137',NULL,'Ankeborgsv 1A','987 52 ANKEBORG'),
    ('4','Janne Jannesson','9512245030','5591628135','c/o Kalle','Vägen 5 A LGH 1111','123 45 SUNDSVALL');

INSERT INTO raindance.kundfaktura (id,NR,KUNDID,KUNDRTYP,BELOPP_SEK,BETALT_SEK,MOMS_VAL,FAKTURADATUM,FORFALLODATUM,OCRNR,VREF,TAB_BEHÄND,FAKTSTATUS,KRAVNIVA,BETPAMDATUM,UTSKRDATUM,FAKTSTATUS2) VALUES
    ('1','504013','9512245030','AV',250.00,0.00,'0.00','2022-03-14 00:00:00.0','2022-03-31 00:00:00.0','45673456','Something Utbildning','','DEF',2,'2022-04-5 00:00:00.0','2022-03-01 00:00:00.0','U'),
    ('2','53626897','9512245031','BO',1500.00,-1500.00,'0.00','2022-08-06 00:00:00.0','2022-08-31 00:00:00.0','34563457','Something Else','JUST','KLAR',0,'1799-12-31 00:00:00.000','2022-08-01 00:00:00.0','U'),
    ('3','53626898','9512245031','BO',1500.00,-740.00,'0.00','2022-08-06 00:00:00.0','2022-08-31 00:00:00.0','34563458','Something Else','DELB','DEF',0,'1799-12-31 00:00:00.000','2022-08-01 00:00:00.0','U'),
    ('4','53626899','9512245031','BO',1500.00,-1600.00,'0.00','2022-08-06 00:00:00.0','2022-08-31 00:00:00.0','34563459','','ÖVER','DEF',0,'1799-12-31 00:00:00.000','2022-08-01 00:00:00.0','U'),
    ('5','53626800','9512245031','BO',1500.00,0.00,'300.00','2022-08-06 00:00:00.0','2022-09-06 00:00:00.0','34563460','Something Else','','DEF',0,'1799-12-31 00:00:00.000','2022-08-01 00:00:00.0','U'),
    ('6','53626801','9512245031','BO',1500.00,0.00,'300.00','2022-08-07 00:00:00.0','2022-09-07 00:00:00.0','34563461','Something Else','','DEF',0,'1799-12-31 00:00:00.000','2022-08-02 00:00:00.0','U'),
    ('7','53626802','9512245031','BO',-1500.00,1500.00,'300.00','2022-08-08 00:00:00.0','2022-09-08 00:00:00.0','34563462','Something Else','','KLAR',0,'1799-12-31 00:00:00.000','2022-08-03 00:00:00.0','U'),
    ('8','53626803','9512245031','BO',-1500.00,0.00,'300.00','2022-08-09 00:00:00.0','2022-09-09 00:00:00.0','34563463','Something Else','','',0,'1799-12-31 00:00:00.000','2022-08-04 00:00:00.0','U'),
    ('9','53626804','9512245031','BO',1500.00,0.00,'300.00','2022-08-10 00:00:00.0','2022-09-10 00:00:00.0','34563464','','','DEF',0,'1799-12-31 00:00:00.000','2022-08-05 00:00:00.0','U'),
    ('10','53626805','9512245031','BO',1500.00,0.00,'0.00','2022-08-11 00:00:00.0','2022-09-11 00:00:00.0','34563465','Something Else','','DEF',0,'1799-12-31 00:00:00.000','2022-08-06 00:00:00.0','U'),
    ('11','53626806','9512245031','BO',1500.00,0.00,'0.00','2022-08-12 00:00:00.0','2022-09-12 00:00:00.0','34563466','Something Else','','DEF',0,'1799-12-31 00:00:00.000','2022-08-07 00:00:00.0','U'),
    ('12','53626807','9512245031','BO',1500.00,0.00,'0.00','2022-08-13 00:00:00.0','2022-09-13 00:00:00.0','34563467','Something Else','','DEF',0,'1799-12-31 00:00:00.000','2022-08-08 00:00:00.0','U'),
    ('13','53626808','9512245031','BO',1500.00,0.00,'0.00','2022-08-14 00:00:00.0','2022-09-14 00:00:00.0','34563468','Something Else','','DEF',0,'1799-12-31 00:00:00.000','2022-08-09 00:00:00.0','U'),
    ('14','53626809','9512245031','BO',1500.00,0.00,'0.00','2020-10-15 00:00:00.0','2020-11-15 00:00:00.0','34563469','Something Else','','DEF',0,'1799-12-31 00:00:00.000','2022-08-10 00:00:00.0','U'),
    ('15','53626810','9512245032','KA',1500.00,0.00,'0.00','2022-08-16 00:00:00.0','2022-09-16 00:00:00.0','','Something Else','','DEF',0,'1799-12-31 00:00:00.000','2022-08-11 00:00:00.0','U');
