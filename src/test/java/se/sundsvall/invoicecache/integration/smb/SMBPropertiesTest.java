package se.sundsvall.invoicecache.integration.smb;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import se.sundsvall.invoicecache.Application;

@SpringBootTest(classes = Application.class)
@ActiveProfiles("junit")
class SMBPropertiesTest {

	@Autowired
	private SMBProperties properties;

	@Test
	void verifyProperties() {
		assertThat(properties.getDomain()).isEqualTo("smb-domain");
		assertThat(properties.getPassword()).isEqualTo("smb-password");
		assertThat(properties.getRemoteDir()).isEqualTo("smb-remote-dir");
		assertThat(properties.getShareAndDir()).isEqualTo("smb-share-and-dir");
		assertThat(properties.getUser()).isEqualTo("smb-user");
		assertThat(properties.getUserDomain()).isEqualTo("smb-user-domain");
	}

}
