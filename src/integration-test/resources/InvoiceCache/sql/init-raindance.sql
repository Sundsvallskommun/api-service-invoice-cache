DROP schema if exists raindance;

CREATE schema raindance;

-- raindance.inkasso definition
CREATE TABLE raindance.inkasso (
                                   RPNR int not null,
                                   BLOPNR int not null,
                                   BEABEL1 decimal(15,2) null,
                                   BEADAT datetime not null,
                                   BEATYP varchar(6) not null,
                                   FR smallint not null
);

-- raindance.inkassotransaktion definition
CREATE TABLE raindance.inkassotransaktion (
                                              FR smallint not null,
                                              BEADAT datetime not null,
                                              BLOPNR int not null,
                                              BEASUM1 decimal(15,2) null,
                                              SBNR int null,
                                              BEARPNR int null,
                                              BEATYP varchar(6) not null
);

-- raindance.kund definition
CREATE TABLE raindance.kund (
    -- id mediumtext default null,
                                KUNDID_TEXT varchar(40) null,
                                KUNDID varchar(16) not null,
                                ORGNR varchar(40) null,
                                NAMN2 varchar(40) null,
                                ADR2 varchar(40) null,
                                ORT varchar(40) null
);

-- raindance.kundfaktura definition
CREATE TABLE raindance.kundfaktura (
    -- id mediumtext default null,
                                       NR numeric(15, 0) not null,
                                       KUNDID varchar(16) null,
                                       KUNDRTYP varchar(2) not null,
                                       BELOPP_SEK numeric(15,2) null,
                                       BETALT_SEK numeric(15,2) null,
                                       MOMS_VAL numeric(15,2) null,
                                       FAKTURADATUM datetime null,
                                       FORFALLODATUM datetime null,
                                       OCRNR varchar(40) null,
                                       VREF varchar(40) null,
                                       TAB_BEHÄND varchar(4) null,
                                       FAKTSTATUS varchar(4) null,
                                       KRAVNIVA numeric(1,0) null,
                                       BETPAMDATUM datetime null,
                                       UTSKRDATUM datetime null,
                                       FAKTSTATUS2 varchar(4) null

);

-- Insert data
INSERT INTO raindance.inkasso (RPNR,BLOPNR,BEABEL1,BEADAT,BEATYP,FR) VALUES
    (1553626810, 165, 430.00, '2013-02-01 00:00:00.0', 'BP2', 300);

INSERT INTO raindance.inkassotransaktion (FR,BEADAT,BLOPNR,BEASUM1,SBNR,BEARPNR,BEATYP) VALUES
    (300, '2022-08-18 00:00:00.0' ,156, 240.00, 65245, 1553626810, 'BP2');

INSERT INTO raindance.kund (KUNDID_TEXT,KUNDID,ORGNR,NAMN2,ADR2,ORT) VALUES
    ('Janne Jannesson', '9512245030', '5591628135', 'c/o Kalle', N'Vägen 5 A LGH 1111',          '123 45 SUNDSVALL'),
    ('Kalle Kallesson', '9512245031', '5591628136', '',          N'Ankeborgsvägen 5 B LGH 1111', '987 62 ANKEBORG'),
    ('Jalle Jallesson', '9512245032', '5591628137', NULL,        N'Ankeborgsv 1A',               '987 52 ANKEBORG'),
    ('Janne Jannesson', '9512245030', '5591628135', 'c/o Kalle', N'Vägen 5 A LGH 1111',          '123 45 SUNDSVALL');

