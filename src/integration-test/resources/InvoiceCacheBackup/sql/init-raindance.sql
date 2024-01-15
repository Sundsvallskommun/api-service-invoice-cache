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
