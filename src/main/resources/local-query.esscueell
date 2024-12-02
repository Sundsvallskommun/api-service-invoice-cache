-- "Local" query modified for use with MSSQL
SELECT kf.NR,
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
       -- Anger betalningsstatus för en faktura
       case
           when kf.TAB_BEHÄND='DELB' then 'Delvis betald'
           when kf.TAB_BEHÄND='ÖVER' then 'För mycket betalt'
           when ik.BLOPNR != '' then 'Gått till inkasso'
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
       -- Kopplingar inkasso
       cast(ik.BLOPNR as nvarchar) as Z21_BLOPNR,
       ik.RPNR as Z21_RPNR,
       ik.BEABEL1 as Z21_BEABEL1,
       ik.BEADAT as Z21_BEADAT,
       cast(it.BLOPNR as nvarchar) as Z11_BLOPNR,
       it.SBNR as Z11_SBNR,
       it.BEASUM1 as Z11_BEASUM1,
       it.BEARPNR as Z11_BEARPNR,
       it.BEADAT as Z11_BEADAT,
       -- Skapar filnamn om fakturan inte är ett inkasso
       case
           when kf.KUNDRTYP='KA' then null
           else concat('Faktura_', kf.NR, '_to_', rtrim(ltrim(dk.KUNDID)), '.pdf')
       end as Filnamn
FROM
    raindance.kundfaktura as kf
        inner join (
        SELECT
            KUNDID,
            KUNDID_TEXT,
            NAMN2,
            ADR2,
            ORT,
            ORGNR
        FROM
            raindance.kund
            -- Om KUND_TAB_MOT är 860 betyder det att det är en privatperson
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

        left join (
        SELECT * FROM raindance.inkasso
        where BEATYP='BP2' and FR='300'
    ) as ik on cast(kf.NR as varchar)=substring(cast(ik.RPNR as varchar),3,10)
-- Hämtar data från transaktionsdatabas med totalt belopp för inkasso
        left join (
        SELECT * FROM raindance.inkassotransaktion
        where BEATYP='BP2' and FR='300'
    ) as it on cast(kf.NR as varchar)=substring(cast(it.BEARPNR as varchar),3,10)

where kf.FORFALLODATUM >= dateadd(month, -18, getdate())
  and not kf.FAKTSTATUS='PREL'
  and not kf.FAKTSTATUS='NY'
  and not kf.FAKTSTATUS='MAK'
-- Sorterar bort påminnelser
  and not (kf.KUNDRTYP='KA' and kf.BELOPP_SEK='60.00')

