package com.disciples.feed.rest;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Persistable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.core.RepositoryInformation;
import org.springframework.data.repository.support.DefaultRepositoryInvokerFactory;
import org.springframework.data.repository.support.Repositories;
import org.springframework.data.repository.support.RepositoryInvoker;
import org.springframework.data.repository.support.RepositoryInvokerFactory;
import org.springframework.format.support.DefaultFormattingConversionService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

import com.disciples.feed.rest.RepositoryEvent.Type;

public class RepositoryService implements ApplicationEventPublisherAware {
	
	private static final Logger LOG = LoggerFactory.getLogger(RepositoryService.class);
	private static final MultiValueMap<String, Object> EMPTY_PARAMS = new LinkedMultiValueMap<String, Object>(0);
	
	private ApplicationEventPublisher publisher;
	private RepositoryInvokerFactory invokeFactory;
	
	private Map<String, Class<?>> repositoryClassCache = new HashMap<String, Class<?>>();
	private Map<Class<?>, Map<String, Method>> repositoryMethodsCache = new HashMap<Class<?>, Map<String, Method>>();
	
	public RepositoryService(Repositories repositories) {
		Assert.notNull(repositories, "Repositories must not be null.");
		
		this.invokeFactory = new DefaultRepositoryInvokerFactory(repositories, new DefaultFormattingConversionService());
		this.populateCache(repositories);
	}
	
	private void populateCache(Repositories repositories) {
		for (Class<?> type : repositories) {
			String key = StringUtils.uncapitalize(type.getSimpleName());
			repositoryClassCache.put(key, type);
			
			Map<String, Method> methodsCache = new HashMap<String, Method>();
			Optional<RepositoryInformation> repositoryInfo = repositories.getRepositoryInformationFor(type);
			if (repositoryInfo.isPresent()) {
			    repositoryInfo.get().getQueryMethods().forEach(queryMethod -> {
	                methodsCache.put(queryMethod.getName(), queryMethod);
	            });
	            repositoryMethodsCache.put(type, methodsCache);
			}
		}
	}
	
	@Override
	public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
	    Assert.notNull(applicationEventPublisher, "ApplicationEventPublisher is required.");
		this.publisher = applicationEventPublisher;
	}
	
	public Class<?> getDomainClass(String key) {
    	return repositoryClassCache.get(key);
    }
	
	public <ID, T extends Persistable<ID>> T save(T entity) {
		Assert.notNull(entity, "实体不能为空");
		RepositoryInvoker invoker = invokeFactory.getInvokerFor(entity.getClass());
		final boolean isNew = entity.isNew();
		Type beforeEventType = isNew ? Type.BEFORE_CREATE : Type.BEFORE_UPDATE;
		publisher.publishEvent(new RepositoryEvent(entity, beforeEventType));
		T saved = entity;
		try {
			saved = invoker.invokeSave(entity);
		} catch (DataAccessException e) {
			LOG.error(e.getMessage(), e);
			throw new RepositoryException("保存失败：数据库访问异常", e);
		}
		Type afterEventType = isNew ? Type.AFTER_CREATE : Type.AFTER_UPDATE;
		publisher.publishEvent(new RepositoryEvent(entity, afterEventType));
		return saved;
	}
	
	protected void doDelete(RepositoryInvoker invoker, List<Map<String, Object>> dataList) {
		for (Map<String, Object> data : dataList) {
			Integer id = (Integer)data.get("id");
			if (id != null) {
	    		Optional<?> optional = invoker.invokeFindById(id);
	    		if (optional.isPresent()) {
	    		    Object object = optional.get();
	    		    publisher.publishEvent(new RepositoryEvent(object, Type.BEFORE_DELETE));
                    invoker.invokeDeleteById(id);
                    publisher.publishEvent(new RepositoryEvent(object, Type.AFTER_DELETE));
	    		}
	    	}
		}
	}
	
	public <T> void delete(Class<T> domainClass, Integer id) {
    	delete(domainClass, Collections.singletonList(Collections.<String, Object>singletonMap("id", id)));
	}

	@Transactional
	public <T> int delete(Class<T> domainClass, List<Map<String, Object>> dataList) {
		Assert.notNull(domainClass, "实体类型不能为空");
		Assert.notEmpty(dataList, "删除数据列表不能为空.");
		RepositoryInvoker invoker = invokeFactory.getInvokerFor(domainClass);
		try {
			doDelete(invoker, dataList);
			return dataList.size();
		} catch (DataAccessException e) {
			if (e instanceof DataIntegrityViolationException) {
				throw new RepositoryException("删除失败：请先删除关联数据再操作", e);
			}
			LOG.error(e.getMessage(), e);
			throw new RepositoryException("删除失败：数据库访问异常", e);
		}
	}
	
	private Method getMappedMethod(Class<?> domainClass, String methodName) {
		Map<String, Method> methodsCache = repositoryMethodsCache.get(domainClass);
		return methodsCache != null ? methodsCache.get(methodName) : null;
	}
	
	public List<?> getKeyValues(Class<?> domainClass, String methodText) {
		Assert.notNull(domainClass, "实体类型不能为空");
		
		RepositoryInvoker invoker = invokeFactory.getInvokerFor(domainClass);
		String methodName = StringUtils.hasText(methodText) ? methodText : "getKeyValues";
		Method method = getMappedMethod(domainClass, methodName);
		if (method == null) {
			return Collections.emptyList();
		}
		Object result = invoker.invokeQueryMethod(method, EMPTY_PARAMS, Pageable.unpaged(), Sort.unsorted()).get();
		return (result instanceof List) ? (List<?>)result : Collections.singletonList(result);
	}
	
	public <T> Page<T> find(Class<T> domainClass, Integer page, Integer size) {
		return find(domainClass, page, size, null);
	}
	
	@SuppressWarnings("unchecked")
	public <T> Page<T> find(Class<T> domainClass, Integer page, Integer size, MultiValueMap<String, Object> params) {
		Assert.notNull(domainClass, "实体类型不能为空");
		
		params = params == null ? new LinkedMultiValueMap<String, Object>() : params;
		Pageable pageable = page != null ? PageRequest.of(page, size) : Pageable.unpaged();
		RepositoryInvoker invoker = invokeFactory.getInvokerFor(domainClass);
		
		String methodText = (String) params.getFirst("method");
		if (StringUtils.hasText(methodText)) {
			Method method = getMappedMethod(domainClass, methodText);
			if (method == null) {
			    String message = String.format("实体%s的数据工厂不存在搜索方法 %s。", domainClass.getSimpleName(), methodText);
				throw new RepositoryException(message);
			}
			Object result = invoker.invokeQueryMethod(method, params, pageable, Sort.unsorted()).get();
			return (Page<T>)toPage(result, pageable);
		}
		QueryEventSource source = new QueryEventSource(domainClass, pageable, params);
		this.publisher.publishEvent(new RepositoryEvent(source));
		Page<T> pageData = (Page<T>) source.getResult();
		if (pageData == null) {
			pageData = (Page<T>) invoker.invokeFindAll(pageable);
		}
		return pageData;
	}
	
    private static Page<?> toPage(Object queryResult, Pageable pageable) {
	    if (queryResult instanceof Page) {
            return (Page<?>) queryResult;
        }
        if (queryResult instanceof List) {
            List<?> result = (List<?>)queryResult;
            return new PageImpl<>(result, pageable, result.size());
        }
        return new PageImpl<>((List<?>) Collections.singletonList(queryResult), pageable, 1);
	}

}
