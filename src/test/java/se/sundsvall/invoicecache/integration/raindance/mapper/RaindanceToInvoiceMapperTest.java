package se.sundsvall.invoicecache.integration.raindance.mapper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import se.sundsvall.invoicecache.TestObjectFactory;
import se.sundsvall.invoicecache.integration.db.entity.InvoiceEntity;

import static org.assertj.core.api.Assertions.assertThat;

class RaindanceToInvoiceMapperTest {

	private static final String CORRECT_ZIP_AND_CITY = " 856 52 SUNDSVALL ";

	private static final String FAULTY_ZIP_AND_CITY = " SOMEWHERE 12345, BANGKOK THAILAND ";

	private static final String SHORT_ZIP_NO_CITY = "856 52";

	private final RaindanceToInvoiceMapper mapper = new RaindanceToInvoiceMapper();

	@Test
	void testMapRaindanceDtoToInvoiceEntity() {
		final var dto = TestObjectFactory.generateRaindanceQueryResultDto();
		final var entity = mapper.mapRaindanceDtoToInvoice(dto);

		assertThat(entity.getCity()).isEqualTo("ANKEBORG");
		assertThat(entity.getClaimLevel()).isEqualTo(1);
		assertThat(entity.getCustomerId()).isEqualTo("8302311234");
		assertThat(entity.getCustomerName()).isEqualTo("Kalle Anka");
		assertThat(entity.getCustomerName2()).isEqualTo("c/o Knattarna");
		assertThat(entity.getCustomerType()).isEqualTo("KA");
		assertThat(entity.getFileName()).isEqualTo("Filnamn.pdf");
		assertThat(entity.getInvoiceAmount()).isEqualTo(BigDecimal.valueOf(100).setScale(2, RoundingMode.HALF_EVEN));
		assertThat(entity.getInvoiceDate()).isEqualTo(LocalDate.of(2021, 1, 1));
		assertThat(entity.getInvoiceDueDate()).isEqualTo(LocalDate.of(2022, 2, 2));
		assertThat(entity.getInvoiceNumber()).isEqualTo("12345678");
		assertThat(entity.getInvoiceReference()).isEqualTo("Oppfinnarjocke");
		assertThat(entity.getInvoiceStatus()).isEqualTo("Betald");
		assertThat(entity.getOcrNumber()).isEqualTo("5566778899");
		assertThat(entity.getOrganizationNumber()).isEqualTo("5599113214");
		assertThat(entity.getPaidAmount()).isEqualTo(BigDecimal.valueOf(50).setScale(2, RoundingMode.HALF_EVEN));
		assertThat(entity.getPaymentStatus()).isEqualTo("KVIT");
		assertThat(entity.getStreet()).isEqualTo("Ankeborgsvägen 2");
		assertThat(entity.getZip()).isEqualTo("123 45");
	}

	@Test
	void testMapRaindanceDtoToInvoiceEntityForVoidInvoice() {

		final var dto = TestObjectFactory.generateRaindanceQueryResultDto();
		dto.setVoid(true);

		final var entity = mapper.mapRaindanceDtoToInvoice(dto);

		assertThat(entity.getCity()).isEqualTo("ANKEBORG");
		assertThat(entity.getClaimLevel()).isEqualTo(1);
		assertThat(entity.getCustomerId()).isEqualTo("8302311234");
		assertThat(entity.getCustomerName()).isEqualTo("Kalle Anka");
		assertThat(entity.getCustomerName2()).isEqualTo("c/o Knattarna");
		assertThat(entity.getCustomerType()).isEqualTo("KA");
		assertThat(entity.getFileName()).isEqualTo("Filnamn.pdf");
		assertThat(entity.getInvoiceAmount()).isEqualTo(BigDecimal.valueOf(100).setScale(2, RoundingMode.HALF_EVEN));
		assertThat(entity.getInvoiceDate()).isEqualTo(LocalDate.of(2021, 1, 1));
		assertThat(entity.getInvoiceDueDate()).isEqualTo(LocalDate.of(2022, 2, 2));
		assertThat(entity.getInvoiceNumber()).isEqualTo("12345678");
		assertThat(entity.getInvoiceReference()).isEqualTo("Oppfinnarjocke");
		assertThat(entity.getInvoiceStatus()).isEqualTo("Makulerad");
		assertThat(entity.getOcrNumber()).isEqualTo("5566778899");
		assertThat(entity.getOrganizationNumber()).isEqualTo("5599113214");
		assertThat(entity.getPaidAmount()).isEqualTo(BigDecimal.valueOf(50).setScale(2, RoundingMode.HALF_EVEN));
		assertThat(entity.getPaymentStatus()).isEqualTo("KVIT");
		assertThat(entity.getStreet()).isEqualTo("Ankeborgsvägen 2");
		assertThat(entity.getZip()).isEqualTo("123 45");
	}

