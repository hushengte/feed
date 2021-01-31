package com.disciples.feed.config;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import javax.sql.DataSource;

import org.hibernate.search.cfg.spi.SearchConfiguration;
import org.hibernate.search.engine.integration.impl.ExtendedSearchIntegrator;
import org.hibernate.search.spi.SearchIntegrator;
import org.hibernate.search.spi.SearchIntegratorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;

import com.disciples.feed.domain.Book;
import com.disciples.feed.domain.Publisher;
import com.disciples.feed.fulltext.FullTextService;
import com.disciples.feed.fulltext.hsearch.JdbcHibernateSearchService;
import com.disciples.feed.fulltext.hsearch.SimpleSearchConfiguration;

@Configuration(proxyBeanMethods = false)
@Import(DataSourceConfig.class)
public class JdbcHibernateSearchConfig {

    @Bean
    public JdbcOperations jdbcOperations(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
    
    @Bean
    public ExtendedSearchIntegrator searchIntegrator() {
        List<Class<?>> docClasses = Arrays.asList(Book.class, Publisher.class);
        Properties props = new Properties();
        props.put("hibernate.search.lucene_version", "LUCENE_CURRENT");
        props.put("hibernate.search.default.indexBase", "/data/feed/jdbcindex");
        SearchConfiguration searchConfig = new SimpleSearchConfiguration(props, docClasses);
        SearchIntegrator searchIntegrator = new SearchIntegratorBuilder()
                .configuration(searchConfig).buildSearchIntegrator();
        return searchIntegrator.unwrap(ExtendedSearchIntegrator.class);
    }
    
    @Bean
    public FullTextService fullTextService(JdbcOperations jdbcOperations, ExtendedSearchIntegrator searchIntegrator) {
        return new JdbcHibernateSearchService(jdbcOperations, searchIntegrator);
    }
    
}
