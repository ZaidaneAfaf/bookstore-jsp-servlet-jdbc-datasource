package org.example;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class BookServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_TITLE = "title";
    private static final String COLUMN_AUTHOR = "author";
    private static final String BOOKS_PATH = "/books";
    private static final Logger LOGGER = Logger.getLogger(BookServlet.class.getName());
    
    // Patterns de validation pour la sécurité
    private static final Pattern TITLE_PATTERN = Pattern.compile("^[\\p{L}\\p{N}\\s\\.,!?;:'\"()-]{1,255}$");
    private static final Pattern AUTHOR_PATTERN = Pattern.compile("^[\\p{L}\\p{N}\\s\\.,!?;:'\"()-]{1,255}$");
    private static final Pattern ID_PATTERN = Pattern.compile("^\\d{1,10}$");
    
    private DataSource dataSource;

    @Override
    public void init() throws ServletException {
        super.init();
        dataSource = MyDataSourceFactory.getDataSource();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        MetricsServlet.getRequestCounter().increment();
        
        String action = request.getParameter("action");
        
        try {
            if (action != null && action.equals("edit")) {
                showEditForm(request, response);
            } else {
                listBooks(request, response);
            }
        } catch (ServletException | IOException e) {
            LOGGER.log(Level.SEVERE, "Error processing GET request", e);
            throw e;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        MetricsServlet.getRequestCounter().increment();
        
        String action = request.getParameter("action");
        
        try {
            if (action != null) {
                switch (action) {
                    case "add":
                        addBook(request, response);
                        break;
                    case "update":
                        updateBook(request, response);
                        break;
                    case "delete":
                        deleteBook(request, response);
                        break;
                    default:
                        listBooks(request, response);
                        break;
                }
            } else {
                listBooks(request, response);
            }
        } catch (ServletException | IOException e) {
            LOGGER.log(Level.SEVERE, "Error processing POST request", e);
            throw e;
        }
    }

    private void listBooks(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        List<Book> books = new ArrayList<>();
        String sql = "SELECT id, title, author FROM books ORDER BY id";
        
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            
            while (resultSet.next()) {
                Book book = new Book();
                book.setId(resultSet.getLong(COLUMN_ID));
                book.setTitle(resultSet.getString(COLUMN_TITLE));
                book.setAuthor(resultSet.getString(COLUMN_AUTHOR));
                books.add(book);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error while listing books", e);
            throw new ServletException("Unable to retrieve books from database", e);
        }
        request.setAttribute("books", books);
        request.getRequestDispatcher("/index.jsp").forward(request, response);
    }

    private void showEditForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idParam = request.getParameter(COLUMN_ID);
        
        // Validation de l'ID
        if (idParam == null || !ID_PATTERN.matcher(idParam).matches()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid book ID");
            return;
        }
        
        Long id = Long.parseLong(idParam);
        Book book = null;
        String sql = "SELECT id, title, author FROM books WHERE id=?";
        
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setLong(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    book = new Book();
                    book.setId(resultSet.getLong(COLUMN_ID));
                    book.setTitle(resultSet.getString(COLUMN_TITLE));
                    book.setAuthor(resultSet.getString(COLUMN_AUTHOR));
                } else {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND, "Book not found");
                    return;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error while retrieving book with id: " + id, e);
            throw new ServletException("Unable to retrieve book from database", e);
        } catch (NumberFormatException e) {
            LOGGER.log(Level.WARNING, "Invalid book ID format: " + idParam, e);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid book ID format");
            return;
        }
        
        request.setAttribute("book", book);
        request.getRequestDispatcher("/edit.jsp").forward(request, response);
    }

    private void addBook(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        String title = request.getParameter(COLUMN_TITLE);
        String author = request.getParameter(COLUMN_AUTHOR);
        
        // Validation des entrées
        if (title == null || author == null || 
            !TITLE_PATTERN.matcher(title).matches() || 
            !AUTHOR_PATTERN.matcher(author).matches()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid book data");
            return;
        }
        
        // Échappement HTML pour prévenir XSS
        title = escapeHtml(title);
        author = escapeHtml(author);
        
        String sql = "INSERT INTO books (title, author) VALUES (?, ?)";
        
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setString(1, title);
            statement.setString(2, author);
            int affectedRows = statement.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("Creating book failed, no rows affected.");
            }
            
            MetricsServlet.getBookAddedCounter().increment();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error while adding book: " + title, e);
            throw new ServletException("Unable to add book to database", e);
        }
        response.sendRedirect(request.getContextPath() + BOOKS_PATH);
    }

    private void updateBook(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        String idParam = request.getParameter(COLUMN_ID);
        String title = request.getParameter(COLUMN_TITLE);
        String author = request.getParameter(COLUMN_AUTHOR);
        
        // Validation des entrées
        if (idParam == null || title == null || author == null ||
            !ID_PATTERN.matcher(idParam).matches() ||
            !TITLE_PATTERN.matcher(title).matches() || 
            !AUTHOR_PATTERN.matcher(author).matches()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid book data");
            return;
        }
        
        Long id = Long.parseLong(idParam);
        
        // Échappement HTML pour prévenir XSS
        title = escapeHtml(title);
        author = escapeHtml(author);
        
        String sql = "UPDATE books SET title=?, author=? WHERE id=?";
        
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setString(1, title);
            statement.setString(2, author);
            statement.setLong(3, id);
            int affectedRows = statement.executeUpdate();
            
            if (affectedRows == 0) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Book not found");
                return;
            }
            
            MetricsServlet.getBookUpdatedCounter().increment();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error while updating book with id: " + id, e);
            throw new ServletException("Unable to update book in database", e);
        } catch (NumberFormatException e) {
            LOGGER.log(Level.WARNING, "Invalid book ID format: " + idParam, e);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid book ID format");
            return;
        }
        response.sendRedirect(request.getContextPath() + BOOKS_PATH);
    }

    private void deleteBook(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        String idParam = request.getParameter(COLUMN_ID);
        
        // Validation de l'ID
        if (idParam == null || !ID_PATTERN.matcher(idParam).matches()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid book ID");
            return;
        }
        
        Long id = Long.parseLong(idParam);
        String sql = "DELETE FROM books WHERE id=?";
        
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setLong(1, id);
            int affectedRows = statement.executeUpdate();
            
            if (affectedRows == 0) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Book not found");
                return;
            }
            
            MetricsServlet.getBookDeletedCounter().increment();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error while deleting book with id: " + id, e);
            throw new ServletException("Unable to delete book from database", e);
        } catch (NumberFormatException e) {
            LOGGER.log(Level.WARNING, "Invalid book ID format: " + idParam, e);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid book ID format");
            return;
        }
        response.sendRedirect(request.getContextPath() + BOOKS_PATH);
    }
    
    /**
     * Méthode utilitaire pour échapper les caractères HTML et prévenir les attaques XSS
     */
    private String escapeHtml(String text) {
        if (text == null) {
            return null;
        }
        return text.replace("&", "&amp;")
                  .replace("<", "&lt;")
                  .replace(">", "&gt;")
                  .replace("\"", "&quot;")
                  .replace("'", "&#x27;")
                  .replace("/", "&#x2F;");
    }
}