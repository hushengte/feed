package com.disciples.feed.entity.mybatis;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.disciples.feed.entity.AbstractIdentifiable;

public abstract class LongId extends AbstractIdentifiable<Long> {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Override
    public Long getId() {
        return id;
    }
    
    @Override
    public void setId(final Long id) {
        this.id = id;
    }
    
}
