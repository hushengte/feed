package com.disciples.feed.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.disciples.feed.domain.BookAuthor;

public interface BookAuthorDao extends JpaRepository<BookAuthor, Integer> {

}
