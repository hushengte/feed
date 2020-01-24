package com.disciples.feed.fulltext.hsearch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import javax.sql.DataSource;

import org.hibernate.search.cfg.spi.SearchConfiguration;
import org.hibernate.search.engine.integration.impl.ExtendedSearchIntegrator;
import org.hibernate.search.spi.SearchIntegrator;
import org.hibernate.search.spi.SearchIntegratorBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.data.domain.Page;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.jdbc.datasource.init.ScriptException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import com.disciples.feed.config.DataSourceConfig;
import com.disciples.feed.domain.Book;
import com.disciples.feed.domain.Publisher;
import com.disciples.feed.fulltext.FullTextQuery;
import com.disciples.feed.fulltext.FullTextService;

@ContextConfiguration(classes = {DataSourceConfig.class, JdbcFullTextConfig.class})
@RunWith(SpringRunner.class)
public class JdbcHibernateSearchServiceTests {
    
    @Autowired
    private DataSource dataSource;
    @Autowired
    private FullTextService fullTextService;
    
    @Test
    public void testFullTextConfiguration() {
        assertTrue(fullTextService instanceof JdbcHibernateSearchService);
    }
    
    @Test
    public void testReindex_Query() {
        try (Connection connection = dataSource.getConnection()) {
            new ResourceDatabasePopulator(new ClassPathResource("/data.sql")).populate(connection);
            
            fullTextService.reindex(Publisher.class, Book.class);
            
            FullTextQuery<Publisher> pquery = FullTextQuery.create(Publisher.class, "Sebastopol")
                    .withFields("place").setMaxResults(10);
            Page<Publisher> publisherData = fullTextService.query(pquery);
            assertEquals(4, publisherData.getTotalElements());
            
            FullTextQuery<Book> bquery = FullTextQuery.create(Book.class, "福音")
                    .withFields("name").setMaxResults(10).withAssociations("publisher");
            Page<Book> bookData = fullTextService.query(bquery);
            assertEquals(2, bookData.getTotalElements());
            for (Book book : bookData) {
                assertNotNull(book.getId());
                assertNotNull(book.getName());
                assertNotNull(book.getAuthor());
                assertNull(book.getPublisher());
            }
            
            //projection query
            FullTextQuery<Book> query = FullTextQuery.create(Book.class, "福音")
                    .withFields("name").setMaxResults(10).addProjections("name", "author");
            Page<Book> pageData = fullTextService.query(query);
            assertEquals(2, pageData.getTotalElements());
            for (Book book : pageData) {
                assertNull(book.getId());
                assertNotNull(book.getName());
                assertNotNull(book.getAuthor());
                assertNull(book.getPublisher());
            }
        } catch (ScriptException | SQLException e) {
            fail();
        }
    }
    
}

@Configuration
class JdbcFullTextConfig {
    
    @Autowired
    private DataSource dataSource;
    
    @Bean
    public JdbcOperations jdbcOperations() {
        return new JdbcTemplate(dataSource);
    }
    
    @Bean
    public FullTextService fullTextService() throws Exception {
        List<Class<?>> docClasses = Arrays.asList(Book.class, Publisher.class);
        Properties props = PropertiesLoaderUtils.loadProperties(new ClassPathResource("/application.properties"));
        SearchConfiguration searchConfig = new SimpleSearchConfiguration(props, docClasses);
        SearchIntegrator searchIntegrator = new SearchIntegratorBuilder()
                .configuration(searchConfig).buildSearchIntegrator();
        ExtendedSearchIntegrator extendedIntegrator = searchIntegrator.unwrap(ExtendedSearchIntegrator.class);
        return new JdbcHibernateSearchService(jdbcOperations(), extendedIntegrator);
    }
    
}
