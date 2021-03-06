package com.disciples.feed.fulltext.hsearch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.context.junit4.SpringRunner;

import com.disciples.feed.config.JdbcHibernateSearchConfig;
import com.disciples.feed.domain.Book;
import com.disciples.feed.domain.Publisher;
import com.disciples.feed.fulltext.FullTextQuery;
import com.disciples.feed.fulltext.FullTextService;

@ContextConfiguration(classes = {JdbcHibernateSearchConfig.class})
@RunWith(SpringRunner.class)
public class JdbcHibernateSearchServiceTests {
    
    @Autowired
    private FullTextService fullTextService;
    
    @Test
    public void testFullTextConfiguration() {
        assertTrue(fullTextService instanceof JdbcHibernateSearchService);
    }
    
    @Test
    @Sql(value = "/data-jdbc.sql", config = @SqlConfig(encoding = "UTF-8"))
    public void testReindex_Query() {
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
    }
    
}
