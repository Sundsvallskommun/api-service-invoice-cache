package se.sundsvall.invoicecache.integration.storage.importer;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IndexXmlParserTest {

	private final IndexXmlParser parser = new IndexXmlParser();

	@Test
	void parse_wellFormedIndex() throws Exception {
		final var xml = """
			<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
			<idataOrder orderNumber="123">
				<document source="firstInvoice.pdf">
					<invoiceNumber>INV-1</invoiceNumber>
					<archiveDate>2022-09-29T14:09:25+02:00</archiveDate>
					<customerNumber>cust-1</customerNumber>
					<customerName>Customer One</customerName>
				</document>
				<document source="secondInvoice.pdf">
					<invoiceNumber>INV-2</invoiceNumber>
					<archiveDate>2023-01-15T09:00:00+01:00</archiveDate>
					<customerNumber>cust-2</customerNumber>
					<customerName>Customer Two</customerName>
				</document>
			</idataOrder>
			""";

		final var index = parser.parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));

		assertThat(index.orderNumber()).isEqualTo("123");
		assertThat(index.documents()).hasSize(2);
		assertThat(index.documents().getFirst().source()).isEqualTo("firstInvoice.pdf");
		assertThat(index.documents().getFirst().invoiceNumber()).isEqualTo("INV-1");
		assertThat(index.documents().getFirst().archiveDate()).isEqualTo(OffsetDateTime.parse("2022-09-29T14:09:25+02:00"));
		assertThat(index.documents().getFirst().customerNumber()).isEqualTo("cust-1");
		assertThat(index.documents().getFirst().customerName()).isEqualTo("Customer One");
		assertThat(index.documents().getLast().source()).isEqualTo("secondInvoice.pdf");
		assertThat(index.documents().getLast().invoiceNumber()).isEqualTo("INV-2");
	}

	@Test
	void parse_missingOptionalFields() throws Exception {
		final var xml = """
			<?xml version="1.0" encoding="UTF-8"?>
			<idataOrder orderNumber="999">
				<document source="lonely.pdf">
					<invoiceNumber>INV-9</invoiceNumber>
				</document>
			</idataOrder>
			""";

		final var index = parser.parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));

		assertThat(index.orderNumber()).isEqualTo("999");
		assertThat(index.documents()).hasSize(1);
		assertThat(index.documents().getFirst().invoiceNumber()).isEqualTo("INV-9");
		assertThat(index.documents().getFirst().archiveDate()).isNull();
		assertThat(index.documents().getFirst().customerNumber()).isNull();
		assertThat(index.documents().getFirst().customerName()).isNull();
	}

	@Test
	void parse_emptyOrder() throws Exception {
		final var xml = """
			<?xml version="1.0" encoding="UTF-8"?>
			<idataOrder orderNumber="empty"></idataOrder>
			""";

		final var index = parser.parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));

		assertThat(index.orderNumber()).isEqualTo("empty");
		assertThat(index.documents()).isEmpty();
	}

	@Test
	void parse_malformedXmlThrows() {
		final var xml = "<idataOrder orderNumber=\"oops\"><document source=\"x.pdf\">";
		assertThatThrownBy(() -> parser.parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8))))
			.isInstanceOf(SAXException.class);
	}

	@Test
	void parse_doctypeRejected() {
		final var xml = """
			<?xml version="1.0" encoding="UTF-8"?>
			<!DOCTYPE foo [<!ENTITY xxe SYSTEM "file:///etc/passwd">]>
			<idataOrder orderNumber="x"></idataOrder>
			""";
		assertThatThrownBy(() -> parser.parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8))))
			.isInstanceOf(SAXException.class)
			.hasMessageContaining("DOCTYPE");
	}
}
