package com.disciples.feed.manage;

import java.util.List;

import org.springframework.data.domain.Page;

import com.disciples.feed.Identifiable;

/**
 * 基本的增、删、改、查服务接口
 * @param <T> 必须实现Identifiable接口
 */
public interface CrudService<T extends Identifiable<Integer>> {

	/**
	 * 获取一页数据
	 * @param page 页号，从0开始
	 * @param size 每页数据量，从1开始
	 * @param filter 过滤条件，为json字符串，可以为空
	 * @return
	 */
	Page<T> getPage(int page, int size, String filter);
	
	T get(Integer id);
	
	/**
	 * 添加或更新
	 * @param model
	 * @return
	 */
	T save(T model);
	
	void delete(Integer id);
	
	int delete(List<T> modelList);
	
}
