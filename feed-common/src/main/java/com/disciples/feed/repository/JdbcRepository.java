package com.disciples.feed.repository;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface JdbcRepository<T, ID extends Serializable> extends PagingAndSortingRepository<T, ID> {
    
    /**
     * 根据条件查询
     * @param condition 查询条件,例如: columnName1 = ? and columnName2 = ? or columnName3 < ?
     * @param values 属性值列表
     * @return 符合条件的实体列表
     */
    List<T> findBy(String condition, Object... args);
    
    /**
     * 根据条件查询并排序
     * @param condition 查询条件,例如: columnName1 = ? and columnName2 = ? or columnName3 < ?
     * @param values 属性值列表
     * @param sort 排序规则
     * @return 符合条件的实体列表
     */
    List<T> findBy(String condition, Sort sort, Object... args);
    
    /**
     * 根据条件查询并分页，支持排序
     * @param condition 查询条件,例如: columnName1 = ? and columnName2 = ? or columnName3 < ?
     * @param values 属性值列表
     * @param pageable 分页规则
     * @return 符合条件的实体页
     */
    Page<T> findBy(String condition, Pageable pageable, Object... args);
    
    /**
     * 根据条件查询一个实体
     * @param condition 查询条件,例如: columnName1 = ? and columnName2 = ? or columnName3 < ?
     * @param values 属性值列表
     * @return 符合条件的实体，如果找到多个，只返回第一个
     */
    T findOneBy(String condition, Object... args);
    
    /**
     * 根据ID更新个别属性值
     * @param id 实体标识
     * @param values 需要更新的属性，key为数据库列名，value为属性值
     * @return 是否成功
     */
    boolean updateById(ID id, Map<String, Object> columnArgs);
    
}
