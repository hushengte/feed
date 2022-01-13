package com.disciples.feed.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.context.junit4.SpringRunner;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.disciples.feed.config.MybatisConfig;
import com.disciples.feed.dao.mybatis.BookDao;
import com.disciples.feed.dao.mybatis.PublisherDao;
import com.disciples.feed.domain.mybatis.Book;
import com.disciples.feed.domain.mybatis.Publisher;

@ContextConfiguration(classes = {MybatisConfig.class})
@RunWith(SpringRunner.class)
public class MybatisDaoTest {
    
    @Autowired
    private BookDao bookDao;
    
    @Autowired
    private PublisherDao publiserDao;
    
    private void cleanData() {
        bookDao.delete(null);
        publiserDao.delete(null);
    }
    
    @Test
    @Sql(value = "/data-jdbc.sql", config = @SqlConfig(encoding = "UTF-8"))
    public void testSelectList() {
        cleanData();
        
        Publisher p1 = new Publisher("Test1", "HangZhou");
        Publisher p2 = new Publisher("Test2", "HangZhou");
        publiserDao.insert(p1);
        publiserDao.insert(p2);
        List<Publisher> publishers = publiserDao.selectList(null);
        assertEquals(2, publishers.size());
        Publisher pp1 = publishers.get(0);
        Publisher pp2 = publishers.get(1);
        assertEquals(p1.getId(), pp1.getId());
        assertEquals(p2.getId(), pp2.getId());
        assertEquals(p1.getName(), pp1.getName());
        assertEquals(p2.getName(), pp2.getName());
    }
    
    private Book createOneBook() {
        Publisher publisher = new Publisher("Eerdmans", "Michigan");
        publiserDao.insert(publisher);
        Book book = new Book();
        book.setName("The Apostolic Preaching");
        book.setPublishYear("1955");
        book.setPublisherId(publisher.getId());
        bookDao.insert(book);
        return book;
    }
    
    @Test
    @Sql(value = "/data-jdbc.sql", config = @SqlConfig(encoding = "UTF-8"))
    public void testSave_Delete_SelectPage() {
        cleanData();
        
        Book book = createOneBook();
        Page<Book> page = new Page<>(0, 10);
        bookDao.selectPage(page, null);
        assertOneBookPage(book, page);
        
        bookDao.selectPage(page, Wrappers.<Book>query().orderByDesc("id"));
        assertOneBookPage(book, page);
        
        bookDao.deleteById(book.getId());
        assertNull(bookDao.selectById(book.getId()));
        
        Integer publisherId = book.getPublisherId();
        publiserDao.deleteById(publisherId);
        assertNull(publiserDao.selectById(publisherId));
    }
    
    private void assertOneBookPage(Book book, Page<Book> pageData) {
        assertEquals(1, pageData.getTotal());
        List<Book> content = pageData.getRecords();
        assertEquals(1, content.size());
        Book find = content.get(0);
        assertEquals(book.getName(), find.getName());
        assertEquals(book.getPublishYear(), find.getPublishYear());
        assertEquals(book.getName(), find.getName());
        assertEquals(book.getPublisherId(), find.getPublisherId());
    }
    
}
