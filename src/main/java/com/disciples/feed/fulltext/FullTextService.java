package com.disciples.feed.fulltext;

import org.springframework.data.domain.Page;

/**
 * Full text search service
 */
public interface FullTextService {
	
	/**
	 * Query document
	 * @param <T> Document class type
	 * @param query A FullTextQuery
	 * @return A page of document
	 */
	<T> Page<T> query(FullTextQuery<T> query);
	
	/**
	 * Rebuild document index
	 * @param docClasses Document classes
	 */
	void reindex(Class<?> ... docClasses);

}
