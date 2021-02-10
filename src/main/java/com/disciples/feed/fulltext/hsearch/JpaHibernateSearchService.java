package com.disciples.feed.fulltext.hsearch;

import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.ManyToOne;

import org.hibernate.Criteria;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.MySQLDialect;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.event.spi.EventSource;
import org.hibernate.internal.CriteriaImpl;
import org.hibernate.query.Query;
import org.hibernate.search.backend.spi.Work;
import org.hibernate.search.backend.spi.WorkType;
import org.hibernate.search.backend.spi.Worker;
import org.hibernate.search.event.impl.EventSourceTransactionContext;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.Search;
import org.hibernate.search.query.engine.spi.HSQuery;
import org.hibernate.search.spi.SearchIntegrator;
import org.hibernate.sql.JoinType;
import org.hibernate.transform.AliasToBeanResultTransformer;
import org.springframework.beans.BeanUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.ReflectionUtils.FieldCallback;

import com.disciples.feed.fulltext.FullTextQuery;

public class JpaHibernateSearchService extends AbstractHibernateSearchService {

    private EntityManagerFactory entityManagerFactory;
    
    public JpaHibernateSearchService(EntityManagerFactory entityManagerFactory, 
            SearchIntegrator searchIntegrator) {
        super(searchIntegrator);
        Assert.notNull(entityManagerFactory, "EntityManagerFactory is required.");
        this.entityManagerFactory = entityManagerFactory;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected <T> List<T> fetchEntityList(HSQuery hsQuery, FullTextQuery<T> ftQuery) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        try {
            Class<?> docClass = ftQuery.getDocClass();
            Pageable pageable = ftQuery.getPageable();
            FullTextEntityManager fullTextEm = Search.getFullTextEntityManager(entityManager);
            org.hibernate.search.jpa.FullTextQuery fullTextQuery = fullTextEm.createFullTextQuery(
                    hsQuery.getLuceneQuery(), docClass);
            // pagination
            if (pageable.isPaged()) {
                fullTextQuery.setFirstResult((int)pageable.getOffset()).setMaxResults(pageable.getPageSize());
            } else {
                fullTextQuery.setFirstResult(0).setMaxResults(ftQuery.getMaxResults());
            }
            Set<String> projections = ftQuery.getProjections();
            if (CollectionUtils.isEmpty(projections)) { 
                // fetch from database
                Set<String> associations = ftQuery.getAssociations();
                if (!CollectionUtils.isEmpty(associations)) {
                    SharedSessionContractImplementor session = (SharedSessionContractImplementor)fullTextEm.getDelegate();
                    Criteria rootCriteria = new CriteriaImpl(docClass.getName(), session);
                    for (String association : associations) {
                        rootCriteria.createCriteria(association, JoinType.LEFT_OUTER_JOIN);
                    }
                    fullTextQuery.setCriteriaQuery(rootCriteria);
                }
            } else { 
                // fetch from lucene index data
                fullTextQuery.setProjection(projections.toArray(new String[projections.size()]));
                fullTextQuery.setResultTransformer(new AliasToBeanResultTransformer(docClass));
            }
            return fullTextQuery.getResultList();
        } finally {
            entityManager.close();
        }
    }
    
    @Override
    protected <T> void index(Class<T> docClass, Method getIdMethod) {
        AssociationFieldCallback callback = new AssociationFieldCallback(docClass);
        ReflectionUtils.doWithFields(docClass, callback);
        String jpql = callback.getQuery();
        
        EntityManager em = entityManagerFactory.createEntityManager();
        SessionFactoryImplementor sessionFactory = entityManagerFactory.unwrap(SessionFactoryImplementor.class);
        try {
            EventSource eventSource = (EventSource)em.getDelegate();
            EventSourceTransactionContext transactionContext = new EventSourceTransactionContext(eventSource);
            
            Dialect dialect = sessionFactory.getJdbcServices().getDialect();
            Query<T> query = createQuery(em, dialect, jpql, docClass);
            
            Worker worker = getSearchIntegrator().getWorker();
            query.getResultStream().forEach(entity -> {
                Serializable id = (Serializable) ReflectionUtils.invokeMethod(getIdMethod, entity);
                Work work = new Work(entity, id, WorkType.INDEX);
                worker.performWork(work, transactionContext);
            });
            worker.flushWorks(transactionContext);
        } finally {
            em.close();
        }
    }
    
    protected <T> Query<T> createQuery(EntityManager em, Dialect dialect, String jpql, Class<T> docClass) {
        Query<T> query = (Query<T>)em.createQuery(jpql, docClass);
        if (dialect instanceof MySQLDialect) {
            //enable mysql client-side stream
            query.setFetchSize(Integer.MIN_VALUE);
        }
        return query;
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
