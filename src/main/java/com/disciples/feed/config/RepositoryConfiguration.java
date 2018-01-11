package com.disciples.feed.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.repository.support.Repositories;

import com.disciples.feed.rest.RepositoryService;

@Configuration
public class RepositoryConfiguration {

	@Autowired
	private ApplicationContext applicationContext;
	
	@Bean
	public Repositories repositories() {
		return new Repositories(applicationContext);
	}
	
	@Bean
	public RepositoryService repositoryService() {
		return new RepositoryService(repositories());
	}
	
}
