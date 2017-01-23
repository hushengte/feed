package com.disciples.feed.manage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.util.Assert;

import com.disciples.feed.Identifiable;

/**
 * 此抽象类提供对领域对象的管理操作：如添加，修改，删除，查找等，子类必须实现 DTO 到 MODEL 的相互转换。
 * @param <DTO> 数据传输对象 (Data Transfer Object)
 * @param <MODEL> 领域对象 (Domain Object)
 */
public abstract class AbstractCrudService<DTO extends Identifiable<Integer>, MODEL extends Identifiable<Integer>, DAO extends PagingAndSortingRepository<MODEL, Integer>> 
		implements CrudService<DTO, MODEL, DAO>, Converter<MODEL, DTO> {
    
    protected static final Logger LOG = LoggerFactory.getLogger(AbstractCrudService.class);
    
    private final Class<DTO> dtoClass;
    private final Class<MODEL> modelClass;
    
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
     */
    public DTO toDto(MODEL model) {
    	if (modelClass == dtoClass) {
    		return dtoClass.cast(model);
    	}
    	DTO dto = BeanUtils.instantiate(dtoClass);
        BeanUtils.copyProperties(model, dto);
        return dto;
    }
    
    /**
     * 将Dto对象转换为Model对象，子类可能需要覆盖此方法
     * @param dto
     * @return Model对象
     */
    public MODEL toModel(DTO dto) {
    	if (modelClass == dtoClass) {
    		return modelClass.cast(dto);
    	}
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
    
    /**
     * 根据过滤条件查找数据，子类需要覆盖此方法
     * @param pageable 分页请求
     * @param params 查询条件
     * @return 一页领域对象数据
     */
    protected Page<MODEL> getPage(Pageable pageable, Map<String, Object> params) {
    	return getDao().findAll(pageable);
    }
    
    @Override
    public Page<DTO> find(int page, int size, Map<String, Object> params) {
        Pageable pageable = new PageRequest(page, size);
        if (params == null) {
        	return getPage(pageable, Collections.<String, Object>emptyMap()).map(this);
        }
        return getPage(pageable, params).map(this);
    }
    
    @Override
	public List<DTO> find(Map<String, Object> params) {
    	if (params == null) {
    		return getPage(null, Collections.<String, Object>emptyMap()).map(this).getContent();
    	}
    	return getPage(null, params).map(this).getContent();
	}
    
    @Override
    public DTO findOne(Integer id) {
        return this.toDto(getDao().findOne(id));
    }
    
    @Override
    public DTO save(DTO dto) {
    	dto.setId(getDao().save(this.toModel(dto)).getId());
        return dto;
    }
    
    @Override
    public void delete(Integer id) {
    	delete(Collections.singletonList(Collections.<String, Object>singletonMap("id", id)));
    }
    
    public Class<DTO> getDtoClass() {
        return dtoClass;
    }

    public Class<MODEL> getModelClass() {
        return modelClass;
    }

}
