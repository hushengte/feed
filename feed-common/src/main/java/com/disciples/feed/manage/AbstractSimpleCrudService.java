package com.disciples.feed.manage;

import org.springframework.data.repository.PagingAndSortingRepository;

import com.disciples.feed.Identifiable;

/**
 * 此抽象类提供对简单的（没有关联对象的）领域对象的管理操作
 * @param <MODEL> 领域对象 (Domain Object)
 */
public abstract class AbstractSimpleCrudService<MODEL extends Identifiable<Integer>, DAO extends PagingAndSortingRepository<MODEL, Integer>> 
		extends AbstractCrudService<MODEL, MODEL, DAO> {
    
    public AbstractSimpleCrudService(Class<MODEL> modelClass) {
    	super(modelClass, modelClass);
    }
    
}
