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

public class BookServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_TITLE = "title";
    private static final String COLUMN_AUTHOR = "author";
    private static final String BOOKS_PATH = "/books";
    private static final Logger LOGGER = Logger.getLogger(BookServlet.class.getName());
    
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
        } catch (ServletException e) {
            LOGGER.log(Level.SEVERE, "Servlet error processing GET request", e);
            throw e;
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "IO error processing GET request", e);
            throw e;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error processing GET request", e);
            throw new ServletException("Unexpected error occurred", e);
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
        } catch (ServletException e) {
            LOGGER.log(Level.SEVERE, "Servlet error processing POST request", e);
            throw e;
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "IO error processing POST request", e);
            throw e;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error processing POST request", e);
            throw new ServletException("Unexpected error occurred", e);
        }
    }

    private void listBooks(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        List<Book> books = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(
                     "SELECT id, title, author FROM books ORDER BY id")) {
            
            while (resultSet.next()) {
                Book book = new Book();
                book.setId(resultSet.getLong(COLUMN_ID));
                book.setTitle(resultSet.getString(COLUMN_TITLE));
                book.setAuthor(resultSet.getString(COLUMN_AUTHOR));
                books.add(book);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error while listing books: " + e.getMessage(), e);
            throw new ServletException("Unable to retrieve books from database", e);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error while listing books", e);
            throw new ServletException("Unexpected error occurred while listing books", e);
        }
        request.setAttribute("books", books);
        request.getRequestDispatcher("/index.jsp").forward(request, response);
    }

    private void showEditForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Long id = Long.parseLong(request.getParameter(COLUMN_ID));
        Book book = null;
        
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT id, title, author FROM books WHERE id=?")) {
            
            statement.setLong(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    book = new Book();
                    book.setId(resultSet.getLong(COLUMN_ID));
                    book.setTitle(resultSet.getString(COLUMN_TITLE));
                    book.setAuthor(resultSet.getString(COLUMN_AUTHOR));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error while retrieving book with id: " + id, e);
            throw new ServletException("Unable to retrieve book from database", e);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error while retrieving book with id: " + id, e);
            throw new ServletException("Unexpected error occurred while retrieving book", e);
        }
        
        request.setAttribute("book", book);
        request.getRequestDispatcher("/edit.jsp").forward(request, response);
    }

    private void addBook(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        String title = request.getParameter(COLUMN_TITLE);
        String author = request.getParameter(COLUMN_AUTHOR);
        
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "INSERT INTO books (title, author) VALUES (?, ?)")) {
            
            statement.setString(1, title);
            statement.setString(2, author);
            statement.executeUpdate();
            
            MetricsServlet.getBookAddedCounter().increment();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error while adding book: " + title, e);
            throw new ServletException("Unable to add book to database", e);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error while adding book: " + title, e);
            throw new ServletException("Unexpected error occurred while adding book", e);
        }
        response.sendRedirect(request.getContextPath() + BOOKS_PATH);
    }

    private void updateBook(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        Long id = Long.parseLong(request.getParameter(COLUMN_ID));
        String title = request.getParameter(COLUMN_TITLE);
        String author = request.getParameter(COLUMN_AUTHOR);
        
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "UPDATE books SET title=?, author=? WHERE id=?")) {
            
            statement.setString(1, title);
            statement.setString(2, author);
            statement.setLong(3, id);
            statement.executeUpdate();
            
            MetricsServlet.getBookUpdatedCounter().increment();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error while updating book with id: " + id, e);
            throw new ServletException("Unable to update book in database", e);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error while updating book with id: " + id, e);
            throw new ServletException("Unexpected error occurred while updating book", e);
        }
        response.sendRedirect(request.getContextPath() + BOOKS_PATH);
    }

    private void deleteBook(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        Long id = Long.parseLong(request.getParameter(COLUMN_ID));
        
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "DELETE FROM books WHERE id=?")) {
            
            statement.setLong(1, id);
            statement.executeUpdate();
            
            MetricsServlet.getBookDeletedCounter().increment();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database error while deleting book with id: " + id, e);
            throw new ServletException("Unable to delete book from database", e);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error while deleting book with id: " + id, e);
            throw new ServletException("Unexpected error occurred while deleting book", e);
        }
        response.sendRedirect(request.getContextPath() + BOOKS_PATH);
    }
}