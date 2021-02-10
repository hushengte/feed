package com.disciples.feed.fulltext.hsearch;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

import org.hibernate.search.cfg.spi.SearchConfiguration;
import org.hibernate.search.spi.SearchIntegrator;
import org.hibernate.search.spi.SearchIntegratorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.util.CollectionUtils;

/**
 * Beans that must be registered by using hibernate-search with jdbc.
 */
@Configuration(proxyBeanMethods = false)
public abstract class AbstractJdbcFullTextConfiguration {
    
    public static final String PROPERTIES_FILE_CLASSPATH = "/hibernate-search.properties";
    
    protected String getPropertiesClasspath() {
        return PROPERTIES_FILE_CLASSPATH;
    }
    
    /**
     * Override this method to provide document class list.
     * 
     * @param props Properties have been loaded.
     * @return Document class list
     */
    protected abstract List<Class<?>> getDocumentClasses(Properties props);
    
    @Bean
    public SearchConfiguration hibernateSearchConfiguration() throws IOException {
        ClassPathResource resource = new ClassPathResource(getPropertiesClasspath());
        Properties props = PropertiesLoaderUtils.loadProperties(resource);
        
        List<Class<?>> docClasses = getDocumentClasses(props);
        if (CollectionUtils.isEmpty(docClasses)) {
            throw new IllegalStateException("Please provide document class list.");
        }
        return new SimpleSearchConfiguration(props, docClasses);
    }
    
    @Bean
    public SearchIntegrator searchIntegrator(SearchConfiguration searchConfig) {
        SearchIntegrator searchIntegrator = new SearchIntegratorBuilder()
                .configuration(searchConfig)
                .buildSearchIntegrator();
        return searchIntegrator;
    }

    @Bean
    public FullTextRepositoryEventListener fullTextRepositoryEventListener(SearchIntegrator searchIntegrator) {
        return new FullTextRepositoryEventListener(searchIntegrator);
    }
    
}
