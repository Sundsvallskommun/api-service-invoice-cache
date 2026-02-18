package se.sundsvall.invoicecache.integration.raindance.mapper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RaindanceRowMapperTest {

	@Mock
	private ResultSet resultSet;

	@InjectMocks
	private RaindanceRowMapper rowMapper;

	@Test
	void testMapRow() throws SQLException {
		when(resultSet.getInt("NR")).thenReturn(321654);
		when(resultSet.getInt("KRAVNIVA")).thenReturn(2);
		when(resultSet.getInt("Z11_BEARPNR")).thenReturn(1234567890);
		when(resultSet.getInt("Z11_BLOPNR")).thenReturn(123);
		when(resultSet.getInt("Z11_SBNR")).thenReturn(654321);
		when(resultSet.getInt("Z21_RPNR")).thenReturn(1987654321);

		when(resultSet.getString("KUNDID_TEXT")).thenReturn("customer name");
		when(resultSet.getString("KUNDID")).thenReturn("customerid");
		when(resultSet.getString("ORGNR")).thenReturn("551234654321");
		when(resultSet.getString("KUNDRTYP")).thenReturn("KA");
		when(resultSet.getString("OCRNR")).thenReturn("45673456");
		when(resultSet.getString("VREF")).thenReturn("Something");
		when(resultSet.getString("FAKTURASTATUS")).thenReturn("Betald");
		when(resultSet.getString("FAKTSTATUS2")).thenReturn("U");
		when(resultSet.getString("TAB_BEHÄND")).thenReturn("JUST");
		when(resultSet.getString("NAMN2")).thenReturn("c/o Kalle");
		when(resultSet.getString("ADR2")).thenReturn("Ankeborgsvägen 2");
		when(resultSet.getString("ORT")).thenReturn("12345 Ankeborg");
		when(resultSet.getString("Filnamn")).thenReturn("Faktura_555555_to_9988776655.pdf");
		when(resultSet.getString("FAKTSTATUS")).thenReturn("KLAR");

		when(resultSet.getBigDecimal("BELOPP_SEK")).thenReturn(BigDecimal.valueOf(999).setScale(2, RoundingMode.HALF_EVEN));
		when(resultSet.getBigDecimal("BETALT_SEK")).thenReturn(BigDecimal.valueOf(888).setScale(2, RoundingMode.HALF_EVEN));
		when(resultSet.getBigDecimal("MOMS_VAL")).thenReturn(BigDecimal.valueOf(199.8).setScale(2, RoundingMode.HALF_EVEN));
		when(resultSet.getBigDecimal("Z21_BEABEL1")).thenReturn(BigDecimal.valueOf(777).setScale(2, RoundingMode.HALF_EVEN));
		when(resultSet.getBigDecimal("Z11_BEASUM1")).thenReturn(BigDecimal.valueOf(666).setScale(2, RoundingMode.HALF_EVEN));

		when(resultSet.getTimestamp("BETPAMDATUM")).thenReturn(Timestamp.valueOf("1799-12-31 00:00:00.000"));
		when(resultSet.getTimestamp("FAKTURADATUM")).thenReturn(Timestamp.valueOf("2022-01-01 01:02:03"));
		when(resultSet.getTimestamp("FORFALLODATUM")).thenReturn(Timestamp.valueOf("2022-02-02 01:02:03"));
		when(resultSet.getTimestamp("UTSKRDATUM")).thenReturn(Timestamp.valueOf("2022-01-01 01:00:00"));
		when(resultSet.getTimestamp("Z11_BEADAT")).thenReturn(Timestamp.valueOf("2022-03-03 01:02:03"));
		when(resultSet.getTimestamp("Z21_BEADAT")).thenReturn(Timestamp.valueOf("2022-04-05 01:02:03"));

		final var dto = rowMapper.mapRow(resultSet, 1);

		assertThat(dto).isNotNull();
		assertThat(dto.getNr()).isEqualTo(321654);
		assertThat(dto.getKravniva()).isEqualTo(2);
		assertThat(dto.getZ11_bearpnr()).isEqualTo(1234567890);
		assertThat(dto.getZ11_blopnr()).isEqualTo(123);
		assertThat(dto.getZ11_sbnr()).isEqualTo(654321);
		assertThat(dto.getZ21_rpnr()).isEqualTo(1987654321);

		assertThat(dto.getKundidText()).isEqualTo("customer name");
		assertThat(dto.getKundid()).isEqualTo("customerid");
		assertThat(dto.getOrgnr()).isEqualTo("551234654321");
		assertThat(dto.getKundrtyp()).isEqualTo("KA");
		assertThat(dto.getOcrnr()).isEqualTo("45673456");
		assertThat(dto.getVREF()).isEqualTo("Something");
		assertThat(dto.getFaktstatus()).isEqualTo("Betald");
		assertThat(dto.isVoid()).isFalse();
		assertThat(dto.getTabBehand()).isEqualTo("JUST");
		assertThat(dto.getNamn2()).isEqualTo("c/o Kalle");
		assertThat(dto.getAdr2()).isEqualTo("Ankeborgsvägen 2");
		assertThat(dto.getOrt()).isEqualTo("12345 Ankeborg");
		assertThat(dto.getFilnamn()).isEqualTo("Faktura_555555_to_9988776655.pdf");

		assertThat(dto.getBeloppSek()).isEqualTo(BigDecimal.valueOf(999.00).setScale(2, RoundingMode.HALF_EVEN));
		assertThat(dto.getBetaltSek()).isEqualTo(BigDecimal.valueOf(888.00).setScale(2, RoundingMode.HALF_EVEN));
		assertThat(dto.getMomsVal()).isEqualTo(BigDecimal.valueOf(199.80).setScale(2, RoundingMode.HALF_EVEN));
		assertThat(dto.getZ21_beabel1()).isEqualTo(BigDecimal.valueOf(777.00).setScale(2, RoundingMode.HALF_EVEN));
		assertThat(dto.getZ11_beasum1()).isEqualTo(BigDecimal.valueOf(666.00).setScale(2, RoundingMode.HALF_EVEN));

		assertThat(dto.getFakturadatum()).isEqualTo(Timestamp.valueOf("2022-01-01 01:02:03"));
		assertThat(dto.getForfallodatum()).isEqualTo(Timestamp.valueOf("2022-02-02 01:02:03"));
		assertThat(dto.getZ11_beadat()).isEqualTo(Timestamp.valueOf("2022-03-03 01:02:03"));
		assertThat(dto.getZ21_beadat()).isEqualTo(Timestamp.valueOf("2022-04-05 01:02:03"));
	}

	@Test
	void testMapRowWhenInvoiceIsVoid() throws SQLException {
		// Ugly "workaround" for not having to mock every call, when we're just interested in the
		// case where resultSet.getString("FAKTSTATUS") returns "MAK"
		when(resultSet.getInt(any(String.class))).thenReturn(123);
		when(resultSet.getString(any(String.class))).thenReturn("MAK");
		when(resultSet.getBigDecimal(any(String.class))).thenReturn(BigDecimal.ONE);
		when(resultSet.getTimestamp(any(String.class))).thenReturn(new Timestamp(1L));

		var dto = rowMapper.mapRow(resultSet, 1);

		assertThat(dto).isNotNull();
		assertThat(dto.isVoid()).isTrue();
	}
}
