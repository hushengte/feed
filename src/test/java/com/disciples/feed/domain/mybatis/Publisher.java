package com.disciples.feed.domain.mybatis;

import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.DocumentId;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.Store;

import com.baomidou.mybatisplus.annotation.TableName;
import com.disciples.feed.entity.mybatis.IntId;
import com.disciples.feed.fulltext.ChineseAnalyzer;

@TableName(value = "lib_publisher")
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

    @DocumentId
    @Override
    public Integer getId() {
        return super.getId();
    }

    @Field(store = Store.YES)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Field(store = Store.YES)
	public String getPlace() {
		return place;
	}

	public void setPlace(String place) {
		this.place = place;
	}

}
