package com.disciples.feed.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.repository.support.Repositories;
import org.springframework.http.converter.json.Jackson2ObjectMapperFactoryBean;

import com.disciples.feed.json.HibernateProxyModule;
import com.disciples.feed.rest.RepositoryService;
import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
public class RepositoryRestConfiguration {
	
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
	
	@Bean
	public ObjectMapper objectMapper() {
		Jackson2ObjectMapperFactoryBean factory = new Jackson2ObjectMapperFactoryBean();
		factory.setSimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		factory.afterPropertiesSet();
		ObjectMapper mapper = factory.getObject();
		mapper.registerModule(new HibernateProxyModule());
		return mapper;
	}
	
}
