package com.disciples.feed.domain;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.disciples.feed.AbstractModel;

@SuppressWarnings("serial")
@Entity
@Table(name = "fc_book_author")
public class BookAuthor extends AbstractModel {

	private Book book;
	private Author author;
	
	public BookAuthor() {}

	public BookAuthor(Book book, Author author) {
		this.book = book;
		this.author = author;
	}

	@ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "book_id")
	public Book getBook() {
		return book;
	}

	public void setBook(Book book) {
		this.book = book;
	}

	@ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "author_id")
	public Author getAuthor() {
		return author;
	}

	public void setAuthor(Author author) {
		this.author = author;
	}
	
}
