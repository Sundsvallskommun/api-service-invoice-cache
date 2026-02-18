package se.sundsvall.invoicecache.integration.party;

import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import se.sundsvall.invoicecache.Application;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = Application.class)
@ActiveProfiles("junit")
class PartyPropertiesTest {

	@Autowired
	private PartyProperties properties;

	@Test
	void verifyProperties() {
		assertThat(properties.getConnectTimeout()).isEqualTo(Duration.ofSeconds(10));
		assertThat(properties.getOauth2ClientId()).isEqualTo("client-id");
		assertThat(properties.getOauth2ClientSecret()).isEqualTo("client-secret");
		assertThat(properties.getOauth2TokenUrl()).isEqualTo("api-gateway-url");
		assertThat(properties.getReadTimeout()).isEqualTo(Duration.ofSeconds(20));
		assertThat(properties.getUrl()).isEqualTo("api-party-url");
	}

}
