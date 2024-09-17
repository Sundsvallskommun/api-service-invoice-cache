package se.sundsvall.invoicecache.integration.party;

import static generated.se.sundsvall.party.PartyType.ENTERPRISE;
import static generated.se.sundsvall.party.PartyType.PRIVATE;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * Class to interact with the PartyIntegration.
 */
@Component
public class PartyClient {

	private final PartyIntegration partyIntegration;

	public PartyClient(final PartyIntegration partyIntegration) {
		this.partyIntegration = partyIntegration;
	}

	/**
	 * Get legalIds for the partyId sent in.
	 *
	 * @param  partyId
	 * @return         a legalIds
	 */
	public String getLegalIdsFromParty(final String partyId, final String municipalityId) {

		return partyIntegration.getLegalId(municipalityId,PRIVATE, partyId)
			// Since raindance on¢¢ly handles legalIds without century digits, for private persons.
			.map(legalId -> StringUtils.substring(legalId, 2))
			.or(() -> partyIntegration.getLegalId(municipalityId,ENTERPRISE, partyId))
			.orElse("");
	}
}
