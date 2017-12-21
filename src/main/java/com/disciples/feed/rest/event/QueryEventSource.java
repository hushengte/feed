package com.disciples.feed.rest.event;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.util.MultiValueMap;

public class QueryEventSource {

	private Class<?> entityType;
	private Pageable pageable;
	private MultiValueMap<String, Object> parameters;
	private Page<?> result;

	public QueryEventSource(Class<?> entityType, Pageable pageable, MultiValueMap<String, Object> parameters) {
		this.entityType = entityType;
		this.pageable = pageable;
		this.parameters = parameters;
	}

	public Class<?> getEntityType() {
		return entityType;
	}

	public Pageable getPageable() {
		return pageable;
	}

	public MultiValueMap<String, Object> getParameters() {
		return parameters;
	}

	public Page<?> getResult() {
		return result;
	}

	public void setResult(Page<?> result) {
		this.result = result;
	}
	
}
