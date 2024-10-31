package se.sundsvall.invoicecache.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import se.sundsvall.invoicecache.TestObjectFactory;
import se.sundsvall.invoicecache.integration.db.entity.InvoiceEntity;
import se.sundsvall.invoicecache.integration.raindance.RaindanceQueryResultDto;

@ExtendWith(SoftAssertionsExtension.class)
class RaindanceToInvoiceMapperTest {

	private static final String CORRECT_ZIP_AND_CITY = " 856 52 SUNDSVALL ";

	private static final String FAULTY_ZIP_AND_CITY = " SOMEWHERE 12345, BANGKOK THAILAND ";

	private static final String SHORT_ZIP_NO_CITY = "856 52";

	private final RaindanceToInvoiceMapper mapper = new RaindanceToInvoiceMapper();

	@Test
	void testMapRaindanceDtoToInvoiceEntity(final SoftAssertions softly) {
		final RaindanceQueryResultDto dto = TestObjectFactory.generateRaindanceQueryResultDto();
		final InvoiceEntity entity = mapper.mapRaindanceDtoToInvoice(dto);

		softly.assertThat(entity.getCity()).isEqualTo("ANKEBORG");
		softly.assertThat(entity.getClaimLevel()).isEqualTo(1);
		softly.assertThat(entity.getCustomerId()).isEqualTo("8302311234");
		softly.assertThat(entity.getCustomerName()).isEqualTo("Kalle Anka");
		softly.assertThat(entity.getCustomerName2()).isEqualTo("c/o Knattarna");
		softly.assertThat(entity.getCustomerType()).isEqualTo("KA");
		softly.assertThat(entity.getFileName()).isEqualTo("Filnamn.pdf");
		softly.assertThat(entity.getInvoiceAmount()).isEqualTo(BigDecimal.valueOf(100).setScale(2, RoundingMode.HALF_EVEN));
		softly.assertThat(entity.getInvoiceDate()).isEqualTo(LocalDate.of(2021, 1, 1));
		softly.assertThat(entity.getInvoiceDueDate()).isEqualTo(LocalDate.of(2022, 2, 2));
		softly.assertThat(entity.getInvoiceNumber()).isEqualTo("12345678");
		softly.assertThat(entity.getInvoiceReference()).isEqualTo("Oppfinnarjocke");
		softly.assertThat(entity.getInvoiceStatus()).isEqualTo("Betald");
		softly.assertThat(entity.getOcrNumber()).isEqualTo("5566778899");
		softly.assertThat(entity.getOrganizationNumber()).isEqualTo("5599113214");
		softly.assertThat(entity.getPaidAmount()).isEqualTo(BigDecimal.valueOf(50).setScale(2, RoundingMode.HALF_EVEN));
		softly.assertThat(entity.getPaymentStatus()).isEqualTo("KVIT");
		softly.assertThat(entity.getStreet()).isEqualTo("Ankeborgsvägen 2");
		softly.assertThat(entity.getZip()).isEqualTo("123 45");
	}

	@Test
	void testMapRaindanceDtoToInvoiceEntityForVoidInvoice(final SoftAssertions softly) {

		final RaindanceQueryResultDto dto = TestObjectFactory.generateRaindanceQueryResultDto();
		dto.setVoid(true);

		final InvoiceEntity entity = mapper.mapRaindanceDtoToInvoice(dto);

		softly.assertThat(entity.getCity()).isEqualTo("ANKEBORG");
		softly.assertThat(entity.getClaimLevel()).isEqualTo(1);
		softly.assertThat(entity.getCustomerId()).isEqualTo("8302311234");
		softly.assertThat(entity.getCustomerName()).isEqualTo("Kalle Anka");
		softly.assertThat(entity.getCustomerName2()).isEqualTo("c/o Knattarna");
		softly.assertThat(entity.getCustomerType()).isEqualTo("KA");
		softly.assertThat(entity.getFileName()).isEqualTo("Filnamn.pdf");
		softly.assertThat(entity.getInvoiceAmount()).isEqualTo(BigDecimal.valueOf(100).setScale(2, RoundingMode.HALF_EVEN));
		softly.assertThat(entity.getInvoiceDate()).isEqualTo(LocalDate.of(2021, 1, 1));
		softly.assertThat(entity.getInvoiceDueDate()).isEqualTo(LocalDate.of(2022, 2, 2));
		softly.assertThat(entity.getInvoiceNumber()).isEqualTo("12345678");
		softly.assertThat(entity.getInvoiceReference()).isEqualTo("Oppfinnarjocke");
		softly.assertThat(entity.getInvoiceStatus()).isEqualTo("Makulerad");
		softly.assertThat(entity.getOcrNumber()).isEqualTo("5566778899");
		softly.assertThat(entity.getOrganizationNumber()).isEqualTo("5599113214");
		softly.assertThat(entity.getPaidAmount()).isEqualTo(BigDecimal.valueOf(50).setScale(2, RoundingMode.HALF_EVEN));
		softly.assertThat(entity.getPaymentStatus()).isEqualTo("KVIT");
		softly.assertThat(entity.getStreet()).isEqualTo("Ankeborgsvägen 2");
		softly.assertThat(entity.getZip()).isEqualTo("123 45");
	}

