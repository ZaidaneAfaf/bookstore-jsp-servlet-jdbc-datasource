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

public class BookServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_TITLE = "title";
    private static final String COLUMN_AUTHOR = "author";
    private static final String BOOKS_PATH = "/books";
    
    private DataSource dataSource;

    @Override
    public void init() throws ServletException {
        super.init();
        dataSource = MyDataSourceFactory.getDataSource();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Incrémenter le compteur de requêtes
        MetricsServlet.getRequestCounter().increment();
        
        String action = request.getParameter("action");
        
        if (action != null && action.equals("edit")) {
            showEditForm(request, response);
        } else {
            listBooks(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Incrémenter le compteur de requêtes
        MetricsServlet.getRequestCounter().increment();
        
        String action = request.getParameter("action");
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
            e.printStackTrace();
            throw new ServletException("Database error", e);
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
            e.printStackTrace();
            throw new ServletException("Database error", e);
        }
        
        request.setAttribute("book", book);
        request.getRequestDispatcher("/edit.jsp").forward(request, response);
    }

    private void addBook(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String title = request.getParameter(COLUMN_TITLE);
        String author = request.getParameter(COLUMN_AUTHOR);
        
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "INSERT INTO books (title, author) VALUES (?, ?)")) {
            
            statement.setString(1, title);
            statement.setString(2, author);
            statement.executeUpdate();
            
            // Incrémenter le compteur
            MetricsServlet.getBookAddedCounter().increment();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new IOException("Error adding book", e);
        }
        response.sendRedirect(request.getContextPath() + BOOKS_PATH);
    }

    private void updateBook(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
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
            
            // Incrémenter le compteur
            MetricsServlet.getBookUpdatedCounter().increment();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new IOException("Error updating book", e);
        }
        response.sendRedirect(request.getContextPath() + BOOKS_PATH);
    }

    private void deleteBook(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        Long id = Long.parseLong(request.getParameter(COLUMN_ID));
        
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "DELETE FROM books WHERE id=?")) {
            
            statement.setLong(1, id);
            statement.executeUpdate();
            
            // Incrémenter le compteur
            MetricsServlet.getBookDeletedCounter().increment();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new IOException("Error deleting book", e);
        }
        response.sendRedirect(request.getContextPath() + BOOKS_PATH);
    }
}