package com.disciples.feed.service;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.disciples.feed.AuthorAnalyzer;
import com.disciples.feed.config.ServiceConfig;
import com.disciples.feed.domain.Book;
import com.disciples.feed.domain.Publisher;
import com.disciples.feed.fulltext.FullTextQuery;
import com.disciples.feed.fulltext.FullTextService;

@ContextConfiguration(classes = ServiceConfig.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class FullTextServiceTest {

	@Autowired
	private FullTextService fullTextService;
	
	@Test
	public void testReindex() {
		fullTextService.reindex(Publisher.class, Book.class);
	}
	
	@Test
	public void testQuery() {
		FullTextQuery<Book> query = FullTextQuery.create(Book.class, "周天和");
		query.withFields("author").setMaxResults(80).setHighlight(true);
		Page<Book> result = fullTextService.query(query);
		System.out.println("Total=" + result.getTotalElements());
		for (Book book : result.getContent()) {
			System.out.println(String.format("id=%d, name=%s, author=%s", book.getId(), book.getName(), book.getAuthor()));
		}
	}
	
	@Test
	public void testAuthorAnalyzer() {
		System.out.println(analyzer("author", "[{\"names\":[\"麦克奈特(Scot McKnight)\"],\"level\":\"著\"},{\"names\":[\"麦启新等\",\"麦陈惠惠\"],\"level\":\"译\"}]"));
		System.out.println(analyzer("author", "[{\"names\":[\"傅士德\"],\"level\":\"著\"},{\"names\":[\"周天和等\"],\"level\":\"译\"}]"));
	}
	
	private List<String> analyzer(String field, String localText) {
		try {
			Reader reader = new StringReader(localText);
			@SuppressWarnings("resource")
			TokenStream stream = new AuthorAnalyzer().tokenStream(field, reader);
			List<String> terms = new ArrayList<String>();
			try {
				CharTermAttribute attribute = stream.addAttribute(CharTermAttribute.class);
				stream.reset();
				while (stream.incrementToken()) {
					if (attribute.length() > 0) {
						String term = new String(attribute.buffer(), 0, attribute.length());
						terms.add(term);
					}
				}
				stream.end();
			}
			finally {
				stream.close();
			}
			return terms;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return Collections.emptyList();
	}
	
}
