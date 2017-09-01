package com.disciples.feed.repository;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.GenericTypeResolver;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Persistable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.ReflectionUtils.FieldCallback;
import org.springframework.util.StringUtils;

/**
 * TODO: 
 * 1.事务支持
 * 2.批量更新
 * 3.in、left join 子句支持
 * 4.SQL生成逻辑抽离
 * 
 * 实现基本的 CRUD 操作
 * @author 001760
 * @param <T> 实体类
 * @param <ID> 实体类主键
 */
public abstract class AbstractJdbcRepository<T extends Persistable<ID>, ID extends Serializable> implements JdbcRepository<T, ID> {
    
    protected Logger logger = LoggerFactory.getLogger(AbstractJdbcRepository.class); 
    
    public static final String QUERY_ALL = "select * from %s";
    public static final String QUERY_FIND_BY_SINGLE_COLUMN = "select * from %s where %s = ?";
    public static final String QUERY_IN = "select * from %s where %s in %s";
    public static final String QUERY_COUNT_ALL = "select count(*) from %s";
    public static final String QUERY_COUNT_BY_SINGLE_COLUMN = "select count(*) from %s where %s = ?";
    public static final String QUERY_MAX_SINGLE_COLUMN = "select MAX(%s) from %s";
    
    public static final String DELETE_BY_SINGLE_COLUMN = "delete from %s where %s = ?";
    public static final String DELETE_ALL = "delete from %s";
    public static final String DELETE_IN = "delete from %s where %s in %s";
    
    public static final String INSERT_SINGLE = "insert into %s (%s) values %s";
    public static final String UPDATE_BY_SINGLE_COLUMN = "update %s set %s where %s = ?";
    
    private static final String DEFAULT_ID_NAME = "id";
    
    @Resource
    private JdbcTemplate jdbcTemplate;
    
    private Class<T> domainClass;
    private PersistableBeanPropertyMapper<T> mappingContext;
    private String tableName;
    private String idName = DEFAULT_ID_NAME;
    
    public AbstractJdbcRepository(Class<T> domainClass, String tableName) {
        Assert.notNull(domainClass, "The given domainClass must not be null.");
        Assert.notNull(tableName, "The given tableName must not be null.");
        this.domainClass = domainClass;
        this.mappingContext = new PersistableBeanPropertyMapper<T>(domainClass);
        // TODO: 解析 @javax.persistence.Table
        this.tableName = tableName;
    }
    
    /**
     * 构建SQL占位符
     * @return "(?,?, ..., ?)"
     */
    protected String buildPlaceholders(Iterable<?> iterable) {
        Iterator<?> it = iterable.iterator();
        if (!it.hasNext()) {
            return "";
        }
        StringBuilder placeholderSb = new StringBuilder("(");
        while (it.hasNext()) {
            placeholderSb.append("?,");
            it.next();
        }
        return placeholderSb.deleteCharAt(placeholderSb.length() - 1).append(')').toString();
    }
    