INSERT INTO raindance.kundfaktura (NR, KUNDID, KUNDRTYP, BELOPP_SEK, BETALT_SEK, MOMS_VAL, FAKTURADATUM, FORFALLODATUM, OCRNR, VREF, TAB_BEHÄND, FAKTSTATUS, KRAVNIVA, BETPAMDATUM, UTSKRDATUM, FAKTSTATUS2) VALUES
    ('504013',   '9512245030', 'AV',   250.00,     0.00,   '0.00', dateadd(month, -12, getdate()), dateadd(month, -10, getdate()), '45673456', 'Something Utbildning', '',     'DEF',  2, dateadd(month, -9, getdate()), dateadd(month, -12, getdate()), 'U'),
    ('53626897', '9512245031', 'BO',  1500.00, -1500.00,   '0.00', dateadd(month, -12, getdate()), dateadd(month, -10, getdate()), '34563457', 'Something Else',       'JUST', 'KLAR', 0, '1799-12-31 00:00:00.000',          dateadd(month, -12, getdate()), 'U'),
    ('53626898', '9512245031', 'BO',  1500.00,  -740.00,   '0.00', dateadd(month, -12, getdate()), dateadd(month, -10, getdate()), '34563458', 'Something Else',       'DELB', 'DEF',  0, '1799-12-31 00:00:00.000',          dateadd(month, -12, getdate()), 'U'),
    ('53626899', '9512245031', 'BO',  1500.00, -1600.00,   '0.00', dateadd(month, -12, getdate()), dateadd(month, -10, getdate()), '34563459', '',                     N'ÖVER', 'DEF',  0, '1799-12-31 00:00:00.000',          dateadd(month, -12, getdate()), 'U'),
    ('53626800', '9512245031', 'BO',  1500.00,     0.00, '300.00', dateadd(month, -12, getdate()), dateadd(month, -10, getdate()), '34563460', 'Something Else',       '',     'DEF',  0, '1799-12-31 00:00:00.000',          dateadd(month, -12, getdate()), 'U'),
    ('53626801', '9512245031', 'BO',  1500.00,     0.00, '300.00', dateadd(month, -12, getdate()), dateadd(month, -10, getdate()), '34563461', 'Something Else',       '',     'DEF',  0, '1799-12-31 00:00:00.000',          dateadd(month, -12, getdate()), 'U'),
    ('53626802', '9512245031', 'BO', -1500.00,  1500.00, '300.00', dateadd(month, -11, getdate()), dateadd(month, -10, getdate()), '34563462', 'Something Else',       '',     'KLAR', 0, '1799-12-31 00:00:00.000',          dateadd(month, -11, getdate()), 'U'),
    ('53626803', '9512245031', 'BO', -1500.00,     0.00, '300.00', dateadd(month, -11, getdate()), dateadd(month, -10, getdate()), '34563463', 'Something Else',       '',     '',     0, '1799-12-31 00:00:00.000',          dateadd(month, -11, getdate()), 'U'),
    ('53626804', '9512245031', 'BO',  1500.00,     0.00, '300.00', dateadd(month, -11, getdate()), dateadd(month, -10, getdate()), '34563464', '',                     '',     'DEF',  0, '1799-12-31 00:00:00.000',          dateadd(month, -11, getdate()), 'U'),
    ('53626805', '9512245031', 'BO',  1500.00,     0.00,   '0.00', dateadd(month, -10, getdate()), dateadd(month, -10, getdate()), '34563465', 'Something Else',       '',     'DEF',  0, '1799-12-31 00:00:00.000',          dateadd(month, -10, getdate()), 'U'),
    ('53626806', '9512245031', 'BO',  1500.00,     0.00,   '0.00', dateadd(month, -10, getdate()), dateadd(month, -10, getdate()), '34563466', 'Something Else',       '',     'DEF',  0, '1799-12-31 00:00:00.000',          dateadd(month, -10, getdate()), 'U'),
    ('53626807', '9512245031', 'BO',  1500.00,     0.00,   '0.00', dateadd(month, -10, getdate()), dateadd(month, -10, getdate()), '34563467', 'Something Else',       '',     'DEF',  0, '1799-12-31 00:00:00.000',          dateadd(month, -10, getdate()), 'U'),
    ('53626808', '9512245031', 'BO',  1500.00,     0.00,   '0.00', dateadd(month, -10, getdate()), dateadd(month, -10, getdate()), '34563468', 'Something Else',       '',     'DEF',  0, '1799-12-31 00:00:00.000',          dateadd(month, -10, getdate()), 'U'),
    ('53626809', '9512245031', 'BO',  1500.00,     0.00,   '0.00', dateadd(month, -9, getdate()),  dateadd(month, -10, getdate()), '34563469', 'Something Else',       '',     'DEF',  0, '1799-12-31 00:00:00.000',          dateadd(month, -9, getdate()),  'U'),
    ('53626810', '9512245032', 'KA',  1500.00,     0.00,   '0.00', dateadd(month, -12, getdate()), dateadd(month, -11, getdate()), '',         'Something Else',       '',     'DEF',  0, '1799-12-31 00:00:00.000',          dateadd(month, -12, getdate()), 'U');
