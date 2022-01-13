package com.disciples.feed.json;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import com.disciples.feed.config.HibernateConfig;
import com.disciples.feed.dao.jpa.BookDao;
import com.disciples.feed.dao.jpa.PublisherDao;
import com.disciples.feed.domain.jpa.Book;
import com.disciples.feed.domain.jpa.Publisher;
import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {HibernateConfig.class})
public class HibernateProxyModuleTest {

    @Autowired
    private BookDao bookDao;
    @Autowired
    private PublisherDao publisherDao;
    @Autowired
    private EntityManagerFactory entityManagerFactory;
    
    private ObjectMapper objectMapper;
    
    @Before
    public void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new HibernateProxyModule());
    }
    
    String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            fail();
        }
        return null;
    }
    
    <T> T parseJson(String json, Class<T> resultType) {
        try {
            return objectMapper.readValue(json, resultType);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    @Test
    public void testSerializeInitializedProxy() {
        Publisher publisher = publisherDao.save(new Publisher("Eerdmans", "Michigan"));
        Book saved = bookDao.save(new Book("Test", null, publisher));
        EntityManager em = entityManagerFactory.createEntityManager();
        try {
            Book book = em.find(Book.class, saved.getId());
            Map<?, ?> bookData = parseJson(toJson(book), Map.class);
            Map<?, ?> publisherData = (Map<?, ?>) bookData.get("publisher");
            assertNotNull(publisherData.get("name"));
            
            bookData = parseJson(toJson(book), Map.class);
            publisherData = (Map<?, ?>) bookData.get("publisher");
            assertEquals(publisher.getName(), publisherData.get("name"));
        } finally {
            em.close();
        }
    }
    
}
