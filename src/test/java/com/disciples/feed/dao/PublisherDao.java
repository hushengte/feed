package com.disciples.feed.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import com.disciples.feed.domain.Publisher;

public interface PublisherDao extends JpaRepository<Publisher, Integer> {

	Page<Publisher> findByNameContaining(@Param("keyword") String keyword, Pageable pageable);

	Page<Publisher> findByPlaceContaining(@Param("keyword") String keyword, Pageable pageable);

}
