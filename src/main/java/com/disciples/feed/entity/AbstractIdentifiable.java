package com.disciples.feed.entity;

import java.io.Serializable;

/**
 * Abstract base class for identifiable entities.
 *
 * @param <ID> the type of the identifier.
 */
public abstract class AbstractIdentifiable<ID extends Serializable> implements Identifiable<ID> {

    @Override
    public boolean hasId() {
        return getId() != null;
    }

    @Override
    public boolean equals(Object obj) {
        if (null == obj) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (!getClass().equals(obj.getClass())) {
            return false;
        }
        AbstractIdentifiable<?> that = (AbstractIdentifiable<?>) obj;
        return null == this.getId() ? false : this.getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        int hashCode = 17;
        hashCode += null == getId() ? 0 : getId().hashCode() * 31;
        return hashCode;
    }
    
    @Override
    public String toString() {
        return String.format("Entity of type %s with id: %s", this.getClass().getName(), getId());
    }
    
}
