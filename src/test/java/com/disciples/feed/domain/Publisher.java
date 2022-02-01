package com.disciples.feed.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.DocumentId;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.Store;

import com.disciples.feed.domain.jpa.IntId;
import com.disciples.feed.fulltext.ChineseAnalyzer;

@Entity
@Table(name = "lib_publisher")
@Indexed
@Analyzer(impl = ChineseAnalyzer.class)
public class Publisher extends IntId {
	
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

	@Id
    @DocumentId
    @GeneratedValue(strategy = GenerationType.TABLE, generator = TABLE_GENERATOR_NAME)
    @TableGenerator(name = TABLE_GENERATOR_NAME, allocationSize = 50)
    @Override
    public Integer getId() {
        return super.getId();
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
