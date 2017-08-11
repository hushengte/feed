package com.disciples.feed.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.mapping.context.PersistentEntities;
import org.springframework.data.repository.support.Repositories;
import org.springframework.http.converter.json.Jackson2ObjectMapperFactoryBean;

import com.disciples.feed.json.HibernateProxyModule;
import com.disciples.feed.manage.ManageService;
import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
public class ManageConfiguration {
	
	@Autowired
	private ApplicationContext applicationContext;
	
	@Bean
	public PersistentEntities persistentEntities() {
		List<MappingContext<?, ?>> mappingContexts = new ArrayList<MappingContext<?, ?>>();
		for (MappingContext<?, ?> context : BeanFactoryUtils.beansOfTypeIncludingAncestors(applicationContext, MappingContext.class).values()) {
			mappingContexts.add(context);
		}
		return new PersistentEntities(mappingContexts);
	}
	
	@Bean
	public Repositories repositories() {
		return new Repositories(applicationContext);
	}
	
	@Bean
	public ManageService manageService() {
		return new ManageService(persistentEntities(), repositories());
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
