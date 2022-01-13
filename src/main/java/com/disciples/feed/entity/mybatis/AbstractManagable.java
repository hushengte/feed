package com.disciples.feed.entity.mybatis;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.disciples.feed.entity.AbstractIdentifiable;
import com.disciples.feed.entity.Managable;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Abstract base class for managable entities.
 *
 * @param <ID> the type of the managing type's identifier.
 */
@JsonIgnoreProperties({"updateTime", "deleted"})
public abstract class AbstractManagable<ID extends Serializable> extends AbstractIdentifiable<ID> implements 
        Managable<ID, ID, LocalDateTime> {
    
    private static final Integer NOT_DELETED = 0;
    private static final Integer DELETED = 1;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;
    
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;
    
    @TableLogic
    private Integer deleted;
    
    public AbstractManagable() {
        this.createTime = new Date();
        this.updateTime = this.createTime;
        this.deleted = NOT_DELETED;
    }

    @Override
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    public LocalDateTime getCreateTime() {
        return createTime != null ? 
                LocalDateTime.ofInstant(createTime.toInstant(), ZoneId.systemDefault()) : null;
    }

    @Override
    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = Date.from(createTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    @Override
    public LocalDateTime getUpdateTime() {
        return updateTime != null ? 
                LocalDateTime.ofInstant(updateTime.toInstant(), ZoneId.systemDefault()) : null;
    }

    @Override
    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = Date.from(updateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    public boolean deleted() {
        return deleted != null ? deleted.equals(DELETED) : false;
    }
    
}
