package com.disciples.feed.manage;

import org.springframework.data.jpa.repository.JpaRepository;

import com.disciples.feed.Identifiable;

public abstract class AbstractJpaSimpleCrudService<MODEL extends Identifiable<Integer>, DAO extends JpaRepository<MODEL, Integer>> 
		extends AbstractJpaCrudService<MODEL, MODEL, DAO> {

	public AbstractJpaSimpleCrudService(Class<MODEL> modelClass) {
		super(modelClass, modelClass);
	}

}
