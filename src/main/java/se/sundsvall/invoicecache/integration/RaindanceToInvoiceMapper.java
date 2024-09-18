package se.sundsvall.invoicecache.integration;

import static org.apache.commons.lang3.StringUtils.trim;
import static se.sundsvall.invoicecache.util.OCR.IN_PATTERN;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import se.sundsvall.invoicecache.api.model.InvoiceStatus;
import se.sundsvall.invoicecache.integration.db.entity.InvoiceEntity;
import se.sundsvall.invoicecache.integration.raindance.RaindanceQueryResultDto;
import se.sundsvall.invoicecache.util.OCR;

@Component
public class RaindanceToInvoiceMapper {

	private static final String ZIP_AND_CITY_REGEX = "^(s-|S-)?\\d{3}\\s?\\d{2}\\s?.{2,}$";

	private static final String MUNICIPALITY_ID = "2281"; // Hardcoded for now

	public InvoiceEntity mapRaindanceDtoToInvoice(final RaindanceQueryResultDto dto) {
		final InvoiceEntity invoice = new InvoiceEntity();

		invoice.setClaimLevel(dto.getKravniva());
		invoice.setCustomerId(trim(dto.getKundid()));
		invoice.setCustomerName(trim(dto.getKundidText()));
		invoice.setCustomerName2(trim(dto.getNamn2()));
		invoice.setCustomerType(trim(dto.getKundrtyp()));
		invoice.setFileName(trim(dto.getFilnamn()));
		invoice.setInvoiceAmount(dto.getBeloppSek());
		invoice.setInvoiceCreatedDate(mapTimeStampToLocalDate(dto.getUtskrdatum()));
		invoice.setInvoiceDate(mapTimeStampToLocalDate(dto.getFakturadatum()));
		invoice.setInvoiceDueDate(mapTimeStampToLocalDate(dto.getForfallodatum()));
		invoice.setInvoiceNumber(trim(String.valueOf(dto.getNr())));
		invoice.setInvoiceReference(trim(dto.getVREF()));
		invoice.setInvoiceReminderDate(mapTimeStampToLocalDate(dto.getBetpamdatum()));
		if (dto.isVoid()) {
			invoice.setInvoiceStatus(InvoiceStatus.VOID.getStatus());
		} else {
			invoice.setInvoiceStatus(trim(dto.getFaktstatus()));
		}
		invoice.setInvoiceStatus2(trim(dto.getFaktstatus2()));
		invoice.setOcrNumber(trim(getOrCalculateOcr(dto)));
		invoice.setOrganizationNumber(trim(dto.getOrgnr()));
		invoice.setPaidAmount(dto.getBetaltSek());
		invoice.setPaymentStatus(trim(dto.getTabBehand()));
		invoice.setStreet(trim(dto.getAdr2()));
		invoice.setVat(dto.getMomsVal());
		invoice.setMunicipalityId(MUNICIPALITY_ID);

		setZipAndCityIfCorrectFormat(dto, invoice);

		return invoice;
	}

	/**
	 * Map reminder date. If it's the default value in raindance, the SQL query will map it to null.
	 *
	 * @param timestamp - might be null
	 * @return LocalDate or null
	 */
	LocalDate mapTimeStampToLocalDate(final Timestamp timestamp) {
		return Optional.ofNullable(timestamp)
			.map(date -> date.toLocalDateTime().toLocalDate())
			.orElse(null);
	}

	/**
	 * Make sure we can parse zip and city, if we cannot the city will contain the "full" value from "ort" and zip will be
	 * null
	 *
	 * @param dto - raindance dto
	 * @param invoice - invoice entity
	 */
	void setZipAndCityIfCorrectFormat(final RaindanceQueryResultDto dto, final InvoiceEntity invoice) {
		if (zipAndCityHasValidFormat(dto.getOrt())) {
			invoice.setCity(dto.getOrt().trim().substring(7));
			invoice.setZip(dto.getOrt().trim().substring(0, 6));
		} else {
			invoice.setCity(dto.getOrt().trim());
		}
	}

	/**
	 * Check if the OCR-number is present. If not, also check that the customerType is "KA".
	 * If that's the case, generate an OCR number.
	 *
	 * @param dto - raindance dto
	 * @return the OCR-number
	 */
	String getOrCalculateOcr(final RaindanceQueryResultDto dto) {
		// Check if the OCR-number is missing and we have the correct customer type, then calculate it.
		if (isOcrBlankAndCustomerTypeKA(dto) && blopnrAndBeadatIsPresent(dto)) {
			// Also check that we have blopnr and beadat
			return OCR.generate(dto.getZ11_blopnr(), dto.getZ11_beadat()
				.toLocalDateTime()
				.format(IN_PATTERN));
		}

		return dto.getOcrnr();
	}

	boolean isOcrBlankAndCustomerTypeKA(final RaindanceQueryResultDto dto) {
		return StringUtils.isBlank(dto.getOcrnr()) && "KA".equalsIgnoreCase(dto.getKundrtyp().trim());
	}

	boolean blopnrAndBeadatIsPresent(final RaindanceQueryResultDto dto) {
		return (dto.getZ11_blopnr() != 0) && (dto.getZ11_beadat() != null);
	}

	/**
	 * Chcl if the zip and city is on a format we expect.
	 * e.g. "876 23 SOMECITY" will validate while e.g. "NO-0251 Oslo" will not.
	 *
	 * @return true/false
	 */
	boolean zipAndCityHasValidFormat(final String zipAndCity) {
		if (StringUtils.isNotBlank(zipAndCity)) {
			return zipAndCity.trim().matches(ZIP_AND_CITY_REGEX);
		}
		return false;
	}

}
