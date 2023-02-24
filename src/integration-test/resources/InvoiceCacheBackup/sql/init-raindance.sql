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