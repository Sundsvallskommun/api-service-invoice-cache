package se.sundsvall.invoicecache.integration.raindance.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.RowMapper;
import se.sundsvall.invoicecache.integration.raindance.RaindanceQueryResultDto;

public class RaindanceRowMapper implements RowMapper<RaindanceQueryResultDto> {

	@Override
	public RaindanceQueryResultDto mapRow(final ResultSet resultSet, final int rowNum) throws SQLException {
		return RaindanceQueryResultDto.builder()
			.withNr(resultSet.getInt("NR"))
			.withKundidText(resultSet.getString("KUNDID_TEXT"))
			.withKundid(resultSet.getString("KUNDID"))
			.withOrgnr(resultSet.getString("ORGNR"))
			.withKundrtyp(resultSet.getString("KUNDRTYP"))
			.withBeloppSek(resultSet.getBigDecimal("BELOPP_SEK"))
			.withBetaltSek(resultSet.getBigDecimal("BETALT_SEK"))
			.withMomsVal(resultSet.getBigDecimal("MOMS_VAL"))
			.withFakturadatum(resultSet.getTimestamp("FAKTURADATUM"))
			.withForfallodatum(resultSet.getTimestamp("FORFALLODATUM"))
			.withBetpamdatum(resultSet.getTimestamp("BETPAMDATUM"))
			.withUtskrdatum(resultSet.getTimestamp("UTSKRDATUM"))
			.withOcrnr(resultSet.getString("OCRNR"))
			.withVREF(resultSet.getString("VREF"))
			.withKravniva(resultSet.getInt("KRAVNIVA"))
			.withFaktstatus(resultSet.getString("FAKTURASTATUS"))
			.withFaktstatus2(resultSet.getString("FAKTSTATUS2"))
			.withTabBehand(resultSet.getString("TAB_BEHÄND"))
			.withNamn2(resultSet.getString("NAMN2"))
			.withAdr2(resultSet.getString("ADR2"))
			.withOrt(resultSet.getString("ORT"))
			.withFilnamn(resultSet.getString("Filnamn"))
			.withZ11_beadat(resultSet.getTimestamp("Z11_BEADAT"))
			.withZ11_bearpnr(resultSet.getInt("Z11_BEARPNR"))
			.withZ11_blopnr(resultSet.getInt("Z11_BLOPNR"))
			.withZ11_sbnr(resultSet.getInt("Z11_SBNR"))
			.withZ21_beabel1(resultSet.getBigDecimal("Z21_BEABEL1"))
			.withZ21_beadat(resultSet.getTimestamp("Z21_BEADAT"))
			.withZ11_beasum1(resultSet.getBigDecimal("Z11_BEASUM1"))
			.withZ21_rpnr(resultSet.getInt("Z21_RPNR"))
			.withIsVoid("MAK".equalsIgnoreCase(resultSet.getString("FAKTSTATUS")))
			.build();
	}
}
