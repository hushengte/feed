package com.disciples.feed.manage;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.repository.PagingAndSortingRepository;

import com.disciples.feed.Identifiable;

/**
 * 基本的增、删、改、查服务接口
 * @param <DTO> 数据传输类
 * @param <MODEL> 实体类
 * @param <DAO> 数据访问对象
 */
public interface CrudService<DTO extends Identifiable<Integer>, MODEL extends Identifiable<Integer>, DAO extends PagingAndSortingRepository<MODEL, Integer>> {
	
	/**
	 * 查找一页数据
	 * @param page 页号，从0开始
	 * @param size 每页数据量，从1开始
	 * @param params 查询条件，可以为null
	 * @return 匹配的页数据
	 */
	Page<DTO> find(int page, int size, Map<String, Object> params);
	
	/**
	 * 根据条件查找数据
	 * @param params 查询条件，可以为null
	 * @return 匹配的数据列表
	 */
	List<DTO> find(Map<String, Object> params);
	
	/**
	 * 根据标识查找实体数据
	 * @param id 实体标识
	 * @return 实体数据
	 */
	DTO findOne(Integer id);
	
	/**
	 * 添加或更新，dto的标识为空，执行添加，否则执行更新
	 * @param dto 实体数据
	 * @return 保存后的实体数据，具有标识
	 */
	DTO save(DTO dto);
	
	/**
	 * 根据标识删除实体
	 * @param id 实体标识
	 */
	void delete(Integer id);
	
	/**
	 * 批量删除实体
	 * @param dtoList 实体相关数据列表
	 * @return 成功删除的数量
	 */
	int delete(List<Map<String, Object>> dtoList);
	
	/**
	 * 获取实体数据访问对象
	 * @return 实体数据访问对象
	 */
	DAO getDao();
	
}
