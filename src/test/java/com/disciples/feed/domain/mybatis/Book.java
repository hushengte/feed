package com.disciples.feed.domain.mybatis;

import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.DocumentId;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.Store;

import com.baomidou.mybatisplus.annotation.TableName;
import com.disciples.feed.entity.mybatis.IntId;
import com.disciples.feed.fulltext.ChineseAnalyzer;
import com.fasterxml.jackson.annotation.JsonRawValue;

@TableName(value = "lib_book")
@Indexed
@Analyzer(impl = ChineseAnalyzer.class)
public class Book extends IntId {
    
    private String name;
    private String author;
    private Integer publisherId;
    private String subject;
    private String isbn;
    private String callNumber;
    private String publishYear;
    private String collation;
    private String serialName;
    private String notes;
    private String ebook;
    
    public Book() {}
    
    public Book(Integer id) {
        this.setId(id);
    }

    public Book(String name, String author, Integer publisherId) {
        this.name = name;
        this.author = author;
        this.publisherId = publisherId;
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
    @JsonRawValue
    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    @Field(store = Store.YES)
    public Integer getPublisherId() {
        return publisherId;
    }

    public void setPublisherId(Integer publisherId) {
        this.publisherId = publisherId;
    }

    @Field(store = Store.YES)
    @JsonRawValue
    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    @Field(index = Index.NO, store = Store.YES)
    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }
    
    @Field(index = Index.NO, store = Store.YES)
    public String getCallNumber() {
        return callNumber;
    }

    public void setCallNumber(String callNumber) {
        this.callNumber = callNumber;
    }
    
    @Field(index = Index.NO, store = Store.YES)
    public String getPublishYear() {
        return publishYear;
    }

    public void setPublishYear(String publishYear) {
        this.publishYear = publishYear;
    }

    public String getCollation() {
        return collation;
    }

    public void setCollation(String collation) {
        this.collation = collation;
    }

    @Field(store = Store.YES)
    public String getSerialName() {
        return serialName;
    }

    public void setSerialName(String serialName) {
        this.serialName = serialName;
    }
    
    @Field(store = Store.YES)
    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public String getEbook() {
        return ebook;
    }

    public void setEbook(String ebook) {
        this.ebook = ebook;
    }

}
