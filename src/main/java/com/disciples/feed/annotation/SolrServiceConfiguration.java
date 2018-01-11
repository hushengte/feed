package com.disciples.feed.annotation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.solr.core.SolrTemplate;

import com.disciples.feed.fulltext.FullTextService;
import com.disciples.feed.fulltext.SolrService;

/**
 * {@code @Configuration} class that registers the Spring infrastructure beans necessary to 
 * enable solr full text service.
 *
 * @author Ted Smith
 * @see com.disciples.feed.annotation.EnableFullText
 * @see com.disciples.feed.annotation.FullTextConfigurationSelector
 */
@Configuration
public class SolrServiceConfiguration extends AbstractFullTextConfiguration {
	
	@Autowired
	private SolrTemplate solrTemplate;

	@Bean
	public FullTextService fullTextService() {
		return new SolrService(solrTemplate);
	}
	
}
