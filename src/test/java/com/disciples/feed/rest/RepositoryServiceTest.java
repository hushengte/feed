package com.disciples.feed.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.ReflectionUtils;

import com.disciples.feed.config.HibernateConfig;
import com.disciples.feed.config.RepositoryConfiguration;
import com.disciples.feed.dao.BookDao;
import com.disciples.feed.dao.PublisherDao;
import com.disciples.feed.domain.Book;
import com.disciples.feed.domain.Publisher;

@ContextConfiguration(classes = {HibernateConfig.class, RepositoryConfiguration.class})
@RunWith(SpringRunner.class)
public class RepositoryServiceTest {

    @Autowired
    private BookDao bookDao;
    @Autowired
    private PublisherDao publisherDao;
	@Autowired
	private RepositoryService repositoryService;
	
	@Test
	public void testGetDomainClass() {
	    assertEquals(Book.class, repositoryService.getDomainClass("book"));
	    assertEquals(Publisher.class, repositoryService.getDomainClass("publisher"));
	}
	
	private void cleanData() {
	    bookDao.deleteAll();
	    publisherDao.deleteAll();
	}
	
	@Test
	public void testGetKeyValues() {
	    cleanData();
	    
	    List<?> bookKvs = repositoryService.getKeyValues(Book.class, null);
	    assertTrue(bookKvs.isEmpty());
	    
	    Publisher p1 = new Publisher("Test1", "HangZhou");
	    Publisher p2 = new Publisher("Test2", "HangZhou");
        repositoryService.save(p1);
        repositoryService.save(p2);
	    List<?> publisherKvs = repositoryService.getKeyValues(Publisher.class, null);
	    assertEquals(2, publisherKvs.size());
	    Map<?, ?> p1Map = (Map<?, ?>) publisherKvs.get(0);
	    Map<?, ?> p2Map = (Map<?, ?>) publisherKvs.get(1);
	    assertEquals(p1.getId(), p1Map.get("key"));
	    assertEquals(p2.getId(), p2Map.get("key"));
	    assertEquals(p1.getName(), p1Map.get("value"));
	    assertEquals(p2.getName(), p2Map.get("value"));
	}
	
	@Test
    public void testDeleteHandleException() {
	    Book book = createOneBook();
	    Assertions.assertThatExceptionOfType(RepositoryException.class).isThrownBy(() -> {
	        repositoryService.delete(Publisher.class, book.getPublisher().getId());
	    }).withMessage("删除失败：请先删除关联数据再操作");
    }
    
    private Book createOneBook() {
        Publisher publisher = new Publisher("Eerdmans", "Michigan");
        repositoryService.save(publisher);
        Book book = new Book();
        book.setName("The Apostolic Preaching");
        book.setPublishYear("1955");
        book.setPublisher(publisher);
        return repositoryService.save(book);
    }
    
	@Test
	public void testSave_Delete_FindPageableWithMethod() {
	    cleanData();
	    
	    Book book = createOneBook();
	    Publisher publisher = book.getPublisher();
		Page<Book> pageData = repositoryService.find(Book.class, 0, 10);
		assertOneBookPage(book, pageData);
		
		String methodName = "findByOrderByLastUpdateDesc";
		Method bookDaoMethod = ReflectionUtils.findMethod(BookDao.class, methodName, Pageable.class);
		assertNotNull(bookDaoMethod);
		MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
		params.add("method", methodName);
		pageData = repositoryService.find(Book.class, 0, 10, params);
		assertOneBookPage(book, pageData);
		
		repositoryService.delete(Book.class, book.getId());
		assertNull(bookDao.findOne(book.getId()));
		repositoryService.delete(Publisher.class, publisher.getId());
		assertNull(publisherDao.findOne(publisher.getId()));
	}
	
	private void assertOneBookPage(Book book, Page<Book> pageData) {
	    assertEquals(1, pageData.getTotalElements());
        List<Book> content = pageData.getContent();
        assertEquals(1, content.size());
        Book find = content.get(0);
        assertEquals(book.getName(), find.getName());
        assertEquals(book.getPublishYear(), find.getPublishYear());
        assertEquals(book.getName(), find.getName());
        assertEquals(book.getPublisher().getId(), find.getPublisher().getId());
	}
	
}
