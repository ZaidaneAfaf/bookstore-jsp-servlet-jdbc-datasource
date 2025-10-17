package org.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServletTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private RequestDispatcher dispatcher;

    @Mock
    private DataSource dataSource;

    @Mock
    private Connection connection;

    @Mock
    private PreparedStatement preparedStatement;

    @Mock
    private Statement statement;

    @Mock
    private ResultSet resultSet;

    private BookServlet servlet;
    private StringWriter stringWriter;
    private PrintWriter writer;

    @BeforeEach
    void setUp() throws Exception {
        servlet = new BookServlet();
        stringWriter = new StringWriter();
        writer = new PrintWriter(stringWriter);
        
        when(response.getWriter()).thenReturn(writer);
    }

    @Test
    void testInit() throws ServletException {
        try (MockedStatic<MyDataSourceFactory> mockedFactory = mockStatic(MyDataSourceFactory.class)) {
            mockedFactory.when(MyDataSourceFactory::getDataSource).thenReturn(dataSource);
            servlet.init();
            mockedFactory.verify(MyDataSourceFactory::getDataSource);
        }
    }

    @Test
    void testListBooks() throws Exception {
        try (MockedStatic<MyDataSourceFactory> mockedFactory = mockStatic(MyDataSourceFactory.class)) {
            mockedFactory.when(MyDataSourceFactory::getDataSource).thenReturn(dataSource);
            servlet.init();

            when(dataSource.getConnection()).thenReturn(connection);
            when(connection.createStatement()).thenReturn(statement);
            when(statement.executeQuery(anyString())).thenReturn(resultSet);
            when(request.getRequestDispatcher("/index.jsp")).thenReturn(dispatcher);

            when(resultSet.next()).thenReturn(true, false);
            when(resultSet.getLong("id")).thenReturn(1L);
            when(resultSet.getString("title")).thenReturn("Test Book");
            when(resultSet.getString("author")).thenReturn("Test Author");

            servlet.service(request, response);

            verify(request).setAttribute(eq("books"), anyList());
            verify(dispatcher).forward(request, response);
        }
    }

    @Test
    void testShowEditForm() throws Exception {
        try (MockedStatic<MyDataSourceFactory> mockedFactory = mockStatic(MyDataSourceFactory.class)) {
            mockedFactory.when(MyDataSourceFactory::getDataSource).thenReturn(dataSource);
            servlet.init();

            when(request.getParameter("action")).thenReturn("edit");
            when(request.getParameter("id")).thenReturn("1");
            when(request.getMethod()).thenReturn("GET");
            when(dataSource.getConnection()).thenReturn(connection);
            when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
            when(preparedStatement.executeQuery()).thenReturn(resultSet);
            when(request.getRequestDispatcher("/edit.jsp")).thenReturn(dispatcher);

            when(resultSet.next()).thenReturn(true);
            when(resultSet.getLong("id")).thenReturn(1L);
            when(resultSet.getString("title")).thenReturn("Test Book");
            when(resultSet.getString("author")).thenReturn("Test Author");

            servlet.service(request, response);

            verify(request).setAttribute(eq("book"), any(Book.class));
            verify(dispatcher).forward(request, response);
        }
    }

    @Test
    void testAddBook() throws Exception {
        try (MockedStatic<MyDataSourceFactory> mockedFactory = mockStatic(MyDataSourceFactory.class)) {
            mockedFactory.when(MyDataSourceFactory::getDataSource).thenReturn(dataSource);
            servlet.init();

            when(request.getMethod()).thenReturn("POST");
            when(request.getParameter("action")).thenReturn("add");
            when(request.getParameter("title")).thenReturn("New Book");
            when(request.getParameter("author")).thenReturn("New Author");
            when(request.getContextPath()).thenReturn("/bookstore");
            when(dataSource.getConnection()).thenReturn(connection);
            when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
            when(preparedStatement.executeUpdate()).thenReturn(1);

            servlet.service(request, response);

            verify(preparedStatement).setString(1, "New Book");
            verify(preparedStatement).setString(2, "New Author");
            verify(response).sendRedirect("/bookstore/books");
        }
    }

    @Test
    void testUpdateBook() throws Exception {
        try (MockedStatic<MyDataSourceFactory> mockedFactory = mockStatic(MyDataSourceFactory.class)) {
            mockedFactory.when(MyDataSourceFactory::getDataSource).thenReturn(dataSource);
            servlet.init();

            when(request.getMethod()).thenReturn("POST");
            when(request.getParameter("action")).thenReturn("update");
            when(request.getParameter("id")).thenReturn("1");
            when(request.getParameter("title")).thenReturn("Updated Book");
            when(request.getParameter("author")).thenReturn("Updated Author");
            when(request.getContextPath()).thenReturn("/bookstore");
            when(dataSource.getConnection()).thenReturn(connection);
            when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
            when(preparedStatement.executeUpdate()).thenReturn(1);

            servlet.service(request, response);

            verify(preparedStatement).setString(1, "Updated Book");
            verify(preparedStatement).setString(2, "Updated Author");
            verify(preparedStatement).setLong(3, 1L);
            verify(response).sendRedirect("/bookstore/books");
        }
    }

    @Test
    void testDeleteBook() throws Exception {
        try (MockedStatic<MyDataSourceFactory> mockedFactory = mockStatic(MyDataSourceFactory.class)) {
            mockedFactory.when(MyDataSourceFactory::getDataSource).thenReturn(dataSource);
            servlet.init();

            when(request.getMethod()).thenReturn("POST");
            when(request.getParameter("action")).thenReturn("delete");
            when(request.getParameter("id")).thenReturn("1");
            when(request.getContextPath()).thenReturn("/bookstore");
            when(dataSource.getConnection()).thenReturn(connection);
            when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
            when(preparedStatement.executeUpdate()).thenReturn(1);

            servlet.service(request, response);

            verify(preparedStatement).setLong(1, 1L);
            verify(response).sendRedirect("/bookstore/books");
        }
    }

    @Test
    void testListBooksWithSQLException() throws Exception {
        try (MockedStatic<MyDataSourceFactory> mockedFactory = mockStatic(MyDataSourceFactory.class)) {
            mockedFactory.when(MyDataSourceFactory::getDataSource).thenReturn(dataSource);
            servlet.init();

            when(request.getMethod()).thenReturn("GET");
            when(dataSource.getConnection()).thenThrow(new SQLException("DB Error"));

            assertThrows(ServletException.class, () -> {
                servlet.service(request, response);
            });
        }
    }

    @Test
    void testAddBookWithSQLException() throws Exception {
        try (MockedStatic<MyDataSourceFactory> mockedFactory = mockStatic(MyDataSourceFactory.class)) {
            mockedFactory.when(MyDataSourceFactory::getDataSource).thenReturn(dataSource);
            servlet.init();

            when(request.getMethod()).thenReturn("POST");
            when(request.getParameter("action")).thenReturn("add");
            when(request.getParameter("title")).thenReturn("New Book");
            when(request.getParameter("author")).thenReturn("New Author");
            when(dataSource.getConnection()).thenThrow(new SQLException("DB Error"));

            assertThrows(ServletException.class, () -> {
                servlet.service(request, response);
            });
        }
    }

    @Test
    void testDefaultAction() throws Exception {
        try (MockedStatic<MyDataSourceFactory> mockedFactory = mockStatic(MyDataSourceFactory.class)) {
            mockedFactory.when(MyDataSourceFactory::getDataSource).thenReturn(dataSource);
            servlet.init();

            when(request.getMethod()).thenReturn("POST");
            when(request.getParameter("action")).thenReturn("unknown");
            when(dataSource.getConnection()).thenReturn(connection);
            when(connection.createStatement()).thenReturn(statement);
            when(statement.executeQuery(anyString())).thenReturn(resultSet);
            when(request.getRequestDispatcher("/index.jsp")).thenReturn(dispatcher);
            when(resultSet.next()).thenReturn(false);

            servlet.service(request, response);

            verify(dispatcher).forward(request, response);
        }
    }
}