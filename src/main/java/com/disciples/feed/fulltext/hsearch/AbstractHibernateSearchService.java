package com.disciples.feed.fulltext.hsearch;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.transaction.Synchronization;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.highlight.Formatter;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.backend.TransactionContext;
import org.hibernate.search.backend.spi.Work;
import org.hibernate.search.backend.spi.WorkType;
import org.hibernate.search.backend.spi.Worker;
import org.hibernate.search.engine.integration.impl.ExtendedSearchIntegrator;
import org.hibernate.search.exception.SearchException;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.hibernate.search.query.dsl.TermMatchingContext;
import org.hibernate.search.query.engine.spi.HSQuery;
import org.hibernate.search.spi.impl.PojoIndexedTypeIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import com.disciples.feed.fulltext.FullTextQuery;
import com.disciples.feed.fulltext.FullTextService;

public abstract class AbstractHibernateSearchService implements FullTextService {

	private static final Logger LOG = LoggerFactory.getLogger(AbstractHibernateSearchService.class);
    
    private static final float MAX_BOOST = 2.0f;
    private static final int DEFAULT_BATCH_SIZE = 1000;
    
    private ExtendedSearchIntegrator extendedIntegrator;
    private Formatter formatter;
    private int batchSize = DEFAULT_BATCH_SIZE;
    
    public AbstractHibernateSearchService(ExtendedSearchIntegrator extendedIntegrator) {
    	this(extendedIntegrator, new SimpleHTMLFormatter("<b><font color='red'>", "</font></b>"));
    }
    
    public AbstractHibernateSearchService(ExtendedSearchIntegrator extendedIntegrator, Formatter formatter) {
        Assert.notNull(extendedIntegrator, "ExtendedSearchIntegrator is required.");
        this.extendedIntegrator = extendedIntegrator;
        setFormatter(formatter);
    }

    public Formatter getFormatter() {
        return formatter;
    }

    public void setFormatter(Formatter formatter) {
        Assert.notNull(formatter, "Lucene Highlight Formatter must not be null.");
        this.formatter = formatter;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        Assert.isTrue(batchSize > 0, "batchSize must be greater than 0");
        this.batchSize = batchSize;
    }

    protected TermMatchingContext buildTermMatchContext(QueryBuilder qb, List<String> fields) {
    	TermMatchingContext termMatchContext = qb.keyword().onField(fields.get(0)).boostedTo(MAX_BOOST);
        for (int i = 1; i < fields.size(); i++) {
        	termMatchContext = termMatchContext.andField(fields.get(i)).boostedTo(MAX_BOOST - (i * 1.0f) / fields.size());
        }
        return termMatchContext;
    }
    
    @Override
	public <T> Page<T> query(FullTextQuery<T> query) {
    	Assert.notNull(query, "FullTextQuery must not be null.");

    	Class<T> docClass = query.getDocClass();
    	String keyword = query.getKeyword();
    	List<String> fields = query.getFields();
    	Pageable pageable = query.getPageable();
    	if (!StringUtils.hasText(keyword) || CollectionUtils.isEmpty(fields)) {
    		return new PageImpl<>(Collections.<T>emptyList());
    	}
    	QueryBuilder qb = extendedIntegrator.buildQueryBuilder().forEntity(docClass).get();
        // create query
        TermMatchingContext tmc = buildTermMatchContext(qb, fields);
        Query localQuery = tmc.matching(keyword).createQuery();
        HSQuery hsQuery = extendedIntegrator.createHSQuery(localQuery, docClass);
        //TODO: timeoutExceptionFactory
        
        // pagination
        if (pageable.isPaged()) {
            hsQuery.firstResult((int)pageable.getOffset()).maxResults(pageable.getPageSize());
        } else {
            hsQuery.firstResult(0).maxResults(query.getMaxResults());
        }
        // query total count
        int total = hsQuery.queryResultSize();
        if (total == 0) {
            return new PageImpl<>(Collections.<T>emptyList());
        }
        Set<String> projections = query.getProjections();
        if (!CollectionUtils.isEmpty(projections)) {
            hsQuery.projection(projections.toArray(new String[projections.size()]));
        }
        // fetch data
        hsQuery.getTimeoutManager().start();
        List<T> content = fetchEntityList(hsQuery, query);
        hsQuery.getTimeoutManager().stop();
        
        if (query.isHighlight()) {
            Analyzer analyzer = extendedIntegrator.getAnalyzer(new PojoIndexedTypeIdentifier(docClass));
            Highlighter highlighter = new Highlighter(formatter, new QueryScorer(localQuery));
            doHighlight(docClass, analyzer, highlighter, content, fields);
        }
        return new PageImpl<T>(content, pageable, total);
	}
    
