package se.sundsvall.invoicecache.integration.db;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import jakarta.activation.DataSource;


/**
 * Explicitly configure JPA to only pickup our "local" database and not anything related to raindance.
 */
@Configuration
@EntityScan(basePackages = "se.sundsvall.invoicecache.integration.db")  //Prevent Entities for raindance to be scanned.
public class LocalConfig {
    
    @Bean(name = "localDataSourceProperties")
    @ConfigurationProperties(prefix = "spring.datasource")
    public DataSourceProperties localDataSourceProperties() {
        return new DataSourceProperties();
    }
    
    @Primary
    @Bean
    public DataSource localDataSource(@Qualifier("localDataSourceProperties") DataSourceProperties dataSourceProperties) {
        return dataSourceProperties.initializeDataSourceBuilder().build();
    }
}

