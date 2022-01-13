package com.disciples.feed.entity.jpa;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.TableGenerator;
import javax.persistence.Transient;

import org.springframework.data.domain.Persistable;

import com.disciples.feed.entity.AbstractIdentifiable;
import com.fasterxml.jackson.annotation.JsonIgnore;

@MappedSuperclass
public abstract class LongId extends AbstractIdentifiable<Long> implements Persistable<Long> {

    public static final String TABLE_GENERATOR_NAME = "id_increment_generator";
    
    private Long id;

    @Id
    @org.springframework.data.annotation.Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = TABLE_GENERATOR_NAME)
    @TableGenerator(name = TABLE_GENERATOR_NAME)
    @Override
    public Long getId() {
        return id;
    }
    
    @Override
    public void setId(Long id) {
        this.id = id;
    }
    
    @Override
    @Transient
    @JsonIgnore
    public boolean isNew() {
        return !hasId();
    }
    
}
