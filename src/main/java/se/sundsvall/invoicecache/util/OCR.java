package se.sundsvall.invoicecache.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.apache.commons.validator.routines.checkdigit.CheckDigitException;
import org.apache.commons.validator.routines.checkdigit.LuhnCheckDigit;
import se.sundsvall.invoicecache.util.exception.InvoiceCacheException;

public final class OCR {

	private OCR() {}

	private static final LuhnCheckDigit LUHN = new LuhnCheckDigit();

	public static final DateTimeFormatter IN_PATTERN = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
	private static final DateTimeFormatter OUT_PATTERN = DateTimeFormatter.ofPattern("yyMMdd");

	public static String generate(final int Z11_BLOPNR, final String Z11_BEADAT) {
		final var sb = new StringBuilder()
			.append("2")		// Fast värde som talar om att det är en påminnelse - alltid 2
			.append("300")		// Företagsnummer - kommunen har 300
			.append("1")		// Nummer (för BSB) - alltid 1
			.append("2");		// Kravnivå - alltid 2

		// Formatera om datumet Z11_BEADAT
		sb.append(LocalDate.parse(Z11_BEADAT, IN_PATTERN).format(OUT_PATTERN));

		// Padda Z11_BLOPNR med nollor i början
		final var blopNrLength = (int) (Math.log10(Z11_BLOPNR) + 1);
		sb.append("00000".substring(blopNrLength)).append(Z11_BLOPNR);

		// Längd (tiotalssiffran utelämnad)
		sb.append((sb.length() + 2) % 10);

		// Kontrollsiffra
		try {
			return sb + LUHN.calculate(sb.toString());
		} catch (final CheckDigitException e) {
			throw new InvoiceCacheException("Unable to calculate check digit for OCR", e);
		}
	}
}
