package com.disciples.feed.dao.jpa;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.disciples.feed.domain.jpa.Book;

public interface BookDao extends JpaRepository<Book, Integer> {

	@EntityGraph("Book.publisher")
	Book getById(Integer id);
	
    @EntityGraph("Book.publisher")
    Page<Book> findByOrderByLastUpdateDesc(Pageable pageable);
    @EntityGraph("Book.publisher")
    Page<Book> findByIsbnContainingOrderByLastUpdateDesc(String keyword, Pageable pageable);
    
    @Query("update Book o set o.ebook = :ebookName where o.id = :bookId")
    @Modifying
    @Transactional
    int updateEbook(@Param("bookId") Integer bookId, @Param("ebookName") String ebookName);

}
