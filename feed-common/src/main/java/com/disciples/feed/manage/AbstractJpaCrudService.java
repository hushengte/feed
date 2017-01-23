package com.disciples.feed.manage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.data.jpa.mapping.JpaPersistentProperty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.mapping.model.BasicPersistentEntity;
import org.springframework.util.Assert;

import com.disciples.feed.Identifiable;

public abstract class AbstractJpaCrudService<DTO extends Identifiable<Integer>, MODEL extends Identifiable<Integer>, DAO extends JpaRepository<MODEL, Integer>> 
		extends AbstractCrudService<DTO, MODEL, DAO> {
	
	@Autowired
	private JpaMetamodelMappingContext mappingContext;
	@Autowired
	private DAO dao;
	
	private BasicPersistentEntity<?, JpaPersistentProperty> persistentEntity;
	
	public AbstractJpaCrudService(Class<DTO> dtoClass, Class<MODEL> modelClass) {
		super(dtoClass, modelClass);
	}
	
	private BasicPersistentEntity<?, JpaPersistentProperty> getPersistentEntity() {
		if (persistentEntity == null) {
			persistentEntity = mappingContext.getPersistentEntity(getModelClass());
		}
		return persistentEntity; 
	}
	
	@Override
	public DTO toDto(final MODEL model) {
		getPersistentEntity().doWithAssociations(new JpaProxyAssociationHandler(mappingContext, model));
		return super.toDto(model);
	}

	@Override
	public int delete(List<Map<String, Object>> dtoList) {
		Assert.notEmpty(dtoList, "The given DTO list must not be empty.");
        List<MODEL> models = new ArrayList<MODEL>();
        for (Map<String, Object> dto : dtoList) {
            MODEL model = BeanUtils.instantiate(getModelClass());
            model.setId((Integer)dto.get("id"));
            models.add(model);
        }
        try {
            getDao().deleteInBatch(models);
            return dtoList.size();
        } catch (DataIntegrityViolationException e) {
            throw new ManageException("存在关联数据，请先删除关联数据");
        }
	}

	@Override
	public DAO getDao() {
		return dao;
	}

}
