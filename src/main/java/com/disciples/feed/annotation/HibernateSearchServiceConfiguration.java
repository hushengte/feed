package com.disciples.feed.annotation;

import javax.persistence.EntityManagerFactory;

import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.search.hcore.impl.SearchFactoryReference;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.disciples.feed.fulltext.FullTextService;
import com.disciples.feed.fulltext.hsearch.JpaHibernateSearchService;

/**
 * {@code @Configuration} class that registers the Spring infrastructure beans necessary to enable 
 * hibernate search full text service.
 *
 * @author Ted Smith
 * @see com.disciples.feed.annotation.EnableFullText
 * @see com.disciples.feed.annotation.FullTextConfigurationSelector
 */
@Configuration(proxyBeanMethods = false)
public class HibernateSearchServiceConfiguration extends AbstractFullTextConfiguration {
	
	@Bean
	public FullTextService fullTextService(EntityManagerFactory entityManagerFactory) {
	    SessionFactoryImplementor factoryImpl = entityManagerFactory.unwrap(SessionFactoryImplementor.class);
        SearchFactoryReference searchFactoryRef = factoryImpl.getServiceRegistry()
                .getService(SearchFactoryReference.class);
        return new JpaHibernateSearchService(entityManagerFactory, searchFactoryRef.getSearchIntegrator());
	}
	
}
