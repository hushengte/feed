package com.disciples.feed.config;

import java.io.IOException;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.disciples.feed.domain.Book;
import com.disciples.feed.repository.DefaultJpaRepository;

@Configuration
@Import(DataSourceConfig.class)
@EnableJpaRepositories(basePackages = "com.disciples.feed.dao", repositoryBaseClass = DefaultJpaRepository.class)
@EnableTransactionManagement(proxyTargetClass = true)
public class HibernateConfig {
    
    @Autowired
    private DataSource dataSource;
	
    @Bean
	public EntityManagerFactory entityManagerFactory() {
		LocalContainerEntityManagerFactoryBean factoryBean = new LocalContainerEntityManagerFactoryBean();
		factoryBean.setDataSource(dataSource);
		factoryBean.setPackagesToScan(Book.class.getPackage().getName());
		factoryBean.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
		try {
			PropertiesFactoryBean appPropertiesFactory = new PropertiesFactoryBean();
			appPropertiesFactory.setLocation(new ClassPathResource("/hibernate.properties"));
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
	
}