    protected abstract <T> List<T> fetchEntityList(HSQuery hsQuery, FullTextQuery<T> ftQuery);
    
    private void doHighlight(Class<?> docClass, Analyzer analyzer, Highlighter highlighter, List<?> content, List<String> fields) {
    	for (Object data : content) {
			BeanWrapper dataBw = PropertyAccessorFactory.forBeanPropertyAccess(data);
			for (String fieldName : fields) {
				if (fieldName.indexOf(".") == -1) {
					Field field = ReflectionUtils.findField(docClass, fieldName);
					if (field != null && field.getType() == String.class) {
						String fieldValue = (String)dataBw.getPropertyValue(fieldName);
						if (StringUtils.hasText(fieldValue)) {
							try {
								String[] frags = highlighter.getBestFragments(analyzer, fieldName, fieldValue, fieldValue.length());
								StringBuilder fragsb = new StringBuilder();
								for (String frag : frags) {
									fragsb.append(frag);
								}
								String highlightedValue = fragsb.toString();
					    		if (StringUtils.hasText(highlightedValue)) {
					    			dataBw.setPropertyValue(fieldName, highlightedValue);
					    		}
					    	} catch (Exception e) {
								LOG.warn(e.getMessage(), e);
							}
						}
					}
				} else {
					String[] parts = fieldName.split("\\.");
					Field field = ReflectionUtils.findField(docClass, parts[0]);
					Object fieldValue = dataBw.getPropertyValue(parts[0]);
					BeanWrapper fieldDataBw = dataBw;
					for (int i = 1; i < parts.length; i++) {
						if (field != null && fieldValue != null) {
							field = ReflectionUtils.findField(field.getType(), parts[i]);
							fieldDataBw = PropertyAccessorFactory.forBeanPropertyAccess(fieldValue);
							fieldValue = fieldDataBw.getPropertyValue(parts[i]);
						}
					}
					if (field != null && fieldValue != null && fieldValue instanceof String) {
						String value = (String)fieldValue;
						try {
							String[] frags = highlighter.getBestFragments(analyzer, fieldName, value, value.length());
							StringBuilder fragsb = new StringBuilder();
							for (String frag : frags) {
								fragsb.append(frag);
							}
							String highlightedValue = fragsb.toString();
				    		if (StringUtils.hasText(highlightedValue)) {
				    			fieldDataBw.setPropertyValue(parts[parts.length - 1], highlightedValue);
				    		}
				    	} catch (Exception e) {
							LOG.warn(e.getMessage(), e);
						}
					}
				}
			}
		}
	}

    @Override
    public void reindex(Class<?> ... docClasses) {
    	Assert.notNull(docClasses, "Document classes must not be null");
    	for (final Class<?> docClass : docClasses) {
            if (docClass != null && AnnotationUtils.findAnnotation(docClass, Indexed.class) != null) {
                index(docClass);
            }
        }
	}
    
    protected abstract long getTotalCount(Class<?> docClass);
    
    protected abstract <T> List<T> getEntityList(Class<T> docClass, Pageable pageable);
    
    protected <T> void index(Class<T> docClass) {
        Method getIdMethod = ReflectionUtils.findMethod(docClass, "getId");
        if (getIdMethod == null) {
            throw new SearchException("Document Class must have getId method.");
        }
        
    	long total = getTotalCount(docClass);
		long batchCount = (total % batchSize == 0 ? (total / batchSize) : (total / batchSize) + 1);
		for (int i = 0; i < batchCount; i++) {
		    Pageable pageable = PageRequest.of(i, batchSize);
		    List<T> entities = getEntityList(docClass, pageable);
		    batchIndex(getIdMethod, entities, EmptyTransactionContext.INSTANCE);
		}
    }
    
    protected <T> void batchIndex(Method getIdMethod, List<T> entities, TransactionContext txContext) {
        Worker worker = extendedIntegrator.getWorker();
        for (T entity : entities) {
            Serializable id = (Serializable) ReflectionUtils.invokeMethod(getIdMethod, entity);
            Work work = new Work(entity, id, WorkType.INDEX);
            worker.performWork(work, txContext);
        }
        worker.flushWorks(txContext);
    }
    
    public void shutdown() {
        extendedIntegrator.close();
    }
    
    static enum EmptyTransactionContext implements TransactionContext {
        INSTANCE;
        
        @Override
        public boolean isTransactionInProgress() {
            return true;
        }

        @Override
        public Object getTransactionIdentifier() {
            return this;
        }

        @Override
        public void registerSynchronization(Synchronization synchronization) {
        }
        
    }
    
}
