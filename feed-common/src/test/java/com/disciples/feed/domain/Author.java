package com.disciples.feed.domain;

import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.NamedAttributeNode;
import javax.persistence.NamedEntityGraph;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.disciples.feed.BaseEntity;

@SuppressWarnings("serial")
@Entity
@Table(name = "fc_author")
@NamedEntityGraph(name = "Author.books", attributeNodes = @NamedAttributeNode("books"))
public class Author extends BaseEntity {

	private String name;
	private String level;
	
	private Set<BookAuthor> books;
	
	public Author() {}

	public Author(String name, String level) {
		this.name = name;
		this.level = level;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLevel() {
		return level;
	}

	public void setLevel(String level) {
		this.level = level;
	}

	@OneToMany(mappedBy = "author")
	public Set<BookAuthor> getBooks() {
		return books;
	}

	public void setBooks(Set<BookAuthor> books) {
		this.books = books;
	}

}
