package com.disciples.feed.fulltext;

import org.springframework.data.domain.Page;

/**
 * 全文检索服务
 */
public interface FullTextService {
	
	/**
	 * 查询文档
	 * @param query 查询请求
	 * @return 结果集
	 */
	<T> Page<T> query(FullTextQuery<T> query);
	
	/**
	 * 重建索引
	 * @param docClasses 文档类
	 */
	void reindex(Class<?> ... docClasses);

}
