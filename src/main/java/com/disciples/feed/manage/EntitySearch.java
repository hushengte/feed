package com.disciples.feed.manage;

import java.util.Collections;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.util.MultiValueMap;

public interface EntitySearch {

	EntitySearch NULL = new EntitySearch() {
		@Override
		public Page<?> search(Class<?> domainClass, Pageable pageable, MultiValueMap<String, Object> params) {
			return new PageImpl<Object>(Collections.emptyList(), pageable, 0);
		}
	};

	Page<?> search(Class<?> domainClass, Pageable pageable, MultiValueMap<String, Object> params);
	
}
