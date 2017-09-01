package com.disciples.feed.service.manage;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.disciples.feed.config.ServiceConfig;
import com.disciples.feed.dao.BookAuthorDao;
import com.disciples.feed.domain.Author;
import com.disciples.feed.domain.Book;
import com.disciples.feed.domain.BookAuthor;
import com.disciples.feed.domain.Publisher;
import com.disciples.feed.manage.ManageService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@ContextConfiguration(classes = ServiceConfig.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class ManageServiceTest {

	@Autowired
	private ManageService manageService;
	@Autowired
	private BookAuthorDao bookAuthorDao;
	@Autowired
	private ObjectMapper objectMapper;
	
	@Test
	public void testFindBook() throws JsonProcessingException {
		Publisher publisher = (Publisher) manageService.save(new Publisher("InterVassy Press", "England"));
		
		Book book = new Book();
		book.setName("The mission of God");
		book.setPublishYear("2006");
		book.setPublisher(publisher);
		manageService.save(book);
		
		Page<Book> pageData = manageService.find(Book.class, 0, 10, null);
		for (Book model : pageData.getContent()) {
			System.out.println(objectMapper.writeValueAsString(model));
		}
	}
	
	@Test
	public void testFindPublisher() throws JsonProcessingException {
		Publisher publisher = (Publisher) manageService.save(new Publisher("InterVassy Press", "England"));
		
		Book book = new Book();
		book.setName("The mission of God");
		book.setPublishYear("2006");
		book.setPublisher(publisher);
		manageService.save(book);
		
		Book book2 = new Book();
		book.setName("The mission of God's people");
		book.setPublishYear("2007");
		book.setPublisher(publisher);
		manageService.save(book2);
		
		Page<Publisher> pageData = manageService.find(Publisher.class, 0, 10, null);
		for (Publisher model : pageData.getContent()) {
			System.out.println(objectMapper.writeValueAsString(model));
		}
	}
	
	@Test
	public void testFindBookAuthors() throws JsonProcessingException {
		Publisher publisher = (Publisher) manageService.save(new Publisher("InterVassy Press", "England"));
		Set<Author> authors = new HashSet<Author>();
		authors.add(new Author("Christopher J.H. Wright", "AUTHOR"));
		authors.add(new Author("Unknown", "TRANSLATOR"));
		manageService.save(authors);
		
		Book book = new Book();
		book.setName("The mission of God");
		book.setPublishYear("2006");
		book.setPublisher(publisher);
		manageService.save(book);
		
		for (Author author : authors) {
			bookAuthorDao.save(new BookAuthor(book, author));
		}
		
		Page<Book> pageData = manageService.find(Book.class, 0, 10, null);
		for (Book model : pageData.getContent()) {
			System.out.println(objectMapper.writeValueAsString(model));
		}
		
		System.out.println("=============================================");
//		Book model = manageService.getDao().findOne(book.getId());
//		Book dto = bookManageService.toDto(model);
//		System.out.println(objectMapper.writeValueAsString(dto));
	}
	
}
