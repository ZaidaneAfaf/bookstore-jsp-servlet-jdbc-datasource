package org.example;

import org.junit.Test;
import static org.junit.Assert.*;

public class BookTest {
    
    @Test
    public void testBookGettersAndSetters() {
        Book book = new Book();
        
        book.setId(1L);
        book.setTitle("Test Book");
        book.setAuthor("Test Author");
        
        // CORRECTION : Si getId() retourne Long (objet)
        assertEquals(Long.valueOf(1L), book.getId());
        assertEquals("Test Book", book.getTitle());
        assertEquals("Test Author", book.getAuthor());
    }
    
    @Test
    public void testBookCreation() {
        Book book = new Book();
        assertNotNull(book);
    }
    
    @Test
    public void testBookTitle() {
        Book book = new Book();
        book.setTitle("Sample Title");
        assertNotNull(book.getTitle());
        assertEquals("Sample Title", book.getTitle());
    }
    
    @Test
    public void testBookAuthor() {
        Book book = new Book();
        book.setAuthor("Sample Author");
        assertNotNull(book.getAuthor());
        assertEquals("Sample Author", book.getAuthor());
    }
    
    @Test
    public void testBookId() {
        Book book = new Book();
        book.setId(100L);
        assertNotNull(book.getId());
        // CORRECTION : Si getId() retourne Long (objet)
        assertEquals(Long.valueOf(100L), book.getId());
    }
}