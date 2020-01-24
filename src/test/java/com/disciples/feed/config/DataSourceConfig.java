package com.disciples.feed.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseFactory;

@Configuration
public class DataSourceConfig {

    @Bean
    public EmbeddedDatabase dataSource() {
        EmbeddedDatabaseFactory databaseFactory = new EmbeddedDatabaseFactory();
        databaseFactory.setDatabaseConfigurer(H2EmbeddedDatabaseConfigurer.getInstance());
        return databaseFactory.getDatabase();
    }
    
}
