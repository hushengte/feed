package com.disciples.feed.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.disciples.feed.domain.Book;

public interface BookDao extends JpaRepository<Book, Integer> {

	Page<Book> findByPublisherId(Integer publisherId, Pageable pageable);

	@Override
	@EntityGraph("Book.authors")
	Book findOne(Integer id);
	
}
