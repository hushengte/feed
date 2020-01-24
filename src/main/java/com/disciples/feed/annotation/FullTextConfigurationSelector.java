package com.disciples.feed.annotation;

import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.ClassUtils;

/**
 * Selects which implementation of {@link AbstractFullTextConfiguration} should be used
 * based on the value of {@link EnableFullText} on the importing {@code @Configuration} class.
 *
 * <p>Detects the presence of hibernate-search or solr accordingly, to enables full text service.
 * @author Ted Smith
 * @see EnableFullText
 */
public class FullTextConfigurationSelector implements ImportSelector {
	
	private static final boolean hibernateSearchPresent = ClassUtils.isPresent(
			"org.hibernate.search.spi.SearchIntegrator", FullTextConfigurationSelector.class.getClassLoader());
	
	private static final boolean solrPresent = ClassUtils.isPresent(
			"org.apache.solr.client.solrj.SolrClient", FullTextConfigurationSelector.class.getClassLoader());
	
	private static final boolean springDataSolrPresent = ClassUtils.isPresent(
			"org.springframework.data.solr.core.SolrTemplate", FullTextConfigurationSelector.class.getClassLoader());
	
	
	@Override
	public String[] selectImports(AnnotationMetadata importMetadata) {
		if (hibernateSearchPresent) {
			return new String[] {HibernateSearchServiceConfiguration.class.getName()};
		}
		if (solrPresent && springDataSolrPresent) {
			return new String[] {SolrServiceConfiguration.class.getName()};
		}
		throw new IllegalArgumentException("Please add hibernate-search-orm dependencies or solr dependencies to classpath");
	}

}
