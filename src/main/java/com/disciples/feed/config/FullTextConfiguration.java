package com.disciples.feed.config;

import javax.persistence.EntityManagerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.disciples.feed.fulltext.FullTextService;
import com.disciples.feed.fulltext.HibernateSearchService;

@Configuration
public class FullTextConfiguration {

	@Autowired
	private EntityManagerFactory entityManagerFactory;
	
	@Bean
	public FullTextService fullTextService() {
		return new HibernateSearchService(entityManagerFactory);
	}
	
}
