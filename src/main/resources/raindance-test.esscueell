SELECT
    kf.NR,
    dk.KUNDID_TEXT,
    dk.KUNDID,
    dk.ORGNR,
    kf.KUNDRTYP,
    case
        when kf.KUNDRTYP='KA' then kf.BELOPP_SEK + it.BEASUM1
        else kf.BELOPP_SEK
        end as BELOPP_SEK,
    kf.BETALT_SEK,
    case -- BETPAMDATUM defaults to '1799-12-31 00:00:00.000' in raindance, set it to null if that's the case
        when kf.BETPAMDATUM < '1800-01-01 00:00:00.000' then null
        else kf.BETPAMDATUM
        end as BETPAMDATUM,
    kf.MOMS_VAL,
    kf.FAKTURADATUM,
    kf.FORFALLODATUM,
    case
        -- utskrdatum defaults to '1799-12-31 00:00:00.000' in raindance, set it to null if that's the case
        when kf.UTSKRDATUM < '1800-01-01 00:00:00.000' then null
        else kf.UTSKRDATUM
        end as UTSKRDATUM,
    case
        -- When customer type is 'KA', means it's inkasso, don't show the OCR-number
        -- Will be calculated in the service
        when kf.KUNDRTYP = 'KA' then null
        else kf.OCRNR
        end as OCRNR,
    kf.VREF,
    -- Payment status for debt
    case
        when kf.TAB_BEHÄND='DELB' then 'Delvis betald'
        when kf.TAB_BEHÄND= N'ÖVER' then N'För mycket betalt'
        when ik.BLOPNR != '' then N'Gått till inkasso'
        when kf.FAKTSTATUS='HBET' and not kf.TAB_BEHÄND='AVSK' then 'Betald'
        when kf.FAKTSTATUS='KLAR' and not kf.TAB_BEHÄND='AVSK' then 'Betald'
        else 'Obetald'
        end as FAKTURASTATUS,
    kf.KRAVNIVA,
    kf.FAKTSTATUS,
    kf.FAKTSTATUS2,
    kf.TAB_BEHÄND,
    dk.NAMN2,
    dk.ADR2,
    dk.ORT,
    -- connections for debt collection
    cast(ik.BLOPNR as nvarchar) as Z21_BLOPNR,
    ik.RPNR as Z21_RPNR,
    ik.BEABEL1 as Z21_BEABEL1,
    ik.BEADAT as Z21_BEADAT,
    cast(it.BLOPNR as nvarchar) as Z11_BLOPNR,
    it.SBNR as Z11_SBNR,
    it.BEASUM1 as Z11_BEASUM1,
    it.BEARPNR as Z11_BEARPNR,
    it.BEADAT as Z11_BEADAT,
    -- Creates a filename if its not debt collection
    case
        when kf.KUNDRTYP='KA' then null
        -- May need to use this Collate when running "local" queries.
        -- else concat('Faktura_', kf.NR, '_to_', rtrim(ltrim(dk.KUNDID)) COLLATE Latin1_General_BIN, '.pdf')
        else concat('Faktura_', kf.NR, '_to_', rtrim(ltrim(dk.KUNDID)), '.pdf')
        end as Filnamn
FROM
    Raindance_Udp.udpb4_300.RK_FAKTA_KUNDFAKT as kf
-- Fetch specific information for a customer from "dimension table"
        inner join (
        SELECT
            KUNDID,
            KUNDID_TEXT,
            NAMN2,
            ADR2,
            ORT,
            ORGNR
        FROM
            Raindance_Udp.udpb4_300.RK_DIM_KUND
-- If KUND_TAB_MOT is 860 it's a private person, we fetch everything though
        where ORGNR is not null
          and not ORGNR=''
          and not KUNDID_TEXT like 'Adresskydd%'
          and not ORGNR like '[A-Z]%'
          and not ORGNR like '__00%'
          and not ORGNR like '____00%'
          and not ORGNR like '____90%'
          and not ORGNR like '%00'
          and not ORGNR like '%TF%'
          and not (KUNDID_TEXT like '% DB%' or KUNDID_TEXT like '%dödsbo%' or KUNDID_TEXT like 'DB%')
          and len(ltrim(rtrim(ORGNR)))=10
    ) as dk on kf.KUNDID=dk.KUNDID

-- Fetch data from transaction database with connection between debt collection and invoice
        left join (
        SELECT * FROM Raindance.raindance.RKZBEARB21
        where BEATYP='BP2' and FR='300'
    ) as ik on cast(kf.NR as varchar)=substring(cast(ik.RPNR as varchar),3,10)

-- Fetch data from transaction database with total amount for debt collection
        left join (
        SELECT * FROM Raindance.raindance.RKZBEARB11
        where BEATYP='BP2' and FR='300'
    ) as it on cast(kf.NR as varchar)=substring(cast(it.BEARPNR as varchar),3,10)

where kf.FORFALLODATUM >= dateadd(month, -1, getdate())
  and not kf.FAKTSTATUS='PREL'
  and not kf.FAKTSTATUS='NY'
-- Fetch void ("makulerade") invoices too
-- and not kf.FAKTSTATUS='MAK'
-- Don't get reminders
  and not (kf.KUNDRTYP='KA' and kf.BELOPP_SEK='60.00')