    /**
     * 追加 order by 子句
     * @param query 原始语句
     * @param sort 排序条件
     * @return 带排序功能的查询
     */
    protected String appendOrderBy(String query, Sort sort) {
    	Assert.notNull(query, "'query' can't be empty");
        StringBuilder sb = new StringBuilder(query);
        if (sort != null) {
        	sb.append(" order by ");
            for (Order order : sort) {
            	sb.append(mappingContext.propertyToColumnMap.get(order.getProperty())).append(' ').append(order.getDirection().toString()).append(',');
            }
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }
    
    /**
     * 追加 limit 子句
     * @param query 原始语句
     * @param pageable pageable 分布规则
     * @return 带分页功能的查询
     */
    protected String appendPaging(String query, Pageable pageable) {
    	Assert.notNull(query, "'query' can't be empty");
        StringBuilder sb = new StringBuilder(query);
        if (pageable != null) {
        	sb.append(" limit ").append(pageable.getPageNumber() * pageable.getPageSize()).append(',').append(pageable.getPageSize());
        }
        return sb.toString();
    }
    
    /**
     * 追加where子句
     * @param query 原始查询
     * @param condition 查询条件, 例如: columnName1 = ? and columnName2 = ? or columnName3 < ?
     */
    protected String appendWhereClause(String query, String condition) {
        Assert.notNull(query, "'query' can't be empty");
        StringBuilder sb = new StringBuilder(query);
        if (StringUtils.hasText(condition)) {
            sb.append(" where ").append(condition);
        }
        return sb.toString();
    }
    
    private String buildColumnList(List<String> columnNames, boolean escapeId) {
        StringBuilder sb = new StringBuilder();
        for (String columnName : columnNames) {
            if (escapeId && columnName.equals(idName)) {
                continue;
            }
            sb.append(columnName).append(',');
        }
        if (sb.length() > 0) {
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public <S extends T> S save(S entity) {
        Assert.notNull(entity, "The given entity must not be null!");
        ID id = entity.getId();
        BeanWrapper bw = PropertyAccessorFactory.forBeanPropertyAccess(entity);
        if (id == null) { // 使用数据库自增长方式生成
            if (!Number.class.isAssignableFrom(GenericTypeResolver.resolveTypeArgument(domainClass, Persistable.class))) {
                throw new UnsupportedOperationException("只支持主键自增长策略，请设置ID");
            }
            final List<Object> argsList = new ArrayList<Object>();
            for (String property : mappingContext.propertyList) {
                if (!property.equals(mappingContext.columnToPropertyMap.get(idName))) {
                    argsList.add(bw.getPropertyValue(property));
                }
            }
            String columnStr = buildColumnList(mappingContext.columnList, true);
            final String sql = String.format(INSERT_SINGLE, tableName, columnStr, buildPlaceholders(argsList));
            logger.debug(sql);
            GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(new PreparedStatementCreator() {
                @Override
                public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
                    PreparedStatement ps = con.prepareStatement(sql, new String[]{idName});
                    for (int i = 0; i < argsList.size(); i++) {
                        ps.setObject(i + 1, argsList.get(i));
                    }
                    return ps;
                }
            }, keyHolder);
            id = (ID) keyHolder.getKey();
            bw.setPropertyValue(mappingContext.columnToPropertyMap.get(idName), id);
            return entity;
        } else {
        	List<Object> argsList = new ArrayList<Object>();
            StringBuilder columns = new StringBuilder();
            for (String property : mappingContext.propertyList) {
                argsList.add(bw.getPropertyValue(property));
                columns.append(mappingContext.propertyToColumnMap.get(property)).append("=?,");
            }
            if (columns.length() > 0) {
            	columns.deleteCharAt(columns.length() - 1);
            }
            String sql = String.format(UPDATE_BY_SINGLE_COLUMN, tableName, columns.toString(), idName);
            logger.debug(sql);
            argsList.add(id);
            jdbcTemplate.update(sql, argsList.toArray());
            return entity;
        }
    }
    
    @Override
	public <S extends T> Iterable<S> save(Iterable<S> entities) {
		Assert.notNull(entities, "The given entities must not be null.");
		for (S entity : entities) {
			save(entity);
		}
		return entities;
	}
    
    @Override
    public boolean updateById(ID id, Map<String, Object> columnArgs) {
        Assert.notNull(columnArgs, "The given values must not be null.");
        StringBuilder sb = new StringBuilder();
        // "column1=?,column2=?,...,columnN=?"
        for (Map.Entry<String, Object> entry : columnArgs.entrySet()) {
            sb.append(mappingContext.propertyToColumnMap.get(entry.getKey())).append("=?,");
        }
        if (sb.length() == 0) {
            return false;
        }
        String sql = String.format(UPDATE_BY_SINGLE_COLUMN, tableName, sb.deleteCharAt(sb.length() - 1).toString(), idName);
        logger.debug(sql);
        Object[] valuesArray = columnArgs.values().toArray();
        Object[] args = new Object[valuesArray.length + 1];
        System.arraycopy(valuesArray, 0, args, 0, valuesArray.length);
        args[args.length - 1] = id;
        return jdbcTemplate.update(sql, args) > 0;
    }
    
    @Override
    public boolean exists(ID id) {
        Assert.notNull(id, "The given id must not be null!");
        String sql = String.format(QUERY_COUNT_BY_SINGLE_COLUMN, tableName, idName);
        logger.debug(sql);
        Long count = jdbcTemplate.queryForObject(sql, new Object[] {id}, Long.class);
        return count != null && count > 0;
    }
    
    @Override
    public long count() {
        String sql = String.format(QUERY_COUNT_ALL, tableName);
        logger.debug(sql);
        return jdbcTemplate.queryForObject(sql, Long.class);
    }
    
    @Override
    public List<T> findAll() {
        String sql = String.format(QUERY_ALL, tableName);
        logger.debug(sql);
        return jdbcTemplate.query(sql, mappingContext);
    }
    
    @Override
    public List<T> findAll(Sort sort) {
        String sql = String.format(appendOrderBy(QUERY_ALL, sort), tableName);
        logger.debug(sql);
        return jdbcTemplate.query(sql, mappingContext);
    }

    @Override
    public Page<T> findAll(Pageable pageable) {
    	if (pageable == null) {
    		List<T> result = findAll();
    		return new PageImpl<T>(result, pageable, result.size());
    	}
        List<T> contents = Collections.emptyList();
        long total = count();
        if (total > 0) {
            String sql = String.format(appendPaging(appendOrderBy(QUERY_ALL, pageable.getSort()), pageable), tableName);
            logger.debug(sql);
            contents = jdbcTemplate.query(sql, mappingContext);
        }
        return new PageImpl<T>(contents, pageable, total);
    }
    
    @Override
    public T findOne(ID id) {
        Assert.notNull(id, "The given id must not be null!");
        String sql = String.format(QUERY_FIND_BY_SINGLE_COLUMN, tableName, idName);
        logger.debug(sql);
        List<T> resultList = jdbcTemplate.query(sql, new Object[] {id}, mappingContext);
        if (resultList.size() == 0) {
            return null;
        }
        return resultList.get(0);
    }

    @Override
    public List<T> findAll(Iterable<ID> ids) {
        Assert.notNull(ids, "The given ids must not be null.");
        List<Object> args = new ArrayList<Object>();
        for (ID id : ids) {
            args.add(id);
        }
        String sql = String.format(QUERY_IN, tableName, idName, buildPlaceholders(ids));
        logger.debug(sql);
        return jdbcTemplate.query(sql, args.toArray(), mappingContext);
    }

    @Override
    public List<T> findBy(String condition, Object... args) {
        return findBy(condition, (Sort)null, args);
    }

    @Override
    public List<T> findBy(String condition, Sort sort, Object... args) {
        Assert.notNull(condition, "The given condition must not be null.");
        String sql = appendOrderBy(appendWhereClause(String.format(QUERY_ALL, tableName), condition), sort);
        logger.debug(sql);
        return jdbcTemplate.query(sql, mappingContext, args);
    }
    
    public List<T> findBySql(String sql, Object... args){
    	logger.debug(sql);
    	return jdbcTemplate.query(sql, mappingContext, args);
    }

    @Override
    public Page<T> findBy(String condition, Pageable pageable, Object... args) {
        Assert.notNull(condition, "The given condition must not be null.");
        List<T> contents = Collections.emptyList();
        String countSql = appendWhereClause(String.format(QUERY_COUNT_ALL, tableName), condition);
        logger.debug(countSql);
        Integer total = jdbcTemplate.queryForObject(countSql, Integer.class, args);
        if (total > 0) {
            String sql = appendOrderBy(appendWhereClause(String.format(QUERY_ALL, tableName), condition), pageable.getSort());
            sql = appendPaging(sql, pageable);
            logger.debug(sql);
            contents = jdbcTemplate.query(sql, mappingContext, args);
        }
        return new PageImpl<T>(contents, pageable, total);
    }

    @Override
    public T findOneBy(String condition, Object... args) {
        List<T> result = findBy(condition, args);
        if (result.size() > 0) {
            return result.get(0);
        }
        return null;
    }

    @Override
    public void delete(ID id) {
        Assert.notNull(id, "The given id must not be null!");
        String sql = String.format(DELETE_BY_SINGLE_COLUMN, tableName, idName);
        logger.debug(sql);
        jdbcTemplate.update(sql, id);
    }

    @Override
    public void delete(T entity) {
        Assert.notNull(entity, "The entity must not be null!");
        delete(entity.getId());
    }

    @Override
    public void delete(Iterable<? extends T> entities) {
        Assert.notNull(entities, "The given Iterable of entities not be null!");
        List<ID> idList = new ArrayList<ID>();
        for (T entity : entities) {
            idList.add(entity.getId());
        }
        String sql = String.format(DELETE_IN, tableName, idName, buildPlaceholders(idList));
        logger.debug(sql);
        jdbcTemplate.update(sql, idList.toArray());
    }
    
    @Override
    public void deleteAll() {
        String sql = String.format(DELETE_ALL, tableName);
        logger.debug(sql);
        jdbcTemplate.update(sql);
    }
    
    public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

	public Class<T> getDomainClass() {
		return domainClass;
	}

	public PersistableBeanPropertyMapper<T> getMappingContext() {
		return mappingContext;
	}

	public String getTableName() {
		return tableName;
	}

	public String getIdName() {
		return idName;
	}

	/**
     * TODO: 解析 JPA的注解，更好地支持 property 到 column 的映射
     */
    private static class PersistableBeanPropertyMapper<T> extends BeanPropertyRowMapper<T> implements FieldCallback {
        
        private Map<String, String> propertyToColumnMap;
        private Map<String, String> columnToPropertyMap;
        
        private List<String> propertyList;
        private List<String> columnList;
        
        public PersistableBeanPropertyMapper(Class<T> domainClass) {
            super(domainClass);
        }

        @Override
        protected void initialize(Class<T> mappedClass) {
            propertyToColumnMap = new HashMap<String, String>();
            columnToPropertyMap = new HashMap<String, String>();
            propertyList = new ArrayList<String>();
            columnList = new ArrayList<String>();
            ReflectionUtils.doWithFields(mappedClass, this);
            super.initialize(mappedClass);
        }
        
        @Override
		public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
        	//skip static fields, collection fields
        	if (Modifier.isStatic(field.getModifiers()) || Collection.class.isAssignableFrom(field.getType())) {
        		return;
        	}
			String propertyName = field.getName();
			//TODO: FIXME
			Value value = field.getAnnotation(Value.class);
            String columnName = value == null ? underscoreName(propertyName) : value.value();
            propertyList.add(propertyName);
            columnList.add(columnName);
            propertyToColumnMap.put(propertyName, columnName);
            columnToPropertyMap.put(columnName, propertyName);
		}
    }
    
}
