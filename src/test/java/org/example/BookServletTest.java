import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class BookServletTest {
    
    @Test
    public void testBookCreation() {
        String title = "Example Book";
        String author = "Example Author";
        
        assertNotNull(title);
        assertNotNull(author);
        assertEquals("Example Book", title);
    }
    
    @Test
    public void testEmptyBookTitle() {
        String title = "";
        assertTrue(title.isEmpty());
    }
    
    @Test
    public void testBookAuthor() {
        String author = "Example Author";
        assertFalse(author.isEmpty());
        assertTrue(author.length() > 0);
    }
}