package com.disciples.feed.annotation;

import javax.persistence.EntityManagerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.disciples.feed.fulltext.FullTextService;
import com.disciples.feed.fulltext.HibernateSearchService;

/**
 * {@code @Configuration} class that registers the Spring infrastructure beans necessary to enable 
 * hibernate search full text service.
 *
 * @author Ted Smith
 * @see com.disciples.feed.annotation.EnableFullText
 * @see com.disciples.feed.annotation.FullTextConfigurationSelector
 */
@Configuration
public class HibernateSearchServiceConfiguration extends AbstractFullTextConfiguration {
	
	@Autowired
	private EntityManagerFactory entityManagerFactory;

	@Bean
	public FullTextService fullTextService() {
		return new HibernateSearchService(entityManagerFactory);
	}
	
}
