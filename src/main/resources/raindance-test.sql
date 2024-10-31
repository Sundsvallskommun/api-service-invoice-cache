SELECT
    kf.NR,
    dk.KUNDID_TEXT,
    dk.KUNDID,
    dk.ORGNR,
    kf.KUNDRTYP,
    CASE
        WHEN kf.KUNDRTYP = 'KA' THEN kf.BELOPP_SEK + it.BEASUM1
        ELSE kf.BELOPP_SEK
    END AS BELOPP_SEK,
    kf.BETALT_SEK,
    CASE -- BETPAMDATUM defaults to '1799-12-31 00:00:00.000' in raindance, set it to null if that's the case
        WHEN kf.BETPAMDATUM < '1800-01-01 00:00:00.000' THEN NULL
        ELSE kf.BETPAMDATUM
    END AS BETPAMDATUM,
    kf.MOMS_VAL,
    kf.FAKTURADATUM,
    kf.FORFALLODATUM,
    CASE -- utskrdatum defaults to '1799-12-31 00:00:00.000' in raindance, set it to null if that's the case
        WHEN kf.UTSKRDATUM < '1800-01-01 00:00:00.000' THEN NULL
        ELSE kf.UTSKRDATUM
    END AS UTSKRDATUM,
    CASE -- When customer type is 'KA', means it's inkasso, don't show the OCR-number-- Will be calculated in the service
        WHEN kf.KUNDRTYP = 'KA' THEN NULL
        ELSE kf.OCRNR
    END AS OCRNR,
    kf.VREF, -- Payment status for debtCASE
    WHEN kf.TAB_BEHÄND = 'DELB' THEN 'Delvis betald'
    WHEN kf.TAB_BEHÄND = N'ÖVER' THEN N'För mycket betalt'
    WHEN ik.BLOPNR != '' THEN N'Gått till inkasso'
    WHEN kf.FAKTSTATUS = 'HBET'
    AND NOT kf.TAB_BEHÄND = 'AVSK' THEN 'Betald'
    WHEN kf.FAKTSTATUS = 'KLAR'
    AND NOT kf.TAB_BEHÄND = 'AVSK' THEN 'Betald'
    ELSE 'Obetald'
END AS FAKTURASTATUS,
kf.KRAVNIVA,
kf.FAKTSTATUS,
kf.FAKTSTATUS2,
kf.TAB_BEHÄND,
dk.NAMN2,
dk.ADR2,
dk.ORT, -- connections for debt collectionCAST(
ik.BLOPNR AS nvarchar ) AS Z21_BLOPNR,
ik.RPNR AS Z21_RPNR,
ik.BEABEL1 AS Z21_BEABEL1,
ik.BEADAT AS Z21_BEADAT,
CAST(
    it.BLOPNR AS nvarchar
) AS Z11_BLOPNR,
it.SBNR AS Z11_SBNR,
it.BEASUM1 AS Z11_BEASUM1,
it.BEARPNR AS Z11_BEARPNR,
it.BEADAT AS Z11_BEADAT, -- Creates a filename if its not debt collectionCASE
WHEN kf.KUNDRTYP = 'KA' THEN NULL -- May need to use this Collate when running "local" queries.
-- else concat('Faktura_', kf.NR, '_to_', rtrim(ltrim(dk.KUNDID)) COLLATE Latin1_General_BIN, '.pdf')

ELSE concat(
    'Faktura_',
    kf.NR,
    '_to_',
    rtrim(
        ltrim(dk.KUNDID)
    ),
    '.pdf'
)
END AS Filnamn
FROM
Raindance_Udp.udpb4_300.RK_FAKTA_KUNDFAKT AS kf -- Fetch specific information for a customer from "dimension table"

INNER JOIN(
SELECT
    KUNDID,
    KUNDID_TEXT,
    NAMN2,
    ADR2,
    ORT,
    ORGNR
FROM
    Raindance_Udp.udpb4_300.RK_DIM_KUND -- If KUND_TAB_MOT is 860 it's a private person, we fetch everything though

WHERE
    ORGNR IS NOT NULL
    AND NOT ORGNR = ''
    AND NOT KUNDID_TEXT LIKE 'Adresskydd%'
    AND NOT ORGNR LIKE '[A-Z]%'
    AND NOT ORGNR LIKE '__00%'
    AND NOT ORGNR LIKE '____00%'
    AND NOT ORGNR LIKE '____90%'
    AND NOT ORGNR LIKE '%00'
    AND NOT ORGNR LIKE '%TF%'
    AND NOT(
        KUNDID_TEXT LIKE '% DB%'
        OR KUNDID_TEXT LIKE '%dödsbo%'
        OR KUNDID_TEXT LIKE 'DB%'
    )
    AND len(
        ltrim(
            rtrim(ORGNR)
        )
    )= 10
) AS dk ON
kf.KUNDID = dk.KUNDID -- Fetch data from transaction database with connection between debt collection and invoice

LEFT JOIN(
SELECT
    *
FROM
    Raindance.raindance.RKZBEARB21
WHERE
    BEATYP = 'BP2'
    AND FR = '300'
) AS ik ON
CAST(
kf.NR AS VARCHAR
)= SUBSTRING( CAST( ik.RPNR AS VARCHAR ), 3, 10 ) -- Fetch data from transaction database with total amount for debt collection

LEFT JOIN(
SELECT
    *
FROM
    Raindance.raindance.RKZBEARB11
WHERE
    BEATYP = 'BP2'
    AND FR = '300'
) AS it ON
CAST(
kf.NR AS VARCHAR
)= SUBSTRING( CAST( it.BEARPNR AS VARCHAR ), 3, 10 )
WHERE
kf.FORFALLODATUM >= dateadd(
MONTH,
- 1,
getdate()
)
AND NOT kf.FAKTSTATUS = 'PREL'
AND NOT kf.FAKTSTATUS = 'NY' -- Fetch void ("makulerade") invoices too
-- and not kf.FAKTSTATUS='MAK'
-- Don't get reminders

AND NOT(
kf.KUNDRTYP = 'KA'
AND kf.BELOPP_SEK = '60.00'
)