	@Test
	void testGenerateOcr() {
		final RaindanceQueryResultDto dto = TestObjectFactory.generateRaindanceQueryResultDto();
		dto.setOcrnr(null);

		assertEquals("2300122202020001290", mapper.getOrCalculateOcr(dto));
	}

	@Test
	void testOcrShouldNotBeGenerated() {
		final RaindanceQueryResultDto dto = TestObjectFactory.generateRaindanceQueryResultDto();
		assertEquals(mapper.getOrCalculateOcr(dto), dto.getOcrnr());
	}

	@Test
	void testOnlyKundrtypDoesntMatch() {
		final RaindanceQueryResultDto dto = TestObjectFactory.generateRaindanceQueryResultDto();
		dto.setKundrtyp("AB");
		assertEquals(dto.getOcrnr(), mapper.getOrCalculateOcr(dto));
	}

	@Test
	void testBlopnrIsZero() {
		final RaindanceQueryResultDto dto = TestObjectFactory.generateRaindanceQueryResultDto();
		dto.setZ11_blopnr(0);
		assertEquals(dto.getOcrnr(), mapper.getOrCalculateOcr(dto));
	}

	@Test
	void testBeadatIsMissing() {
		final RaindanceQueryResultDto dto = TestObjectFactory.generateRaindanceQueryResultDto();
		dto.setZ11_beadat(null);
		assertEquals(dto.getOcrnr(), mapper.getOrCalculateOcr(dto));
	}

	@Test
	void testCorrectOrtShouldExtractZipAndCity() {
		final RaindanceQueryResultDto dto = TestObjectFactory.generateRaindanceQueryResultDto();

		dto.setOrt(CORRECT_ZIP_AND_CITY);
		final InvoiceEntity invoice = new InvoiceEntity();
		mapper.setZipAndCityIfCorrectFormat(dto, invoice);
		assertEquals("856 52", invoice.getZip());
		assertEquals("SUNDSVALL", invoice.getCity());
	}

	@Test
	void testFaultyOrtShouldNotExtractZipAndCity() {
		final RaindanceQueryResultDto dto = TestObjectFactory.generateRaindanceQueryResultDto();

		dto.setOrt(FAULTY_ZIP_AND_CITY);
		final InvoiceEntity invoice = new InvoiceEntity();
		mapper.setZipAndCityIfCorrectFormat(dto, invoice);
		assertNull(invoice.getZip());
		assertEquals(FAULTY_ZIP_AND_CITY.trim(), invoice.getCity());
	}

	@Test
	void testShortOrtShouldNotExtractZipAndCity() {
		final RaindanceQueryResultDto dto = TestObjectFactory.generateRaindanceQueryResultDto();

		dto.setOrt(SHORT_ZIP_NO_CITY);
		final InvoiceEntity invoice = new InvoiceEntity();
		mapper.setZipAndCityIfCorrectFormat(dto, invoice);
		assertNull(invoice.getZip());
		assertEquals(SHORT_ZIP_NO_CITY.trim(), invoice.getCity());
	}

	@ParameterizedTest
	@ValueSource(strings = {
		"", "-02 50 OSLO", "NO-0250 Oslo", "SE-831 90 ÖSTERSUND", "SUNDSVALL", "SOMEWHERE 12345, BANGKOK THAILAND"
	})
	void testInvalidOrtShouldReturnOriginalValue(final String testString) {
		final RaindanceQueryResultDto dto = TestObjectFactory.generateRaindanceQueryResultDto();
		dto.setOrt(testString);
		final InvoiceEntity invoice = new InvoiceEntity();
		mapper.setZipAndCityIfCorrectFormat(dto, invoice);

		assertEquals(testString, invoice.getCity());
	}

}
