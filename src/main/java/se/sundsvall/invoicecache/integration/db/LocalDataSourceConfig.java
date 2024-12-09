package se.sundsvall.invoicecache.integration.db;

import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.orm.jpa.JpaTransactionManager;

/**
 * Explicitly configure JPA to only pickup our "local" database and not anything related to raindance.
 */
@Configuration
@EntityScan(basePackages = "se.sundsvall.invoicecache.integration.db")  // Prevent Entities for raindance to be scanned.
public class LocalDataSourceConfig {

	@Bean(name = "localDataSourceProperties")
	@ConfigurationProperties(prefix = "spring.datasource")
	public DataSourceProperties localDataSourceProperties() {
		return new DataSourceProperties();
	}

	@Primary
	@Bean(name = "batchDataSource")
	@ConfigurationProperties(prefix = "spring.datasource.configuration")
	public DataSource localDataSource(@Qualifier("localDataSourceProperties") DataSourceProperties dataSourceProperties) {
		return dataSourceProperties.initializeDataSourceBuilder().type(HikariDataSource.class).build();
	}

	@Primary
	@Bean(name = "transactionManager")
	public JpaTransactionManager jpaTransactionManager(@Qualifier("batchDataSource") DataSource dataSource) {
		final JpaTransactionManager transactionManager = new JpaTransactionManager();
		transactionManager.setDataSource(dataSource);
		return transactionManager;
	}
}
