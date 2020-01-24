package com.disciples.feed.fulltext.hsearch;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.hibernate.annotations.common.reflection.ReflectionManager;
import org.hibernate.annotations.common.reflection.java.JavaReflectionManager;
import org.hibernate.search.cfg.SearchMapping;
import org.hibernate.search.cfg.spi.IdUniquenessResolver;
import org.hibernate.search.cfg.spi.SearchConfigurationBase;
import org.hibernate.search.engine.service.classloading.impl.DefaultClassLoaderService;
import org.hibernate.search.engine.service.classloading.spi.ClassLoaderService;
import org.hibernate.search.engine.service.spi.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

public class SimpleSearchConfiguration extends SearchConfigurationBase {
    
    private Properties properties;
    
    private Map<String, Class<?>> classMappings = new HashMap<>();
    private Set<Class<?>> mappedClasses = new HashSet<>();
    
    private final ClassLoaderService classLoaderService;
    private final Map<Class<? extends Service>, Object> providedServices;
    private ReflectionManager reflectionManager;
    
    public SimpleSearchConfiguration(Properties properties, List<Class<?>> mappingClasses) {
        Assert.notNull(properties, "Properties is required.");
        this.properties = properties;
        if (!CollectionUtils.isEmpty(mappingClasses)) {
            for (Class<?> mappingClass : mappingClasses) {
                if (mappingClass != null) {
                    classMappings.put(mappingClass.getName(), mappingClass);
                }
            }
            mappedClasses.addAll(classMappings.values());
        }
        
        classLoaderService = new DefaultClassLoaderService();
        IdUniquenessResolver idUniquenessResolver = new MappedClassIdUniquenessResolver();
        providedServices = Collections.singletonMap(IdUniquenessResolver.class, idUniquenessResolver);
        reflectionManager = new JavaReflectionManager();
    }

    @Override
    public Iterator<Class<?>> getClassMappings() {
        return mappedClasses.iterator();
    }

    @Override
    public Class<?> getClassMapping(String name) {
        return classMappings.get(name);
    }

    @Override
    public String getProperty(String propertyName) {
        return properties.getProperty(propertyName);
    }

    @Override
    public Properties getProperties() {
        return properties;
    }

    @Override
    public ReflectionManager getReflectionManager() {
        return reflectionManager;
    }

    @Override
    public SearchMapping getProgrammaticMapping() {
        return null;
    }

    @Override
    public Map<Class<? extends Service>, Object> getProvidedServices() {
        return providedServices;
    }

    @Override
    public ClassLoaderService getClassLoaderService() {
        return classLoaderService;
    }
    
    @Override
    public boolean isIndexMetadataComplete() {
        return true;
    }
    
    private class MappedClassIdUniquenessResolver implements IdUniquenessResolver {
        
        @Override
        public boolean areIdsUniqueForClasses(Class<?> entityInIndex, Class<?> otherEntityInIndex) {
            /*
             * Look for the top most superclass of each that is also a mapped entity
             * That should be the root entity for that given class.
             */
            Class<?> rootOfEntityInIndex = getRootEntity( entityInIndex );
            Class<?> rootOfOtherEntityInIndex = getRootEntity( otherEntityInIndex );
            return rootOfEntityInIndex == rootOfOtherEntityInIndex;
        }

        private Class<?> getRootEntity(Class<?> entityInIndex) {
            if (!mappedClasses.contains(entityInIndex)) {
                // should not happen so we return the entity class itself
                return entityInIndex;
            }
            Class<?> potentialParent = entityInIndex;
            do {
                potentialParent = potentialParent.getSuperclass();
                if (potentialParent != null && potentialParent != Object.class && 
                        mappedClasses.contains(potentialParent)) {
                    entityInIndex = potentialParent;
                }
            } while (potentialParent != null && potentialParent != Object.class);
            return entityInIndex;
        }
        
    }

}
