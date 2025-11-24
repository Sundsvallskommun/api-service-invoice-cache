package se.sundsvall.invoicecache.integration.raindance.samba;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jcifs.context.SingletonContext;
import jcifs.smb.NtlmPasswordAuthenticator;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
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
		assertThat(properties.domain()).isEqualTo("samba-domain/");
		assertThat(properties.password()).isEqualTo("samba-password");
		assertThat(properties.remoteDir()).isEqualTo("samba-remote-dir");
		assertThat(properties.shareAndDir()).isEqualTo("samba-share-and-dir/");
		assertThat(properties.user()).isEqualTo("samba-user");
		assertThat(properties.userDomain()).isEqualTo("samba-user-domain");
	}

	@Test
	void targetUrlTest() {
		var sourceUrl = properties.targetUrl();

		assertThat(sourceUrl).isEqualTo("smb://samba-domain/samba-share-and-dir/samba-remote-dir");
	}

	@Test
	void cifsContextTest() {
		var mockedContext = Mockito.mock(SingletonContext.class);
		var argumentCaptor = ArgumentCaptor.forClass(NtlmPasswordAuthenticator.class);
		try (final var mockedStatic = mockStatic(SingletonContext.class)) {
			mockedStatic.when(SingletonContext::getInstance).thenReturn(mockedContext);
			when(mockedContext.withCredentials(Mockito.any(NtlmPasswordAuthenticator.class))).thenCallRealMethod();
			var cifsContext = properties.cifsContext();
			assertThat(cifsContext).isNotNull();
			verify(mockedContext).withCredentials(argumentCaptor.capture());
			var value = argumentCaptor.getValue();
			assertThat(value.getName()).isEqualTo("samba-user-domain\\samba-user");
			assertThat(value.getUsername()).isEqualTo("samba-user");
			assertThat(value.getPassword()).isEqualTo("samba-password");
			assertThat(value.getUserDomain()).isEqualTo("samba-user-domain");
		}
	}

}
