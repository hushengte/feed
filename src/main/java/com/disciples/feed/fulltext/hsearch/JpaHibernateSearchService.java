package com.disciples.feed.fulltext.hsearch;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.FlushModeType;
import javax.persistence.ManyToOne;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.event.spi.EventSource;
import org.hibernate.search.backend.TransactionContext;
import org.hibernate.search.engine.integration.impl.ExtendedSearchIntegrator;
import org.hibernate.search.event.impl.EventSourceTransactionContext;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.Search;
import org.hibernate.search.query.engine.spi.HSQuery;
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
            ExtendedSearchIntegrator extendedIntegrator) {
        super(extendedIntegrator);
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
                    Session session = (Session)fullTextEm.getDelegate();
                    @SuppressWarnings("deprecation")
                    Criteria rootCriteria = session.createCriteria(docClass);
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
    protected long getTotalCount(Class<?> docClass) {
        String jpql = String.format("select count(*) from %s o", docClass.getSimpleName());
        EntityManager em = entityManagerFactory.createEntityManager();
        try {
            return (Long)em.createQuery(jpql).getSingleResult();
        } finally {
            em.close();
        }
    }

    @Override
    protected <T> List<T> getEntityList(Class<T> docClass, Pageable pageable) {
        AssociationFieldCallback callback = new AssociationFieldCallback(docClass);
        ReflectionUtils.doWithFields(docClass, callback);
        String jpql = callback.getQuery();
        EntityManager em = entityManagerFactory.createEntityManager();
        try {
            return em.createQuery(jpql, docClass)
                    .setFirstResult((int)pageable.getOffset())
                    .setMaxResults(pageable.getPageSize())
                    .getResultList();
        } finally {
            em.close();
        }
    }
    
    @Override
    protected <T> void batchIndex(Method getIdMethod, List<T> entities, TransactionContext txContext) {
        EntityManager em = entityManagerFactory.createEntityManager();
        em.setFlushMode(FlushModeType.COMMIT);
        EventSource eventSource = (EventSource)em.getDelegate();
        EventSourceTransactionContext transactionContext = new EventSourceTransactionContext(eventSource);
        try {
            super.batchIndex(getIdMethod, entities, transactionContext);
            em.clear();
        } finally {
            em.close();
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
