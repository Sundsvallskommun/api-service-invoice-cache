package se.sundsvall.invoicecache.integration.storage.importer;

import java.io.IOException;
import java.io.InputStream;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.springframework.stereotype.Component;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import se.sundsvall.invoicecache.integration.storage.importer.model.InvoiceIndex;
import se.sundsvall.invoicecache.integration.storage.importer.model.InvoiceIndexEntry;

@Component
public class IndexXmlParser {

	public InvoiceIndex parse(final InputStream xml) throws IOException, SAXException, ParserConfigurationException {
		final var factory = DocumentBuilderFactory.newInstance();
		factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
		factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
		factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
		factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
		factory.setXIncludeAware(false);
		factory.setExpandEntityReferences(false);

		final var builder = factory.newDocumentBuilder();
		final var document = builder.parse(xml);
		document.getDocumentElement().normalize();

		final var root = document.getDocumentElement();
		final var orderNumber = root.getAttribute("orderNumber");

		final var entries = new ArrayList<InvoiceIndexEntry>();
		final NodeList nodes = root.getElementsByTagName("document");
		for (var i = 0; i < nodes.getLength(); i++) {
			final var node = nodes.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				entries.add(toEntry((Element) node));
			}
		}
		return new InvoiceIndex(orderNumber, List.copyOf(entries));
	}

	private InvoiceIndexEntry toEntry(final Element element) {
		return new InvoiceIndexEntry(
			emptyToNull(element.getAttribute("source")),
			text(element, "invoiceNumber"),
			parseDate(text(element, "archiveDate")),
			text(element, "customerNumber"),
			text(element, "customerName"));
	}

	private static String text(final Element parent, final String tag) {
		final var nodes = parent.getElementsByTagName(tag);
		if (nodes.getLength() == 0) {
			return null;
		}
		return emptyToNull(nodes.item(0).getTextContent());
	}

	private static String emptyToNull(final String value) {
		return Optional.ofNullable(value)
			.map(String::trim)
			.filter(s -> !s.isEmpty())
			.orElse(null);
	}

	private static OffsetDateTime parseDate(final String value) {
		return Optional.ofNullable(value)
			.map(OffsetDateTime::parse)
			.orElse(null);
	}
}
