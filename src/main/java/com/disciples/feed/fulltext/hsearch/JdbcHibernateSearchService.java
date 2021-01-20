package com.disciples.feed.fulltext.hsearch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.persistence.Table;

import org.hibernate.search.engine.integration.impl.ExtendedSearchIntegrator;
import org.hibernate.search.exception.SearchException;
import org.hibernate.search.query.engine.spi.EntityInfo;
import org.hibernate.search.query.engine.spi.HSQuery;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
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
    protected long getTotalCount(Class<?> docClass) {
        String countSql = String.format("select count(0) from %s", getTableName(docClass));
        return jdbcOperations.queryForObject(countSql, Long.class);
    }

    @Override
    protected <T> List<T> getEntityList(Class<T> docClass, Pageable pageable) {
        String sql = fetchEntitySqlBuilder(docClass, null).append(" limit ?,?").toString();
        RowMapper<T> rowMapper = getRowMapper(docClass);
        return jdbcOperations.query(sql.toString(), rowMapper, pageable.getOffset(), pageable.getPageSize());
    }

}
