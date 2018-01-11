package com.disciples.feed.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.repository.Repository;

import com.disciples.feed.json.HibernateProxyModule;
import com.disciples.feed.web.RepositoryRestController;

@Configuration
@ConditionalOnClass({Repository.class})
@Import({RepositoryConfiguration.class})
public class RepositoryAutoConfiguration {

	@Bean
	@ConditionalOnWebApplication
	@ConditionalOnMissingBean
	public RepositoryRestController repositoryRestController() {
		return new RepositoryRestController();
	}
	
	@Bean
	@ConditionalOnMissingBean
	public HibernateProxyModule hibernateProxyModule() {
		return new HibernateProxyModule();
	}
	
}
