package se.sundsvall.invoicecache.integration.raindance;

import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class RaindanceDataSourceConfig {

	@Bean(name = "raindanceDataSourceProperties")
	@ConfigurationProperties(prefix = "spring.raindance-datasource")
	DataSourceProperties raindanceDataSourceProperties() {
		return new DataSourceProperties();
	}

	@Bean(name = "raindanceDataSource")
	@ConfigurationProperties(prefix = "spring.raindance-datasource.configuration")
	DataSource raindanceDataSource(@Qualifier("raindanceDataSourceProperties") DataSourceProperties dataSourceProperties) {
		return dataSourceProperties.initializeDataSourceBuilder().type(HikariDataSource.class).build();
	}

	@Bean(name = "randinceJdbcTemplate")
	JdbcTemplate raindanceJdbcTemplate(@Qualifier("raindanceDataSource") DataSource dataSource) {
		return new JdbcTemplate(dataSource);
	}
}
