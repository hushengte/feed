package com.disciples.feed.domain;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedAttributeNode;
import javax.persistence.NamedEntityGraph;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Index;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.hibernate.search.annotations.Store;
import org.wltea.analyzer.lucene.IKAnalyzer;

import com.disciples.feed.BaseEntity;
import com.fasterxml.jackson.annotation.JsonRawValue;

@SuppressWarnings("serial")
@Entity
@Table(name = "lib_book")
@NamedEntityGraph(name = "Book.publisher", attributeNodes = @NamedAttributeNode(value = "publisher"))
@Indexed
@Analyzer(impl = IKAnalyzer.class)
public class Book extends BaseEntity {
    
    private String name;
    private String author;
    private Publisher publisher;
    private String subject;
    private String isbn;
    private String callNumber;
    private String publishYear;
    private String collation;
    private String serialName;
    private String notes;
    private String ebook;
    
    private Date createDate;
    private Date lastUpdate;
    
    public Book() {}
    
    public Book(Integer id) {
        this.setId(id);
    }

    public Book(String name, String author, Publisher publisher) {
        this.name = name;
        this.author = author;
        this.publisher = publisher;
    }

    @Column(nullable = false)
    @Field(store = Store.YES)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Column(length = 512)
    @Field(store = Store.YES)
    @JsonRawValue
    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "publisher_id")
    @IndexedEmbedded(depth = 1)
    public Publisher getPublisher() {
        return publisher;
    }

    public void setPublisher(Publisher publisher) {
        this.publisher = publisher;
    }

    @Column(length = 1024)
    @Field(store = Store.YES)
    @JsonRawValue
    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    @Column(length = 20)
    @Field(index = Index.NO, store = Store.YES)
    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }
    
    @Column(length = 50)
    @Field(index = Index.NO, store = Store.YES)
    public String getCallNumber() {
        return callNumber;
    }

    public void setCallNumber(String callNumber) {
        this.callNumber = callNumber;
    }
    
    @Column(length = 15)
    @Field(index = Index.NO, store = Store.YES)
    public String getPublishYear() {
        return publishYear;
    }

    public void setPublishYear(String publishYear) {
        this.publishYear = publishYear;
    }

    @Column(length = 30)
    public String getCollation() {
        return collation;
    }

    public void setCollation(String collation) {
        this.collation = collation;
    }

    @Column(length = 30)
    @Field(store = Store.YES)
    public String getSerialName() {
        return serialName;
    }

    public void setSerialName(String serialName) {
        this.serialName = serialName;
    }
    
    @Column(length = 200)
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

    @Temporal(TemporalType.TIMESTAMP)
    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    @Temporal(TemporalType.TIMESTAMP)
    public Date getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

}
