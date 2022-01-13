package com.disciples.feed.entity;

import java.io.Serializable;

/**
 * Simple interface for entities
 * 
 * @param <ID> the type of the identifier
 */
public interface Identifiable<ID extends Serializable> {
    
    /**
     * Returns the id of the entity.
     * 
     * @return the id of the entity
     */
	ID getId();

	/**
     * Set the id of the entity.
     * 
     * @param id the id to set
     */
    void setId(ID id);
    
    /**
     * Returns if the id of {@code Identifiable} is set.
     * 
     * @return if the object has id value
     */
    boolean hasId();
    
}
