package se.sundsvall.invoicecache.integration.raindance.samba;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import se.sundsvall.invoicecache.Application;

@SpringBootTest(classes = Application.class)
@ActiveProfiles("junit")
class RaindanceSambaPropertiesTest {

	@Autowired
	private RaindanceSambaProperties properties;

	@Test
	void verifyProperties() {
		assertThat(properties.domain()).isEqualTo("samba-domain");
		assertThat(properties.password()).isEqualTo("samba-password");
		assertThat(properties.remoteDir()).isEqualTo("samba-remote-dir");
		assertThat(properties.shareAndDir()).isEqualTo("samba-share-and-dir");
		assertThat(properties.user()).isEqualTo("samba-user");
		assertThat(properties.userDomain()).isEqualTo("samba-user-domain");
	}

}
