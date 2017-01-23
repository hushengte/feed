package com.disciples.feed.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.disciples.feed.domain.Author;

public interface AuthorDao extends JpaRepository<Author, Integer> {

}
