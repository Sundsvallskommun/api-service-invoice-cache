package se.sundsvall.invoicecache.integration.storage;

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
class StorageSambaPropertiesTest {

	@Autowired
	private StorageSambaProperties properties;

	@Test
	void verifyProperties() {
		assertThat(properties).isNotNull();
		assertThat(properties.user()).isEqualTo("user");
		assertThat(properties.password()).isEqualTo("password");
		assertThat(properties.userDomain()).isEqualTo("user-domain");
		assertThat(properties.hostname()).isEqualTo("samba-hostname");
		assertThat(properties.baseDirectory()).isEqualTo("abc");
		assertThat(properties.serviceDirectory()).isEqualTo("invoice-cache");
		assertThat(properties.environment()).isEqualTo("test");
	}

	@Test
	void targetUrlTest() {
		var sourceUrl = properties.targetUrl();

		assertThat(sourceUrl).isEqualTo("smb://samba-hostname/abc/invoice-cache/test");
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
			assertThat(value.getName()).isEqualTo("user-domain\\user");
			assertThat(value.getUsername()).isEqualTo("user");
			assertThat(value.getPassword()).isEqualTo("password");
			assertThat(value.getUserDomain()).isEqualTo("user-domain");
		}
	}

}
