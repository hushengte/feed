package com.disciples.feed.config;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.repository.support.Repositories;

import com.disciples.feed.rest.RepositoryService;

@Configuration(proxyBeanMethods = false)
public class RepositoryConfiguration {

	@Bean
	public Repositories repositories(ApplicationContext applicationContext) {
		return new Repositories(applicationContext);
	}
	
	@Bean
	public RepositoryService repositoryService(Repositories repositories) {
		return new RepositoryService(repositories);
	}
	
}
