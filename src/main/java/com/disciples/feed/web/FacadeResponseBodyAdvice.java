package com.disciples.feed.web;

import java.util.Collection;

import org.springframework.core.MethodParameter;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.servlet.mvc.method.annotation.AbstractMappingJacksonResponseBodyAdvice;

/**
 * Return a facade object as serialize object for jackson.
 * 
 * @author Ted Smith
 * @see Response
 */
public class FacadeResponseBodyAdvice extends AbstractMappingJacksonResponseBodyAdvice {

	@Override
	protected void beforeBodyWriteInternal(MappingJacksonValue bodyContainer, MediaType contentType,
	        MethodParameter returnType, ServerHttpRequest request, ServerHttpResponse response) {
		Object value = bodyContainer.getValue();
		if (value instanceof Page) {
			Page<?> pageValue = (Page<?>)value;
			bodyContainer.setValue(Response.success(pageValue.getContent(), pageValue.getTotalElements()));
		} else if (value instanceof Collection) {
			Collection<?> collectionVal = (Collection<?>)value;
			bodyContainer.setValue(Response.success(collectionVal, collectionVal.size()));
		} else {
			if (!(value instanceof Response)) {
				bodyContainer.setValue(Response.success(value));
			}
		}
	}

}
