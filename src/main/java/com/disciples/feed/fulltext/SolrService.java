package com.disciples.feed.fulltext;

import java.util.List;
import java.util.Set;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.Criteria;
import org.springframework.data.solr.core.query.HighlightOptions;
import org.springframework.data.solr.core.query.SimpleHighlightQuery;
import org.springframework.data.solr.core.query.result.HighlightEntry;
import org.springframework.data.solr.core.query.result.HighlightEntry.Highlight;
import org.springframework.data.solr.core.query.result.HighlightPage;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

public class SolrService implements FullTextService {
	
	private static final float MAX_BOOST = 2.0f;
	private static final String HILIGHT_PREFIX = "<b><font color='red'>";
	private static final String HILIGHT_POSTFIX = "</font></b>";
	
	private SolrTemplate solrTemplate;
	
	public SolrService(SolrTemplate solrTemplate) {
		Assert.notNull(solrTemplate, "solrTemplate must not be null.");
		this.solrTemplate = solrTemplate;
	}
	
	private Criteria buildCriteria(List<String> fields, String[] words) {
		String firstField = fields.get(0);
    	Criteria criteria = Criteria.where(firstField).is(words[0]).boost(MAX_BOOST);
    	for (int i = 1; i < words.length; i++) {
    		criteria = criteria.or(firstField).is(words[i]).boost(MAX_BOOST);
    	}
    	for (int i = 1; i < fields.size(); i++) {
    		String field = fields.get(i);
    		float boost = MAX_BOOST - (i * 1.0f) / fields.size();
    		for (int j = 0; j < words.length; j++) {
    			criteria = criteria.or(field).is(words[j]).boost(boost);
        	}
    	}
    	return criteria;
    }

	@Override
	public <T> Page<T> query(FullTextQuery<T> ftQuery) {
		Class<T> docClass = ftQuery.getDocClass();
		String keyword = ftQuery.getKeyword();
		List<String> fields = ftQuery.getFields();
		SimpleHighlightQuery query = new SimpleHighlightQuery(buildCriteria(fields, keyword.split("\\s")));
		Pageable pageable = ftQuery.getPageable();
		if (pageable != null) {
			query.setPageRequest(pageable);
		} else {
			query.setOffset(0L).setRows(ftQuery.getMaxResults());
		}
		
		Set<String> projections = ftQuery.getProjections();
		if (!CollectionUtils.isEmpty(projections)) {
			query.addProjectionOnFields(projections.toArray(new String[projections.size()]));
		}
		if (ftQuery.isHighlight()) {
			HighlightOptions options = new HighlightOptions();
			options.setSimplePrefix(HILIGHT_PREFIX).setSimplePostfix(HILIGHT_POSTFIX).addField(fields.toArray(new String[fields.size()]));
			query.setHighlightOptions(options);
		}
		//FIXME: collection name
		HighlightPage<T> page = solrTemplate.queryForHighlightPage("", query, docClass);
    	for (HighlightEntry<T> entry : page.getHighlighted()) {
    		BeanWrapper bw = PropertyAccessorFactory.forBeanPropertyAccess(entry.getEntity());
    		List<Highlight> highlights = entry.getHighlights();
    		for (Highlight highlight : highlights) {
    			String fieldName = highlight.getField().getName();
    			List<String> snipplets = highlight.getSnipplets();
    			if (snipplets != null && snipplets.size() > 0) {
    				bw.setPropertyValue(fieldName, snipplets.get(0));
    			}
    		}
    	}
		return page;
	}

	@Override
	public void reindex(Class<?>... docClasses) {
		throw new UnsupportedOperationException("Not implemented");
	}

}
