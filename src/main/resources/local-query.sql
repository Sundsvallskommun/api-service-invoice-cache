-- "Local" query modified for use with MSSQL
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
    kf.VREF,
    CASE -- Anger betalningsstatus för en faktura
        WHEN kf.TAB_BEHÄND = 'DELB' THEN 'Delvis betald'
        WHEN kf.TAB_BEHÄND = 'ÖVER' THEN 'För mycket betalt'
        WHEN ik.BLOPNR != '' THEN 'Gått till inkasso'
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
    dk.ORT,
    CAST( -- Kopplingar inkasso
        ik.BLOPNR AS nvarchar
    ) AS Z21_BLOPNR,
    ik.RPNR AS Z21_RPNR,
    ik.BEABEL1 AS Z21_BEABEL1,
    ik.BEADAT AS Z21_BEADAT,
    CAST(
        it.BLOPNR AS nvarchar
    ) AS Z11_BLOPNR,
    it.SBNR AS Z11_SBNR,
    it.BEASUM1 AS Z11_BEASUM1,
    it.BEARPNR AS Z11_BEARPNR,
    it.BEADAT AS Z11_BEADAT,
    CASE -- Skapar filnamn om fakturan inte är ett inkassoärende
        WHEN kf.KUNDRTYP = 'KA' THEN NULL
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
    raindance.kundfaktura AS kf
INNER JOIN(
        SELECT
            KUNDID,
            KUNDID_TEXT,
            NAMN2,
            ADR2,
            ORT,
            ORGNR
        FROM
            raindance.kund -- Om KUND_TAB_MOT är 860 betyder det att det är en privatperson

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
    kf.KUNDID = dk.KUNDID
LEFT JOIN(
        SELECT
            *
        FROM
            raindance.inkasso
        WHERE
            BEATYP = 'BP2'
            AND FR = '300'
    ) AS ik ON
    CAST(
        kf.NR AS VARCHAR
    )= SUBSTRING( CAST( ik.RPNR AS VARCHAR ), 3, 10 ) -- Hämtar data från transaktionsdatabas med totalt belopp för inkasso

LEFT JOIN(
        SELECT
            *
        FROM
            raindance.inkassotransaktion
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
        - 18,
        getdate()
    )
    AND NOT kf.FAKTSTATUS = 'PREL'
    AND NOT kf.FAKTSTATUS = 'NY'
    AND NOT kf.FAKTSTATUS = 'MAK'
    AND NOT( -- Sorterar bort påminnelser
        kf.KUNDRTYP = 'KA'
        AND kf.BELOPP_SEK = '60.00'
    )
