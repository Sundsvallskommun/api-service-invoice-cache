package se.sundsvall.invoicecache.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class OCRTests {

	@Test
	void testGenerate() {
		assertEquals("2300122201200028297", OCR.generate(282, "2022-01-20 00:00:00.000"));
		assertEquals("2300122107220003998", OCR.generate(39, "2021-07-22 00:00:00.000"));
		assertEquals("2300122107220025090", OCR.generate(250, "2021-07-22 00:00:00.000"));
		assertEquals("2300122111220018592", OCR.generate(185, "2021-11-22 00:00:00.000"));
		assertEquals("2300122111220032098", OCR.generate(320, "2021-11-22 00:00:00.000"));
		assertEquals("2300122106210005997", OCR.generate(59, "2021-06-21 00:00:00.000"));
	}
}
