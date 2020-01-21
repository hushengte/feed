package com.disciples.feed.json;

import java.io.IOException;

import org.hibernate.collection.spi.PersistentCollection;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.proxy.LazyInitializer;
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
			Class<?> entityClass = lazyInitializer.getPersistentClass();
			Object entity = BeanUtils.instantiateClass(entityClass);
			if (entity instanceof BaseEntity) {
				((BaseEntity)entity).setId((Integer)lazyInitializer.getIdentifier());
			} else {
				try {
					PropertyAccessorFactory.forBeanPropertyAccess(entity).setPropertyValue("id", value);
				} catch (BeansException e) {
					//ignore
				}
			}
			gen.writeObject(entity);
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
