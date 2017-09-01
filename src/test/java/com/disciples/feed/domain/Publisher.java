package com.disciples.feed.domain;

import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.disciples.feed.BaseEntity;

@SuppressWarnings("serial")
@Entity
@Table(name = "fc_publisher")
public class Publisher extends BaseEntity {

	private String name;
	private String place;
	
	private Set<Book> books;
	
	public Publisher() {}

	public Publisher(String name, String place) {
		this.name = name;
		this.place = place;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPlace() {
		return place;
	}

	public void setPlace(String place) {
		this.place = place;
	}

	@OneToMany(mappedBy = "publisher")
	public Set<Book> getBooks() {
		return books;
	}

	public void setBooks(Set<Book> books) {
		this.books = books;
	}
	
}
