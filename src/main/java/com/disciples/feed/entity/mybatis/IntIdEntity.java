package com.disciples.feed.entity.mybatis;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Abstract base class for managable entities with a int type id.
 */
@JsonIgnoreProperties({"updateBy"})
public abstract class IntIdEntity extends AbstractManagable<Integer> {
    
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    
    @TableField(value = "create_by", fill = FieldFill.INSERT)
    private Integer createBy;

    @TableField(value = "update_by", fill = FieldFill.INSERT_UPDATE)
    private Integer updateBy;
    
    public IntIdEntity() {
        super();
    }

    @Override
    public Integer getCreateBy() {
        return createBy;
    }

    @Override
    public void setCreateBy(Integer createBy) {
        this.createBy = createBy;
    }
    
    @Override
    public Integer getUpdateBy() {
        return updateBy;
    }

    @Override
    public void setUpdateBy(Integer updateBy) {
        this.updateBy = updateBy;
    }

}
