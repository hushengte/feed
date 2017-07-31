package com.disciples.feed.manage;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.core.MethodParameter;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mapping.PersistentEntity;
import org.springframework.data.mapping.context.MappingContext;
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

import com.disciples.feed.KeyValue;

public class ManageService implements ApplicationContextAware, ApplicationEventPublisherAware {
	
	private static final String SPRING_DATA_JPA_MAPPING_CONTEXT_CLASS = "org.springframework.data.jpa.mapping.JpaMetamodelMappingContext";
	
	private ApplicationEventPublisher publisher;
	
	private PersistentEntities persistentEntities;
	private Repositories repositories;
	private RepositoryInvokerFactory invokeFactory;
	private List<MappingContext<?, ?>> mappingContexts;
	private ResourceMappings resourceMappings;
	
	private MappingContext<?, ?> jpaMappingContext;
	
	private Map<String, Class<?>> repositoryClassMap = new ConcurrentHashMap<String, Class<?>>();
	
	private EntitySearch entitySearch;
	
	public ManageService() {}
	
	@Override
	public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
		this.publisher = applicationEventPublisher;
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.mappingContexts = new ArrayList<MappingContext<?, ?>>();
		this.entitySearch = BeanFactoryUtils.beanOfType(applicationContext, EntitySearch.class);
		if (this.entitySearch == null) {
			this.entitySearch = EntitySearch.EMPTY;
		}
		for (MappingContext<?, ?> context : BeanFactoryUtils.beansOfTypeIncludingAncestors(applicationContext, MappingContext.class).values()) {
			if (SPRING_DATA_JPA_MAPPING_CONTEXT_CLASS.equals(context.getClass().getName())) {
				jpaMappingContext = context;
			}
			mappingContexts.add(context);
		}
		this.persistentEntities = new PersistentEntities(mappingContexts);
		this.repositories = new Repositories(applicationContext);
		this.invokeFactory = new DefaultRepositoryInvokerFactory(repositories, new DefaultFormattingConversionService());
		this.resourceMappings = new RepositoryResourceMappings(repositories, persistentEntities, RepositoryDetectionStrategies.DEFAULT);
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
	
	public List<KeyValue> getKeyValues(Class<?> domainClass) {
		RepositoryInvoker invoker = invokeFactory.getInvokerFor(domainClass);
		Iterable<Object> content = invoker.invokeFindAll((Pageable)null);
		List<KeyValue> result = new ArrayList<KeyValue>();
		for (Object obj : content) {
			BeanWrapper bw = PropertyAccessorFactory.forBeanPropertyAccess(obj);
			Object key = bw.getPropertyValue("id");
			String value = (String) bw.getPropertyValue("name");
			result.add(new KeyValue(key, value));
		}
		return result;
	}
	
	@Transactional
	public Object save(Object object) {
		Assert.notNull(object, "Object is required");
		
		Class<?> domainClass = object.getClass();
		PersistentEntity<?, ?> persistentEntity = persistentEntities.getPersistentEntity(domainClass);
		if (persistentEntity == null) {
			throw new ManageException(String.format("Cannot save object of type %s.", domainClass.getName()));
		}
		
		RepositoryInvoker invoker = invokeFactory.getInvokerFor(domainClass);
		publisher.publishEvent(new BeforeSaveEvent(object));
		Object savedObject = invoker.invokeSave(object);
		publisher.publishEvent(new AfterSaveEvent(object));
		
		if (jpaMappingContext != null) {
			persistentEntity.doWithAssociations(new ProxyAssociationHandler(jpaMappingContext, savedObject));
		}
		return savedObject;
	}
	
	public Object processAssociation(Object object) {
		if (object != null && jpaMappingContext != null) {
			Class<?> domainClass = object.getClass();
			PersistentEntity<?, ?> persistentEntity = persistentEntities.getPersistentEntity(domainClass);
			if (persistentEntity == null) {
				throw new ManageException(String.format("Cannot save object of type %s.", domainClass.getName()));
			}
			persistentEntity.doWithAssociations(new ProxyAssociationHandler(jpaMappingContext, object));
		}
		return object;
	}

	@Transactional
	public void delete(String repository, Integer id, Map<String, Object> dto) {
		Class<?> domainClass = getDomainClass(repository);
    	Assert.notNull(domainClass, String.format("领域对象不存在：repository=%s", repository));
    	
    	RepositoryInvoker invoker = invokeFactory.getInvokerFor(domainClass);
    	if (id != null) {
    		Object object = invoker.invokeFindOne(id);
        	if (object != null) {
        		publisher.publishEvent(new BeforeDeleteEvent(object));
        		invoker.invokeDelete(id);
        		publisher.publishEvent(new AfterDeleteEvent(object));
        	}
    	}
	}

	@Transactional
	public int delete(String repository, List<Map<String, Object>> dtoList) {
		Assert.notEmpty(dtoList, "dtoList cannot be emtpty.");
		for (Map<String, Object> dto : dtoList) {
			delete(repository, (Integer)dto.get("id"), dto);
		}
		return dtoList.size();
	}
	
	public <T> Page<T> find(Class<T> domainClass, Integer page, Integer size) {
		return find(domainClass, page, size, null);
	}
	
	@SuppressWarnings("unchecked")
	public <T> Page<T> find(Class<T> domainClass, Integer page, Integer size, MultiValueMap<String, Object> params) {
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
		final PersistentEntity<?, ?> persistentEntity = persistentEntities.getPersistentEntity(domainClass);
		if (persistentEntity != null && jpaMappingContext != null) {
			return (Page<T>) entitySearch.afterSearch(domainClass, pageData.map(new Converter<T, T>() {
				@Override
				public T convert(T source) {
					persistentEntity.doWithAssociations(new ProxyAssociationHandler(jpaMappingContext, source));
					return source;
				}
			}));
		}
		return (Page<T>) entitySearch.afterSearch(domainClass, pageData);
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
