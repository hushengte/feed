package com.disciples.feed.fulltext;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.FlushModeType;
import javax.persistence.ManyToOne;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.Search;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.hibernate.search.query.dsl.TermMatchingContext;
import org.hibernate.sql.JoinType;
import org.hibernate.transform.AliasToBeanResultTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.ReflectionUtils.FieldCallback;
import org.springframework.util.StringUtils;
import org.wltea.analyzer.lucene.IKAnalyzer;

public class HibernateSearchService implements FullTextService {

	private static final Logger LOG = LoggerFactory.getLogger(HibernateSearchService.class);
    
    private static final float MAX_BOOST = 2.0f;
    private static final int BATCH_SIZE = 1000;
    
    private EntityManagerFactory entityManagerFactory;
    
    private Analyzer analyzer = new IKAnalyzer();
    private SimpleHTMLFormatter formatter = new SimpleHTMLFormatter("<b><font color='red'>", "</font></b>");
    
    public HibernateSearchService(EntityManagerFactory entityManagerFactory) {
    	Assert.notNull(entityManagerFactory, "entityManagerFactory must not be null.");
    	this.entityManagerFactory = entityManagerFactory;
    }

    private TermMatchingContext buildTermMatchContext(QueryBuilder qb, List<String> fields) {
    	TermMatchingContext termMatchContext = qb.keyword().onField(fields.get(0)).boostedTo(MAX_BOOST);
        for (int i = 1; i < fields.size(); i++) {
        	termMatchContext = termMatchContext.andField(fields.get(i)).boostedTo(MAX_BOOST - (i * 1.0f) / fields.size());
        }
        return termMatchContext;
    }
    
    @Override
    @SuppressWarnings("unchecked")
	public <T> Page<T> query(Class<T> docClass, FullTextQuery query) {
    	Assert.notNull(docClass, "docClass must not be null.");
    	Assert.notNull(query, "query must not be null.");
    	Assert.isTrue(docClass == query.getDocClass(), "docClass must be same as docClass in query.");
    	
    	String keyword = query.getKeyword();
    	List<String> fields = query.getFields();
    	if (!StringUtils.hasText(keyword) || CollectionUtils.isEmpty(fields)) {
    		return new PageImpl<T>(Collections.<T>emptyList(), null, 0);
    	}
    	EntityManager entityManager = entityManagerFactory.createEntityManager();
    	try {
    		FullTextEntityManager fullTextEm = Search.getFullTextEntityManager(entityManager);
    		QueryBuilder qb = fullTextEm.getSearchFactory().buildQueryBuilder().forEntity(docClass).get();
    		//创建查询
    		Query localQuery = buildTermMatchContext(qb, fields).matching(keyword).createQuery();
    		org.hibernate.search.jpa.FullTextQuery fullTextQuery = fullTextEm.createFullTextQuery(localQuery, docClass);
    		//设置分页
    		Pageable pageable = query.getPageable();
    		if (pageable != null) {
    			fullTextQuery.setFirstResult(pageable.getOffset()).setMaxResults(pageable.getPageSize());
    		} else {
    			fullTextQuery.setFirstResult(0).setMaxResults(query.getMaxResults());
    		}
    		//查询总数
    		int total = fullTextQuery.getResultSize();
            if (total == 0) {
            	return new PageImpl<T>(Collections.<T>emptyList(), null, 0);
            }
    		//抓取数据
            List<String> projections = query.getProjections();
            if (CollectionUtils.isEmpty(projections)) { //从数据库抓取
            	List<String> associations = query.getAssociations();
            	if (!CollectionUtils.isEmpty(associations)) {
            		Session session = (Session)fullTextEm.getDelegate();
                	Criteria rootCriteria = session.createCriteria(docClass);
                	for (String association : associations) {
                		rootCriteria.createCriteria(association, JoinType.LEFT_OUTER_JOIN);
                	}
                	fullTextQuery.setCriteriaQuery(rootCriteria);
            	}
            } else { //从索引中抓取
            	fullTextQuery.setProjection(projections.toArray(new String[projections.size()]));
                fullTextQuery.setResultTransformer(new AliasToBeanResultTransformer(docClass));
            }
			List<T> content = (List<T>)fullTextQuery.getResultList();
    		if (query.isHighlight()) {
    			Highlighter highlighter = new Highlighter(formatter, new QueryScorer(localQuery));
    			doHighlight(docClass, highlighter, content, fields);
    		}
    		return new PageImpl<T>(content, null, total);
    	} finally {
    		entityManager.close();
    	}
	}
    
    private void doHighlight(Class<?> docClass, Highlighter highlighter, List<?> content, List<String> fields) {
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
    	EntityManager em = entityManagerFactory.createEntityManager();
		try {
			FullTextEntityManager fullTextEm = Search.getFullTextEntityManager(em);
			fullTextEm.setFlushMode(FlushModeType.COMMIT);
			
			for (final Class<?> docClass : docClasses) {
				if (docClass != null && AnnotationUtils.findAnnotation(docClass, Indexed.class) != null) {
					AssociationFieldCallback callback = new AssociationFieldCallback(docClass);
					ReflectionUtils.doWithFields(docClass, callback);
					index(fullTextEm, docClass, callback.getQuery());
				}
			}
		} finally {
			em.close();
		}
	}
    
    private <T> void index(FullTextEntityManager fullTextEm, Class<T> docClass, String fetchJpql) {
    	int count = ((Long)fullTextEm.createQuery(String.format("select count(*) from %s o", docClass.getSimpleName())).getSingleResult()).intValue();
		int batchCount = (count % BATCH_SIZE == 0 ? (count / BATCH_SIZE) : (count / BATCH_SIZE) + 1);
		for (int i = 0; i < batchCount; i++) {
			try {
				List<T> dataList = fullTextEm.createQuery(fetchJpql, docClass).setFirstResult(i * BATCH_SIZE).setMaxResults(BATCH_SIZE).getResultList();
				for (T data : dataList) {
					fullTextEm.index(data);
				}
				fullTextEm.flushToIndexes();
				fullTextEm.clear();
			} catch (RuntimeException e) {
				LOG.error(e.getMessage(), e);
			}
		}
    }
    
    private static class AssociationFieldCallback implements FieldCallback {
    	private final Class<?> docClass;
    	private final StringBuilder query;
    	
    	public AssociationFieldCallback(Class<?> docClass) {
    		this.docClass = docClass;
    		this.query = new StringBuilder(String.format("select o from %s o", docClass.getSimpleName()));
    	}
    	
    	@Override
		public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
    		if (!Modifier.isStatic(field.getModifiers())) {
    			PropertyDescriptor fieldPd = BeanUtils.getPropertyDescriptor(docClass, field.getName());
    			if (fieldPd != null && (AnnotationUtils.findAnnotation(field, ManyToOne.class) != null ||
    					AnnotationUtils.findAnnotation(fieldPd.getReadMethod(), ManyToOne.class) != null ||
    					AnnotationUtils.findAnnotation(fieldPd.getWriteMethod(), ManyToOne.class) != null)) {
    				query.append(" left join fetch o.").append(field.getName());
    			}
    		}
		}
    	
		public String getQuery() {
			return query.toString();
		}
    }

}
