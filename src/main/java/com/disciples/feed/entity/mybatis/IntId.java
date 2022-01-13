package com.disciples.feed.entity.mybatis;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.disciples.feed.entity.AbstractIdentifiable;

public abstract class IntId extends AbstractIdentifiable<Integer> {
    
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @Override
    public Integer getId() {
        return id;
    }
    
    @Override
    public void setId(final Integer id) {
        this.id = id;
    }
    
}
