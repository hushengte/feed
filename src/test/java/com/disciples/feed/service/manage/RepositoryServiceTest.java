package com.disciples.feed.service.manage;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.disciples.feed.config.RepositoryRestConfiguration;
import com.disciples.feed.config.ServiceConfig;
import com.disciples.feed.domain.Book;
import com.disciples.feed.domain.Publisher;
import com.disciples.feed.rest.RepositoryService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@ContextConfiguration(classes = {ServiceConfig.class, RepositoryRestConfiguration.class})
@RunWith(SpringJUnit4ClassRunner.class)
public class RepositoryServiceTest {

	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private ObjectMapper objectMapper;
	
	private void print(Iterable<?> datas) {
		for (Object data : datas) {
			try {
				System.out.println(objectMapper.writeValueAsString(data));
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}
		}
	}
	
	@Test
	public void testFindBook() {
		Publisher publisher = (Publisher) repositoryService.save(new Publisher("aaaa", "England"));
		
		Book book = new Book();
		book.setName("The mission of God");
		book.setPublishYear("2006");
		book.setPublisher(publisher);
		repositoryService.save(book);
		
		print(repositoryService.find(Book.class, 0, 10, null));
	}
	
	@Test
	public void testFindPublisher() {
		Publisher publisher = (Publisher) repositoryService.save(new Publisher("InterVassy Press", "England"));
		
		Book book = new Book();
		book.setName("The mission of God");
		book.setPublishYear("2006");
		book.setPublisher(publisher);
		repositoryService.save(book);
		
		Book book2 = new Book();
		book.setName("The mission of God's people");
		book.setPublishYear("2007");
		book.setPublisher(publisher);
		repositoryService.save(book2);
		
		print(repositoryService.find(Publisher.class, 0, 10, null));
	}
	
	@Test
	public void testFindByMethodName() {
		MultiValueMap<String, Object> params = new LinkedMultiValueMap<String, Object>();
		params.add("method", "findByNameContaining");
		params.add("keyword", "生活");
		print(repositoryService.find(Publisher.class, 0, 10, params));
	}
	
}
