package se.sundsvall.invoicecache.integration.storage;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import se.sundsvall.invoicecache.Application;

@SpringBootTest(classes = Application.class)
@ActiveProfiles("junit")
class StorageSambaPropertiesTest {

	@Autowired
	private StorageSambaProperties properties;

	@Test
	void verifyProperties() {
		assertThat(properties).isNotNull();
		assertThat(properties.user()).isEqualTo("user");
		assertThat(properties.password()).isEqualTo("password");
		assertThat(properties.userDomain()).isEqualTo("user-domain");
		assertThat(properties.host()).isEqualTo("samba-host");
		assertThat(properties.port()).isEqualTo(445);
		assertThat(properties.share()).isEqualTo("abc");
		assertThat(properties.serviceDirectory()).isEqualTo("invoice-cache");
		assertThat(properties.environment()).isEqualTo("test");
	}

	@Test
	void targetUrlTest() {
		var sourceUrl = properties.targetUrl();

		assertThat(sourceUrl).isEqualTo("smb://samba-host:445/abc/invoice-cache/test");
	}

}
