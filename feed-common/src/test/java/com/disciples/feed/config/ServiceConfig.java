package com.disciples.feed.config;

import java.io.IOException;

import javax.sql.DataSource;

import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.http.converter.json.Jackson2ObjectMapperFactoryBean;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.disciples.feed.domain.Book;
import com.disciples.feed.repository.DefaultJpaRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
@ComponentScan("com.disciples.feed.service")
@EnableJpaRepositories(basePackages = "com.disciples.feed.dao", repositoryBaseClass = DefaultJpaRepository.class)
@EnableTransactionManagement(proxyTargetClass = true)
public class ServiceConfig {
	
    @Bean
	public DataSource dataSource() {
    	return new EmbeddedDatabaseBuilder().setType(EmbeddedDatabaseType.H2).build();
	}
    
    @Bean
	public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
		LocalContainerEntityManagerFactoryBean factoryBean = new LocalContainerEntityManagerFactoryBean();
		factoryBean.setDataSource(dataSource());
		factoryBean.setPackagesToScan(Book.class.getPackage().getName());
		factoryBean.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
		try {
			PropertiesFactoryBean appPropertiesFactory = new PropertiesFactoryBean();
			appPropertiesFactory.setLocation(new ClassPathResource("application.properties"));
			appPropertiesFactory.afterPropertiesSet();
			factoryBean.setJpaProperties(appPropertiesFactory.getObject());
			return factoryBean;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Bean
    public PlatformTransactionManager transactionManager() {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory().getObject());
        return transactionManager;
    }
	
	@Bean
	public ObjectMapper objectMapper() {
		Jackson2ObjectMapperFactoryBean factory = new Jackson2ObjectMapperFactoryBean();
		factory.setSimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		factory.afterPropertiesSet();
		return factory.getObject();
	}
	
}
