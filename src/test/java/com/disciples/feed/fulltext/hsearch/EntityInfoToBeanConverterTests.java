package com.disciples.feed.fulltext.hsearch;

import static org.junit.Assert.assertEquals;

import org.hibernate.search.query.engine.impl.EntityInfoImpl;
import org.junit.Test;

import com.disciples.feed.domain.Book;

public class EntityInfoToBeanConverterTests {

    @Test
    public void testConvert() {
        String[] fields = new String[] {"name", "author"};
        Object[] values = new Object[] {"testname", "testauthor"};
        EntityInfoToBeanConverter<Book> converter = new EntityInfoToBeanConverter<>(Book.class, fields);
        
        Book book = converter.convert(new EntityInfoImpl(Book.class, "id", 1, values));
        assertEquals(values[0], book.getName());
        assertEquals(values[1], book.getAuthor());
    }
    
}
