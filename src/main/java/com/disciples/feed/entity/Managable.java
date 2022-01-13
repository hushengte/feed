package com.disciples.feed.entity;

import java.io.Serializable;
import java.time.temporal.TemporalAccessor;

/**
 * Interface for managable entities. Allows storing and retrieving creation and modification information. The changing
 * instance (typically some user) is to be defined by a generics definition.
 *
 * @param <U> the managing type. Typically some kind of user.
 * @param <ID> the type of the managed type's identifier
 * @param <T> the create and update time type.
 */
public interface Managable<U, ID extends Serializable, T extends TemporalAccessor> extends Identifiable<ID> {

    /**
     * Returns the user who created this entity.
     *
     * @return the createBy
     */
    U getCreateBy();

    /**
     * Sets the user who created this entity.
     *
     * @param createBy the creating user to set
     */
    void setCreateBy(U createBy);

    /**
     * Returns the creation time of the entity.
     *
     * @return the createTime
     */
    T getCreateTime();

    /**
     * Sets the creation date of the entity.
     *
     * @param createTime the creation time to set
     */
    void setCreateTime(T createTime);

    /**
     * Returns the user who updated the entity lastly.
     *
     * @return the updateBy
     */
    U getUpdateBy();

    /**
     * Sets the user who updated the entity lastly.
     *
     * @param updateBy the last updating user to set
     */
    void setUpdateBy(U updateBy);

    /**
     * Returns the last updating time of the entity.
     *
     * @return the updateTime
     */
    T getUpdateTime();

    /**
     * Sets the last updating time.
     *
     * @param updateTime the last updating time to set
     */
    void setUpdateTime(T updateTime);
    
}
