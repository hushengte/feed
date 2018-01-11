package com.disciples.feed.config;

import org.apache.lucene.analysis.Analyzer;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.data.solr.SolrRepositoriesAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.disciples.feed.annotation.FullTextConfigurationSelector;

@Configuration
@ConditionalOnClass({Analyzer.class})
@AutoConfigureAfter({JpaRepositoriesAutoConfiguration.class, SolrRepositoriesAutoConfiguration.class})
@Import(FullTextConfigurationSelector.class)
public class FullTextAutoConfiguration {

}
