package com.disciples.feed.manage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.disciples.feed.Identifiable;

/**
 * 此抽象类提供对领域对象的管理操作：如添加，修改，删除，查找等，子类必须实现 DTO 到 MODEL 的相互转换。
 * 
 * @param <DTO> 数据传输对象 (Data Transfer Object)
 * @param <MODEL> 领域对象 (Domain Object)
 */
public abstract class AbstractCrudService<DTO extends Identifiable<Integer>, MODEL extends Identifiable<Integer>, DAO extends JpaRepository<MODEL, Integer>>
		implements CrudService<DTO>, Converter<MODEL, DTO> {
    
    protected static final Logger LOG = LoggerFactory.getLogger(AbstractCrudService.class);
    
    private final Class<DTO> dtoClass;
    private final Class<MODEL> modelClass;
    @Autowired
    protected DAO modelDao;
    
    public AbstractCrudService(Class<DTO> dtoClass, Class<MODEL> modelClass) {
        Assert.notNull(dtoClass, "The given DTO class must not be null.");
        Assert.notNull(modelClass, "The given MODEL class must not be null.");
        this.dtoClass = dtoClass;
        this.modelClass = modelClass;
    }
    
    @Override
	public DTO convert(MODEL source) {
		return toDto(source);
	}
    
    /**
     * 将Model对象转换为Dto对象，子类可能需要覆盖此方法
     * @param model
     * @return Dto对象
     * @throws ManageException
     */
    public DTO toDto(MODEL model) {
    	DTO dto = BeanUtils.instantiate(dtoClass);
        BeanUtils.copyProperties(model, dto);
        return dto;
    }
    
    /**
     * 将Dto对象转换为Model对象，子类可能需要覆盖此方法
     * @param dto
     * @return Model对象
     * @throws ManageException
     */
    public MODEL toModel(DTO dto) {
        MODEL model = BeanUtils.instantiate(modelClass);
        BeanUtils.copyProperties(dto, model);
        return model;
    }
    
    public List<DTO> toDtoList(List<MODEL> modelList) {
        Assert.notNull(modelList, "The given MODEL list must not be null.");
        List<DTO> dtoList = new ArrayList<DTO>();
        for (MODEL model : modelList) {
        	dtoList.add(toDto(model));
        }
        return dtoList;
    }
    
    public List<MODEL> toModelList(List<DTO> dtoList) {
        Assert.notNull(dtoList, "The given DTO list must not be null.");
        List<MODEL> modelList = new ArrayList<MODEL>();
        for (DTO dto : dtoList) {
            modelList.add(toModel(dto));
        }
        return modelList;
    }
    
    protected Page<MODEL> getPage(Pageable pageable) {
        return modelDao.findAll(pageable);
    }
    
    /**
     * 根据过滤条件查找数据
     * @param pageable 分页请求
     * @param field 搜索域，不为空
     * @return 一页领域对象数据
     */
    protected Page<MODEL> getPage(Pageable pageable, JSONObject filter) {
        throw new UnsupportedOperationException("Please OVERRIDE this method in subclass.");
    }
    
    @Override
    public Page<DTO> getPage(int page, int size, String filter) {
        Pageable pageable = new PageRequest(page, size);
        if (!StringUtils.hasText(filter)) {
            return getPage(pageable).map(this);
        }
        try {
        	return getPage(pageable, new JSONObject(filter)).map(this);
        } catch (JSONException e) {
        	throw new ManageException(String.format("filter: '%s' 必须为json格式", filter));
        }
    }
    
    @Override
    public DTO get(Integer id) {
        return this.toDto(modelDao.findOne(id));
    }
    
    /**
     * 添加或修改功能
     */
    @Override
    public DTO save(DTO dto) {
    	MODEL model = modelDao.save(this.toModel(dto));
        if (dto.getId() == 0) {
        	dto.setId(model.getId());
        }
        return dto;
    }
    
    @Override
    public void delete(Integer id) {
    	delete(Collections.singletonList(get(id)));
    }
    
    /**
     * 批量删除
     * @param dtoList 数据传输对象列表
     * @return 成功删除的记录数
     * @throws ManageException 删除失败时抛出
     */
    @Override
    public int delete(List<DTO> dtoList) {
        Assert.notEmpty(dtoList, "The given DTO list must not be empty.");
        List<MODEL> entities = new ArrayList<MODEL>();
        for (DTO dto : dtoList) {
            MODEL entity = BeanUtils.instantiate(modelClass);
            entity.setId(dto.getId());
            entities.add(entity);
        }
        try {
        	modelDao.deleteInBatch(entities);
            return dtoList.size();
        } catch (DataIntegrityViolationException e) {
            throw new ManageException("存在关联数据，请先删除关联数据");
        }
        
    }
    
    public Class<DTO> getDtoClass() {
        return dtoClass;
    }

    public Class<MODEL> getModelClass() {
        return modelClass;
    }

}
