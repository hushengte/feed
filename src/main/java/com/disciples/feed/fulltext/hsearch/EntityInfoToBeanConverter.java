package com.disciples.feed.fulltext.hsearch;

import org.hibernate.search.query.engine.spi.EntityInfo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.core.convert.converter.Converter;
import org.springframework.util.Assert;

class EntityInfoToBeanConverter<T> implements Converter<EntityInfo, T> {
    
    private Class<T> docClass;
    private String[] fields;

    public EntityInfoToBeanConverter(Class<T> docClass, String[] fields) {
        Assert.notNull(docClass, "Document class must not be null.");
        Assert.isTrue(fields != null && fields.length > 0, "fields must not be empty.");
        this.docClass = docClass;
        this.fields = fields;
    }
    
    @Override
    public T convert(EntityInfo source) {
        Object[] projections = source.getProjection();
        T result = BeanUtils.instantiateClass(docClass);
        BeanWrapper bw = PropertyAccessorFactory.forBeanPropertyAccess(result);
        for (int i = 0; i < projections.length; i++) {
            String propertyName = fields[i];
            Object value = projections[i];
            bw.setPropertyValue(propertyName, value);
        }
        return result;
    }

}
