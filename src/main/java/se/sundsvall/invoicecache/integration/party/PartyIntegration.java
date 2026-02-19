package se.sundsvall.invoicecache.integration.party;

import generated.se.sundsvall.party.PartyType;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.util.Optional;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;
import static se.sundsvall.invoicecache.integration.party.PartyConfiguration.REGISTRATION_ID;

@FeignClient(
	name = REGISTRATION_ID,
	url = "${integration.party.url}",
	configuration = PartyConfiguration.class,
	dismiss404 = true)
@CircuitBreaker(name = REGISTRATION_ID)
public interface PartyIntegration {

	/**
	 * Get legalID by partyId
	 *
	 * @param  municipalityId municipality id
	 * @param  partyType      "ENTERPRISE" or "PRIVATE"
	 * @param  partyId        uuid of the person or organization
	 * @return                legalId of the person or organization, Optional.empty() if not found.
	 */
	@GetMapping(path = "/{municipalityId}/{type}/{partyId}/legalId", produces = {
		TEXT_PLAIN_VALUE, APPLICATION_PROBLEM_JSON_VALUE
	})
	Optional<String> getLegalId(
		@PathVariable String municipalityId,
		@PathVariable PartyType type,
		@PathVariable String partyId);
}
