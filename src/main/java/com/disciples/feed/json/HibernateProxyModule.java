package com.disciples.feed.json;

import java.io.IOException;

import org.hibernate.collection.spi.PersistentCollection;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.proxy.LazyInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyAccessorFactory;

import com.disciples.feed.BaseEntity;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

public class HibernateProxyModule extends SimpleModule {

	private static final long serialVersionUID = 1901217925118180957L;
	
	private static final Logger logger = LoggerFactory.getLogger(HibernateProxyModule.class);
	
	public HibernateProxyModule() {
		addSerializer(new HibernateProxySerializer());
		addSerializer(new PersistentCollectionSerializer());
	}
	
	@SuppressWarnings("serial")
	static class HibernateProxySerializer extends StdSerializer<HibernateProxy> {

		private HibernateProxySerializer() {
			super(HibernateProxy.class);
		}
		
		@Override
		public void serialize(HibernateProxy value, JsonGenerator gen, SerializerProvider provider) throws IOException {
			LazyInitializer lazyInitializer = value.getHibernateLazyInitializer();
			if (lazyInitializer.isUninitialized()) {
			    Class<?> entityClass = lazyInitializer.getPersistentClass();
	            Object entity = BeanUtils.instantiateClass(entityClass);
	            Object id = lazyInitializer.getIdentifier();
	            if (entity instanceof BaseEntity) {
	                ((BaseEntity)entity).setId((Integer)id);
	            } else {
	                try {
	                    PropertyAccessorFactory.forBeanPropertyAccess(entity).setPropertyValue("id", id);
	                } catch (BeansException e) {
	                    logger.error("Set entity id failed: class={}, id={}, error={}", 
	                            entityClass.getName(), id, e.getMessage());
	                }
	            }
	            gen.writeObject(entity);
			} else {
			    gen.writeObject(lazyInitializer.getImplementation());
			}
		}
	}
	
	@SuppressWarnings("serial")
	static class PersistentCollectionSerializer extends StdSerializer<PersistentCollection> {

		private PersistentCollectionSerializer() {
			super(PersistentCollection.class);
		}
		
		@Override
		public void serialize(PersistentCollection value, JsonGenerator gen, SerializerProvider provider) throws IOException {
			gen.writeNull();
		}
	}
	
}
