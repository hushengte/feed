package com.disciples.feed.manage;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.core.MethodParameter;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mapping.context.PersistentEntities;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.support.DefaultRepositoryInvokerFactory;
import org.springframework.data.repository.support.Repositories;
import org.springframework.data.repository.support.RepositoryInvoker;
import org.springframework.data.repository.support.RepositoryInvokerFactory;
import org.springframework.data.rest.core.event.AfterDeleteEvent;
import org.springframework.data.rest.core.event.AfterSaveEvent;
import org.springframework.data.rest.core.event.BeforeDeleteEvent;
import org.springframework.data.rest.core.event.BeforeSaveEvent;
import org.springframework.data.rest.core.mapping.RepositoryDetectionStrategy.RepositoryDetectionStrategies;
import org.springframework.data.rest.core.mapping.RepositoryResourceMappings;
import org.springframework.data.rest.core.mapping.ResourceMappings;
import org.springframework.data.rest.core.mapping.ResourceMetadata;
import org.springframework.data.rest.core.mapping.SearchResourceMappings;
import org.springframework.data.util.ClassTypeInformation;
import org.springframework.data.util.TypeInformation;
import org.springframework.format.support.DefaultFormattingConversionService;
import org.springframework.hateoas.core.AnnotationAttribute;
import org.springframework.hateoas.core.MethodParameters;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

public class ManageService implements ApplicationContextAware, ApplicationEventPublisherAware {
	
	private static final Logger LOG = LoggerFactory.getLogger(ManageService.class);
	
	private ApplicationEventPublisher publisher;
	
	private Repositories repositories;
	private RepositoryInvokerFactory invokeFactory;
	private ResourceMappings resourceMappings;
	
	private Map<String, Class<?>> repositoryClassMap = new ConcurrentHashMap<String, Class<?>>();
	
	private EntitySearch entitySearch;
	
	public ManageService(PersistentEntities persistentEntities, Repositories repositories) {
		Assert.notNull(persistentEntities, "PersistentEntities must not be null.");
		Assert.notNull(repositories, "Repositories must not be null.");
		
		this.repositories = repositories;
		this.invokeFactory = new DefaultRepositoryInvokerFactory(repositories, new DefaultFormattingConversionService());
		this.resourceMappings = new RepositoryResourceMappings(repositories, persistentEntities, RepositoryDetectionStrategies.DEFAULT);
	}
	
