package se.sundsvall.invoicecache.integration.party;

import static generated.se.sundsvall.party.PartyType.ENTERPRISE;
import static generated.se.sundsvall.party.PartyType.PRIVATE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import generated.se.sundsvall.party.PartyType;

@ExtendWith(value = {MockitoExtension.class, SoftAssertionsExtension.class})
class PartyClientTest {

    @Mock
    private PartyIntegration mockPartyIntegration;

    @InjectMocks
    private PartyClient partyClient;

    @Test
    void testGetLegalId_shouldReturnPartyIds_whenOnlyPrivateExists() {
        when(mockPartyIntegration.getLegalId(eq(PRIVATE), any(String.class)))
                .thenReturn(Optional.of("198001011234"));

        final String legalId = partyClient.getLegalIdsFromParty("party1");

        verify(mockPartyIntegration, times(1)).getLegalId(eq(PRIVATE), any(String.class));
        verify(mockPartyIntegration, times(0)).getLegalId(eq(ENTERPRISE), any(String.class));
        assertEquals("8001011234", legalId);
    }

    @Test
    void testGetLegalId_shouldReturnPartyIds_whenOnePrivateAndOneEnterprise() {
        when(mockPartyIntegration.getLegalId(eq(PRIVATE), eq("party1")))
                .thenReturn(Optional.empty());  //Fake that we got no match
        when(mockPartyIntegration.getLegalId(eq(ENTERPRISE), eq("party1")))
                .thenReturn(Optional.of("5591621234"));

        final String legalId = partyClient.getLegalIdsFromParty("party1");

        verify(mockPartyIntegration, times(1)).getLegalId(eq(PRIVATE), any(String.class));
        verify(mockPartyIntegration, times(1)).getLegalId(eq(ENTERPRISE), any(String.class));
        assertEquals("5591621234", legalId);
    }
}