	@Test
	void testGenerateOcr() {
		final var dto = TestObjectFactory.generateRaindanceQueryResultDto();
		dto.setOcrnr(null);

		assertThat(mapper.getOrCalculateOcr(dto)).isEqualTo("2300122202020001290");
	}

	@Test
	void testOcrShouldNotBeGenerated() {
		final var dto = TestObjectFactory.generateRaindanceQueryResultDto();
		assertThat(mapper.getOrCalculateOcr(dto)).isEqualTo(dto.getOcrnr());
	}

	@Test
	void testOnlyKundrtypDoesntMatch() {
		final var dto = TestObjectFactory.generateRaindanceQueryResultDto();
		dto.setKundrtyp("AB");
		assertThat(dto.getOcrnr()).isEqualTo(mapper.getOrCalculateOcr(dto));
	}

	@Test
	void testBlopnrIsZero() {
		final var dto = TestObjectFactory.generateRaindanceQueryResultDto();
		dto.setZ11_blopnr(0);
		assertThat(dto.getOcrnr()).isEqualTo(mapper.getOrCalculateOcr(dto));
	}

	@Test
	void testBeadatIsMissing() {
		final var dto = TestObjectFactory.generateRaindanceQueryResultDto();
		dto.setZ11_beadat(null);
		assertThat(dto.getOcrnr()).isEqualTo(mapper.getOrCalculateOcr(dto));
	}

	@Test
	void testCorrectOrtShouldExtractZipAndCity() {
		final var dto = TestObjectFactory.generateRaindanceQueryResultDto();
		dto.setOrt(CORRECT_ZIP_AND_CITY);

		final var invoice = new InvoiceEntity();
		mapper.setZipAndCityIfCorrectFormat(dto, invoice);
		assertThat(invoice.getZip()).isEqualTo("856 52");
		assertThat(invoice.getCity()).isEqualTo("SUNDSVALL");
	}

	@Test
	void testFaultyOrtShouldNotExtractZipAndCity() {
		final var dto = TestObjectFactory.generateRaindanceQueryResultDto();
		dto.setOrt(FAULTY_ZIP_AND_CITY);

		final var invoice = new InvoiceEntity();
		mapper.setZipAndCityIfCorrectFormat(dto, invoice);
		assertThat(invoice.getZip()).isNull();
		assertThat(FAULTY_ZIP_AND_CITY.trim()).isEqualTo(invoice.getCity());
	}

	@Test
	void testShortOrtShouldNotExtractZipAndCity() {
		final var dto = TestObjectFactory.generateRaindanceQueryResultDto();
		dto.setOrt(SHORT_ZIP_NO_CITY);

		final var invoice = new InvoiceEntity();
		mapper.setZipAndCityIfCorrectFormat(dto, invoice);
		assertThat(invoice.getZip()).isNull();
		assertThat(SHORT_ZIP_NO_CITY.trim()).isEqualTo(invoice.getCity());
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"", "-02 50 OSLO", "NO-0250 Oslo", "SE-831 90 ÖSTERSUND", "SUNDSVALL", "SOMEWHERE 12345, BANGKOK THAILAND"
	})
	void testInvalidOrtShouldReturnOriginalValue(final String testString) {
		final var dto = TestObjectFactory.generateRaindanceQueryResultDto();
		dto.setOrt(testString);
		final var invoice = new InvoiceEntity();
		mapper.setZipAndCityIfCorrectFormat(dto, invoice);

		assertThat(testString).isEqualTo(invoice.getCity());
	}

}