	@Override
	public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
		this.publisher = applicationEventPublisher;
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		try {
			this.entitySearch = BeanFactoryUtils.beanOfTypeIncludingAncestors(applicationContext, EntitySearch.class);
		} catch (NoSuchBeanDefinitionException e) {
			this.entitySearch = EntitySearch.NULL;
		}
	}
	
	public Class<?> getDomainClass(String key) {
    	Class<?> clazz = repositoryClassMap.get(key);
    	if (clazz == null) {
    		for (Class<?> domainClass : repositories) {
        		if (domainClass.getSimpleName().equalsIgnoreCase(key)) {
        			repositoryClassMap.put(key, domainClass);
        			return domainClass;
        		}
        	}
    	}
    	return clazz;
    }
	
	@SuppressWarnings("unchecked")
	public List<Map<String, Object>> getKeyValues(Class<?> domainClass, String methodText) {
		Assert.notNull(domainClass, "实体类型不能为空");
		
		RepositoryInvoker invoker = invokeFactory.getInvokerFor(domainClass);
		SearchResourceMappings srm = resourceMappings.getSearchResourceMappings(domainClass);
		Method method = srm.getMappedMethod(StringUtils.hasText(methodText) ? methodText : "getKeyValues");
		if (method == null) {
			return Collections.emptyList();
		}
		return (List<Map<String, Object>>) invokeQueryMethod(method, invoker, null, new LinkedMultiValueMap<String, Object>(0));
	}
	
	@Transactional
	public Object save(Object object) {
		Assert.notNull(object, "Object is required");
		
		RepositoryInvoker invoker = invokeFactory.getInvokerFor(object.getClass());
		publisher.publishEvent(new BeforeSaveEvent(object));
		Object savedObject = null;
		try {
			savedObject = invoker.invokeSave(object);
		} catch (DataAccessException e) {
			LOG.error(e.getMessage(), e);
			throw new ManageException("保存失败");
		}
		publisher.publishEvent(new AfterSaveEvent(object));
		return savedObject;
	}
	
	@Transactional
	public <T> void delete(Class<T> domainClass, Integer id, Map<String, Object> dto) {
    	Assert.notNull(domainClass, "实体类型不能为空");
    	
    	RepositoryInvoker invoker = invokeFactory.getInvokerFor(domainClass);
    	if (id != null) {
    		Object object = invoker.invokeFindOne(id);
        	if (object != null) {
        		publisher.publishEvent(new BeforeDeleteEvent(object));
        		try {
        			invoker.invokeDelete(id);
        		} catch (DataIntegrityViolationException e) {
        			throw new ManageException("删除失败：存在关联记录");
        		} catch (DataAccessException e) {
        			LOG.error(e.getMessage(), e);
        			throw new ManageException("删除失败");
        		}
        		publisher.publishEvent(new AfterDeleteEvent(object));
        	}
    	}
	}

	@Transactional
	public <T> int delete(Class<T> domainClass, List<Map<String, Object>> dtoList) {
		Assert.notEmpty(dtoList, "dtoList cannot be emtpty.");
		for (Map<String, Object> dto : dtoList) {
			delete(domainClass, (Integer)dto.get("id"), dto);
		}
		return dtoList.size();
	}
	
	public <T> Page<T> find(Class<T> domainClass, Integer page, Integer size) {
		return find(domainClass, page, size, null);
	}
	
	@SuppressWarnings("unchecked")
	public <T> Page<T> find(Class<T> domainClass, Integer page, Integer size, MultiValueMap<String, Object> params) {
		Assert.notNull(domainClass, "实体类型不能为空");
		
		params = params == null ? new LinkedMultiValueMap<String, Object>() : params;
		Pageable pageable = page != null ? new PageRequest(page, size) : null;
		RepositoryInvoker invoker = invokeFactory.getInvokerFor(domainClass);
		
		Page<T> pageData = null;
		String methodText = (String) params.getFirst("method");
		if (StringUtils.hasText(methodText)) {
			SearchResourceMappings srm = resourceMappings.getSearchResourceMappings(domainClass);
			Method method = srm.getMappedMethod(methodText);
			if (method == null) {
				throw new ManageException(String.format("数据工厂 %s 找不到搜索方法 %s。", domainClass.getSimpleName(), methodText));
			}
			Object queryResult = invokeQueryMethod(method, invoker, pageable, params);
			if (queryResult instanceof Page) {
				pageData = (Page<T>) queryResult;
			} else if (queryResult instanceof List) {
				List<T> result = (List<T>)queryResult;
				pageData = new PageImpl<T>(result, pageable, result.size());
			} else {
				pageData = new PageImpl<T>((List<T>) Collections.singletonList(queryResult), pageable, 1);
			}
		} else {
			pageData = (Page<T>) entitySearch.search(domainClass, pageable, params);
			if (pageData == null) {
				pageData = (Page<T>) invoker.invokeFindAll(pageable);
			}
		}
		return pageData;
	}

	private Object invokeQueryMethod(Method method, RepositoryInvoker invoker, Pageable pageable, MultiValueMap<String, Object> params) {
		MultiValueMap<String, Object> result = new LinkedMultiValueMap<String, Object>(params);
		MethodParameters methodParameters = new MethodParameters(method, new AnnotationAttribute(Param.class));
		List<MethodParameter> parameterList = methodParameters.getParameters();
		List<TypeInformation<?>> parameterTypeInformations = ClassTypeInformation.from(method.getDeclaringClass()).getParameterTypes(method);

		for (Entry<String, List<Object>> entry : params.entrySet()) {
			MethodParameter parameter = methodParameters.getParameter(entry.getKey());
			if (parameter != null) {
				int parameterIndex = parameterList.indexOf(parameter);
				TypeInformation<?> domainType = parameterTypeInformations.get(parameterIndex).getActualType();

				ResourceMetadata metadata = resourceMappings.getMetadataFor(domainType.getType());

				if (metadata != null && metadata.isExported()) {
					result.put(parameter.getParameterName(), entry.getValue());
				}
			}
		}
		return invoker.invokeQueryMethod(method, result, pageable, pageable != null ? pageable.getSort() : null);
	}

}
