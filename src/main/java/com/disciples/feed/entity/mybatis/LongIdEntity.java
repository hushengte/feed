package com.disciples.feed.entity.mybatis;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Abstract base class for managable entities with a long type id.
 */
@JsonIgnoreProperties({"updateBy"})
public abstract class LongIdEntity extends AbstractManagable<Long> {
    
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    @TableField(value = "create_by", fill = FieldFill.INSERT)
    private Long createBy;

    @TableField(value = "update_by", fill = FieldFill.INSERT_UPDATE)
    private Long updateBy;
    
    public LongIdEntity() {
        super();
    }

    @Override
    public Long getCreateBy() {
        return createBy;
    }

    @Override
    public void setCreateBy(Long createBy) {
        this.createBy = createBy;
    }
    
    @Override
    public Long getUpdateBy() {
        return updateBy;
    }

    @Override
    public void setUpdateBy(Long updateBy) {
        this.updateBy = updateBy;
    }

}
