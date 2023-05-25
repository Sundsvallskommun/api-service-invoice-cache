package se.sundsvall.invoicecache.integration.raindance;

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
    public DataSourceProperties raindanceDataSourceProperties() {
        return new DataSourceProperties();
    }
    
    @Bean(name = "raindanceDataSource")
    public DataSource raindanceDataSource(@Qualifier("raindanceDataSourceProperties") DataSourceProperties dataSourceProperties) {
        return dataSourceProperties.initializeDataSourceBuilder().build();
    }
    
    @Bean(name = "randinceJdbcTemplate")
    public JdbcTemplate raindanceJdbcTemplate(@Qualifier("raindanceDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}
