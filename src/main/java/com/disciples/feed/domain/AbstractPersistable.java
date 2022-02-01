package com.disciples.feed.domain;

import java.io.Serializable;

import org.springframework.data.domain.Persistable;

/**
 * Abstract base class for {@link Persistable} entities.
 *
 * @param <ID> the type of the identifier.
 */
public abstract class AbstractPersistable<ID extends Serializable> implements Persistable<ID> {

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
        AbstractPersistable<?> that = (AbstractPersistable<?>) obj;
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
