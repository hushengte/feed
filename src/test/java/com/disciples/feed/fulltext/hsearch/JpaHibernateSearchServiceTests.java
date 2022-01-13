package com.disciples.feed.fulltext.hsearch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.context.junit4.SpringRunner;

import com.disciples.feed.config.JpaHibernateSearchConfig;
import com.disciples.feed.dao.jpa.BookDao;
import com.disciples.feed.dao.jpa.PublisherDao;
import com.disciples.feed.domain.jpa.Book;
import com.disciples.feed.domain.jpa.Publisher;
import com.disciples.feed.fulltext.FullTextQuery;
import com.disciples.feed.fulltext.FullTextService;

@ContextConfiguration(classes = {JpaHibernateSearchConfig.class})
@RunWith(SpringRunner.class)
public class JpaHibernateSearchServiceTests {

    @Autowired
    private BookDao bookDao;
    @Autowired
    private PublisherDao publisherDao;
    @Autowired
    private FullTextService fullTextService;
    
    @Test
    public void testFullTextConfigurationSelector() {
        assertTrue(fullTextService instanceof JpaHibernateSearchService);
    }
    
    @Test
    @Sql(value = "/data-jpa.sql", config = @SqlConfig(encoding = "UTF-8"))
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
        assertEquals(bookData.getTotalElements(), bookData.getContent().size());
        for (Book book : bookData) {
            assertNotNull(book.getId());
            assertNotNull(book.getName());
            assertNotNull(book.getPublisher());
        }
    }
    
    void initBook() {
        Publisher publisher = new Publisher("Eerdmans", "Michigan");
        Publisher savedPublisher = publisherDao.save(publisher);
        List<Book> books = Arrays.asList(
                new Book("Test1", "John", savedPublisher),
                new Book("Test2", "Mark", savedPublisher)
                );
        bookDao.saveAll(books);
    }
    
    @Test
    public void testProjectionQuery() {
        initBook();
        
        final CountDownLatch indexFinish = new CountDownLatch(1);
        new Thread(new Runnable() {
            @Override
            public void run() {
                fullTextService.reindex(Book.class);
                indexFinish.countDown();
            }
        }).start();
        
        try {
            indexFinish.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        FullTextQuery<Book> query = FullTextQuery.create(Book.class, "test")
                .withFields("name").setMaxResults(1).addProjections("name", "author");
        Page<Book> pageData = fullTextService.query(query);
        assertEquals(2, pageData.getTotalElements());
        assertEquals(query.getMaxResults(), pageData.getContent().size());
        for (Book book : pageData) {
            assertNull(book.getId());
            assertNull(book.getPublisher());
            assertNotNull(book.getName());
            assertNotNull(book.getAuthor());
        }
    }
    
}
