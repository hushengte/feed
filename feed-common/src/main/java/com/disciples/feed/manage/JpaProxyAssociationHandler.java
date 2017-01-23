package com.disciples.feed.manage;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.data.jpa.mapping.JpaPersistentProperty;
import org.springframework.data.mapping.Association;
import org.springframework.data.mapping.PersistentProperty;
import org.springframework.data.mapping.PersistentPropertyAccessor;
import org.springframework.data.mapping.SimpleAssociationHandler;
import org.springframework.data.mapping.model.BasicPersistentEntity;
import org.springframework.util.Assert;

public class JpaProxyAssociationHandler implements SimpleAssociationHandler {
	
	private JpaMetamodelMappingContext mappingContext;
	private PersistentPropertyAccessor propertyAccessor;
	private Class<?> ownerType;
	
	public JpaProxyAssociationHandler(JpaMetamodelMappingContext mappingContext, Object model) {
		Assert.notNull(mappingContext, "mappingContext must not be null");
		Assert.notNull(model, "model must not be null");
		BasicPersistentEntity<?, JpaPersistentProperty> persistentEntity = mappingContext.getPersistentEntity(model.getClass());
		if (persistentEntity == null) {
			throw new IllegalArgumentException(model.getClass() + " is NOT a persistent type.");
		}
		this.mappingContext = mappingContext;
		this.propertyAccessor = persistentEntity.getPropertyAccessor(model);
	}

	private JpaProxyAssociationHandler(JpaMetamodelMappingContext mappingContext, Object model, Class<?> ownerType) {
		this(mappingContext, model);
		this.ownerType = ownerType;
	}
	
	@Override
	public void doWithAssociation(Association<? extends PersistentProperty<?>> association) {
		PersistentProperty<?> associationProperty = association.getInverse();
		Object associationModel = propertyAccessor.getProperty(associationProperty);
		if (associationModel != null) {
			Class<?> acutalType = associationModel.getClass();
			//集合
			if (associationProperty.isCollectionLike()) {
				Class<?> componentType = associationProperty.getComponentType();
				BasicPersistentEntity<?, JpaPersistentProperty> componentEntity = mappingContext.getPersistentEntity(componentType);
				//处理集合元素
				if (componentEntity != null && (associationModel instanceof Iterable)) {
					Iterable<?> associationModelItems = (Iterable<?>)associationModel;
					try {
						for (Object item : associationModelItems) {
							componentEntity.doWithAssociations(new JpaProxyAssociationHandler(mappingContext, item, associationProperty.getOwner().getType()));
						}
					} catch (Exception e) {
						propertyAccessor.setProperty(associationProperty, null);
					}
				}
			} else { //实体
				if (associationProperty.isEntity()) {
					BasicPersistentEntity<?, JpaPersistentProperty> associationEntity = mappingContext.getPersistentEntity(associationProperty.getType());
					Class<?> delaredType = associationProperty.getType();
					if (acutalType != delaredType) {
						Object associationModelId = associationEntity.getIdentifierAccessor(associationModel).getIdentifier();
						Object newModel = BeanUtils.instantiate(associationProperty.getType());
						PropertyAccessorFactory.forBeanPropertyAccess(newModel).setPropertyValue(associationEntity.getIdProperty().getName(), associationModelId);
						propertyAccessor.setProperty(associationProperty, newModel);
					} else if (delaredType == ownerType) { //检测循环依赖
						propertyAccessor.setProperty(associationProperty, null);
					} else {
						associationEntity.doWithAssociations(new JpaProxyAssociationHandler(mappingContext, associationModel, associationProperty.getOwner().getType()));
					}
				}
			}
		}
	}

}
