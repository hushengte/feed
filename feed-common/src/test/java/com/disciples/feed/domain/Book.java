package com.disciples.feed.domain;

import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedAttributeNode;
import javax.persistence.NamedEntityGraph;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.disciples.feed.AbstractModel;

@SuppressWarnings("serial")
@Entity
@Table(name = "fc_book")
@NamedEntityGraph(name = "Book.authors", attributeNodes = @NamedAttributeNode("authors"))
public class Book extends AbstractModel {

	private String name;
	private String publishYear;
	private Publisher publisher;
	
	private Set<BookAuthor> authors;
	
	public Book() {}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPublishYear() {
		return publishYear;
	}

	public void setPublishYear(String publishYear) {
		this.publishYear = publishYear;
	}

	@ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "publisher_id")
	public Publisher getPublisher() {
		return publisher;
	}

	public void setPublisher(Publisher publisher) {
		this.publisher = publisher;
	}

	@OneToMany(mappedBy = "book")
	public Set<BookAuthor> getAuthors() {
		return authors;
	}

	public void setAuthors(Set<BookAuthor> authors) {
		this.authors = authors;
	}
	
}
