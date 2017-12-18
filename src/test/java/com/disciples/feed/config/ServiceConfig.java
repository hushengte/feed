package com.disciples.feed.config;

import java.io.IOException;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.disciples.feed.domain.Book;
import com.disciples.feed.fulltext.FullTextService;
import com.disciples.feed.fulltext.HibernateSearchService;
import com.disciples.feed.repository.DefaultJpaRepository;
import com.zaxxer.hikari.HikariDataSource;

@Configuration
@PropertySource("classpath:application.properties")
@ComponentScan("com.disciples.feed.service")
@EnableJpaRepositories(basePackages = "com.disciples.feed.dao", repositoryBaseClass = DefaultJpaRepository.class)
@EnableTransactionManagement(proxyTargetClass = true)
public class ServiceConfig {
	
//    @Bean
//	public DataSource dataSource() {
//    	return new EmbeddedDatabaseBuilder().setType(EmbeddedDatabaseType.H2).build();
//	}
    
    @Autowired
	private Environment env;

    @Bean
	public DataSource dataSource() {
		HikariDataSource dataSource = new HikariDataSource();
		dataSource.setDriverClassName(env.getProperty("jdbc.driverClassName"));
		dataSource.setJdbcUrl(env.getProperty("jdbc.url"));
		dataSource.setUsername(env.getProperty("jdbc.username"));
		dataSource.setPassword(env.getProperty("jdbc.password"));
		dataSource.setMinimumIdle(env.getProperty("jdbc.pool.minSize", Integer.class));
		dataSource.setMaximumPoolSize(env.getProperty("jdbc.pool.maxSize", Integer.class));
		return dataSource;
	}
    
    @Bean
	public EntityManagerFactory entityManagerFactory() {
		LocalContainerEntityManagerFactoryBean factoryBean = new LocalContainerEntityManagerFactoryBean();
		factoryBean.setDataSource(dataSource());
		factoryBean.setPackagesToScan(Book.class.getPackage().getName());
		factoryBean.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
		try {
			PropertiesFactoryBean appPropertiesFactory = new PropertiesFactoryBean();
			appPropertiesFactory.setLocation(new ClassPathResource("application.properties"));
			appPropertiesFactory.afterPropertiesSet();
			factoryBean.setJpaProperties(appPropertiesFactory.getObject());
			factoryBean.afterPropertiesSet();
			return factoryBean.getObject();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Bean
    public PlatformTransactionManager transactionManager() {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory());
        return transactionManager;
    }
	
	@Bean
	public FullTextService fullTextService() {
		return new HibernateSearchService(entityManagerFactory());
	}
	
}
