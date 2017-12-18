package com.disciples.feed.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.Store;
import org.wltea.analyzer.lucene.IKAnalyzer;

import com.disciples.feed.BaseEntity;

@SuppressWarnings("serial")
@Entity
@Table(name = "lib_publisher")
@Indexed
@Analyzer(impl = IKAnalyzer.class)
public class Publisher extends BaseEntity {
	
    private String name;
    private String place;
    
    public Publisher() {}
    
    public Publisher(Integer id) {
        this.setId(id);
    }
    
    public Publisher(String name, String place) {
		this.name = name;
		this.place = place;
	}

	public Publisher(String name) {
        this.name = name;
    }

    @Column(length = 40, nullable = false)
    @Field(store = Store.YES)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Column(length = 32)
    @Field(store = Store.YES)
	public String getPlace() {
		return place;
	}

	public void setPlace(String place) {
		this.place = place;
	}

}
