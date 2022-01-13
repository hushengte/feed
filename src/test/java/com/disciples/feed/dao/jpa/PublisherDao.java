package com.disciples.feed.dao.jpa;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.disciples.feed.domain.jpa.Publisher;

public interface PublisherDao extends JpaRepository<Publisher, Integer> {
    
    @Query("select o.id as key, o.name as value from Publisher o")
    List<Map<String, Object>> getKeyValues();

	Page<Publisher> findByNameContaining(@Param("keyword") String keyword, Pageable pageable);

	Page<Publisher> findByPlaceContaining(@Param("keyword") String keyword, Pageable pageable);

}
