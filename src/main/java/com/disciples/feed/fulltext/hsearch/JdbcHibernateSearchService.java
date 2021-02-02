package com.disciples.feed.fulltext.hsearch;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.persistence.Table;

import org.hibernate.search.backend.spi.Work;
import org.hibernate.search.backend.spi.WorkType;
import org.hibernate.search.backend.spi.Worker;
import org.hibernate.search.engine.integration.impl.ExtendedSearchIntegrator;
import org.hibernate.search.exception.SearchException;
import org.hibernate.search.query.engine.spi.EntityInfo;
import org.hibernate.search.query.engine.spi.HSQuery;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import com.disciples.feed.fulltext.FullTextQuery;

public class JdbcHibernateSearchService extends AbstractHibernateSearchService {
    
    private JdbcOperations jdbcOperations;

    public JdbcHibernateSearchService(JdbcOperations jdbcOperations, ExtendedSearchIntegrator extendedIntegrator) {
        super(extendedIntegrator);
        Assert.notNull(jdbcOperations, "JdbcOperations is required.");
        this.jdbcOperations = jdbcOperations;
    }

    @Override
    protected <T> List<T> fetchEntityList(HSQuery hsQuery, FullTextQuery<T> ftQuery) {
        List<EntityInfo> entityInfos = hsQuery.queryEntityInfos();
        if (CollectionUtils.isEmpty(entityInfos)) {
            return Collections.emptyList();
        }
        Class<T> docClass = ftQuery.getDocClass();
        String[] projectedFields = hsQuery.getProjectedFields();
        if (projectedFields != null && projectedFields.length > 0) {
            List<T> results = new ArrayList<>();
            EntityInfoToBeanConverter<T> converter = new EntityInfoToBeanConverter<>(docClass, projectedFields);
            for (EntityInfo entityInfo : entityInfos) {
                results.add(converter.convert(entityInfo));
            }
            return results;
        } else {
            List<Object> entityIds = new ArrayList<>();
            for (EntityInfo entityInfo : entityInfos) {
                entityIds.add(entityInfo.getId());
            }
            EntityInfo firstInfo = entityInfos.get(0);
            String placeholders = StringUtils.collectionToCommaDelimitedString(Collections.nCopies(entityIds.size(), "?"));
            String whereClause = String.format(" where o.%s in (%s)", firstInfo.getIdName(), placeholders);
            String sql = fetchEntitySqlBuilder(docClass, ftQuery).append(whereClause).toString();
            RowMapper<T> rowMapper = getRowMapper(docClass);
            return jdbcOperations.query(sql.toString(), rowMapper, entityIds.toArray());
        }
    }
    
    protected <T> StringBuilder fetchEntitySqlBuilder(Class<T> docClass, FullTextQuery<T> ftQuery) {
        return new StringBuilder(String.format("select o.* from %s o", getTableName(docClass)));
    }
    
    protected <T> RowMapper<T> getRowMapper(Class<T> docClass) {
        return new BeanPropertyRowMapper<>(docClass);
    }
    
    public static String getTableName(Class<?> docClass) {
        Table table = AnnotationUtils.findAnnotation(docClass, Table.class);
        if (table == null) {
            throw new SearchException("Annotation @javax.persistence.Table is required for docClass: " 
                    + docClass.getName());
        }
        return table.name();
    }

    @Override
    protected <T> void index(Class<T> docClass, Method getIdMethod) {
        String sql = fetchEntitySqlBuilder(docClass, null).toString();
        final RowMapper<T> rowMapper = getRowMapper(docClass);
        final Worker worker = getExtendedIntegrator().getWorker();
        jdbcOperations.query(sql, new ResultSetExtractor<Void>() {
            @Override
            public Void extractData(ResultSet rs) throws SQLException, DataAccessException {
                int rowNum = 0;
                while (rs.next()) {
                    processRow(rowMapper.mapRow(rs, rowNum++));
                }
                worker.flushWorks(EmptyTransactionContext.INSTANCE);
                return null;
            }

            private void processRow(T entity) {
                Serializable id = (Serializable) ReflectionUtils.invokeMethod(getIdMethod, entity);
                Work work = new Work(entity, id, WorkType.INDEX);
                worker.performWork(work, EmptyTransactionContext.INSTANCE);
            }
        });
    }

}
