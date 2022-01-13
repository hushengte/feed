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
public abstract class IntId extends AbstractIdentifiable<Integer> implements Persistable<Integer> {

    public static final String TABLE_GENERATOR_NAME = "id_increment_generator";
    
    private Integer id;

    @Id
    @org.springframework.data.annotation.Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = TABLE_GENERATOR_NAME)
    @TableGenerator(name = TABLE_GENERATOR_NAME)
    @Override
    public Integer getId() {
        return id;
    }
    
    @Override
    public void setId(final Integer id) {
        this.id = id;
    }
    
    @Override
    @Transient
    @JsonIgnore
    public boolean isNew() {
        return !hasId();
    }
    
}
