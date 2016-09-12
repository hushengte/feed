package com.disciples.feed.manage;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.disciples.feed.Identifiable;

/**
 * 此抽象类提供对领域对象的管理操作：如添加，修改，删除，查找等
 * 
 * @author disciples
 * @param <MODEL> 领域对象 (Domain Object)
 */
public abstract class AbstractSimpleCrudService<MODEL extends Identifiable, DAO extends JpaRepository<MODEL, Integer>>
		implements CrudService<MODEL> {
    
    protected static final Logger LOG = LoggerFactory.getLogger(AbstractSimpleCrudService.class);
    
    private final Class<MODEL> modelClass;
    @Autowired
    protected DAO modelDao;
    
    public AbstractSimpleCrudService(Class<MODEL> modelClass) {
        Assert.notNull(modelClass, "The given MODEL class must not be null.");
        this.modelClass = modelClass;
    }
    
    protected Page<MODEL> getPage(Pageable pageable) {
        return modelDao.findAll(pageable);
    }
    
    /**
     * 根据过滤条件查找领域对象
     * @param pageable 分页请求
     * @param filter 过滤条件
     * @return 一页领域对象数据
     */
    protected Page<MODEL> getPage(Pageable pageable, JSONObject filter) {
        throw new UnsupportedOperationException("Please OVERRIDE this method in subclass.");
    }
    
    @Override
    public Page<MODEL> getPage(int page, int size, String filter) {
    	Pageable pageable = new PageRequest(page, size);
        if (!StringUtils.hasText(filter)) {
            return getPage(pageable);
        }
        try {
        	return getPage(pageable, new JSONObject(filter));
        } catch (JSONException e) {
        	throw new ManageException(String.format("filter: '%s' 必须为json格式", filter));
        }
    }
    
    @Override
    public MODEL get(Integer id) {
        return modelDao.findOne(id);
    }
    
    /**
     * 添加或修改功能
     */
    @Override
    public MODEL save(MODEL model) {
    	return modelDao.save(model);
    }
    
    @Override
    public void delete(Integer id) {
        try {
        	modelDao.delete(id);
        } catch (DataIntegrityViolationException e) {
            throw new ManageException("存在关联数据，请先删除关联数据");
        }
    }
    
    /**
     * 批量删除
     * @param modelList domain对象列表
     * @return 成功删除的记录数
     * @throws ManageException 删除失败时抛出
     */
    @Override
    public int delete(List<MODEL> modelList) {
        Assert.notEmpty(modelList, "The given MODEL list must not be empty.");
        try {
        	modelDao.deleteInBatch(modelList);
            return modelList.size();
        } catch (DataIntegrityViolationException e) {
            throw new ManageException("存在关联数据，请先删除关联数据");
        }
        
    }
    
    public Class<MODEL> getModelClass() {
        return modelClass;
    }

}
