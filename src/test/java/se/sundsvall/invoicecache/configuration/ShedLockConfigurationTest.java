package se.sundsvall.invoicecache.configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider.Configuration;
import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider.Configuration.Builder;

@ExtendWith(MockitoExtension.class)
class ShedLockConfigurationTest {

	@Spy
	private Builder builderSpy;

	@Mock
	private DataSource dataSourceMock;

	@Captor
	private ArgumentCaptor<JdbcTemplate> jdbcTemplateCaptor;

	@Test
	void testLockProvider() {
		final var shedlockConfiguration = new ShedLockConfiguration();

		try (final MockedStatic<Configuration> configurationMock = Mockito.mockStatic(Configuration.class)) {
			configurationMock.when(Configuration::builder).thenReturn(builderSpy);

			assertThat(shedlockConfiguration.lockProvider(dataSourceMock)).isNotNull();

			verify(builderSpy).usingDbTime();
			verify(builderSpy).withJdbcTemplate(jdbcTemplateCaptor.capture());
			verify(builderSpy).build();

			assertThat(jdbcTemplateCaptor.getValue().getDataSource()).isEqualTo(dataSourceMock);
		}
	}
}
