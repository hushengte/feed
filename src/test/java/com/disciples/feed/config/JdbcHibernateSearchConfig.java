package com.disciples.feed.config;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import javax.sql.DataSource;

import org.hibernate.search.spi.SearchIntegrator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import com.disciples.feed.domain.Book;
import com.disciples.feed.domain.Publisher;
import com.disciples.feed.fulltext.FullTextService;
import com.disciples.feed.fulltext.hsearch.AbstractJdbcFullTextConfiguration;
import com.disciples.feed.fulltext.hsearch.JdbcHibernateSearchService;

@Configuration(proxyBeanMethods = false)
@Import(DataSourceConfig.class)
public class JdbcHibernateSearchConfig extends AbstractJdbcFullTextConfiguration {

    @Override
    protected List<Class<?>> getDocumentClasses(Properties props) {
        props.put("hibernate.search.default.indexBase", "/data/feed/jdbcindex");
        return Arrays.asList(Book.class, Publisher.class);
    }
    
    @Bean
    public FullTextService fullTextService(DataSource dataSource, SearchIntegrator searchIntegrator) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metadata = connection.getMetaData();
            if (metadata.getDriverName().contains("MySQL")) {
                // enable mysql client-side stream
                jdbcTemplate.setFetchSize(Integer.MIN_VALUE);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        return new JdbcHibernateSearchService(jdbcTemplate, searchIntegrator);
    }

}
