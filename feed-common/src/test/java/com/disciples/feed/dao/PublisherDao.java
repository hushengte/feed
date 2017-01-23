package com.disciples.feed.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.disciples.feed.domain.Publisher;

public interface PublisherDao extends JpaRepository<Publisher, Integer> {

}
