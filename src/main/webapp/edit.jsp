<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<%@ page import="org.example.Book" %>
<%
    Book book = (Book) request.getAttribute("book");
    if (book == null) {
        response.sendRedirect(request.getContextPath() + "/books");
        return;
    }
%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<link rel="stylesheet" type="text/css" href="css/style.css">
<title>Edit Book</title>
</head>
<body>
    <h1>Edit Book</h1>
    
    <form action="books" method="POST" class="book-form">
        <input type="hidden" name="id" value="<%= book.getId() %>">
        <input type="hidden" name="action" value="update">
        
        <label>Title:</label>
        <input type="text" name="title" value="<%= book.getTitle() %>" required>
        
        <label>Author:</label>
        <input type="text" name="author" value="<%= book.getAuthor() %>" required>
        
        <div style="margin-top: 20px;">
            <input type="submit" value="Update Book">
            <a href="books" style="margin-left: 10px; padding: 8px 15px; background-color: #6c757d; color: white; text-decoration: none; border-radius: 4px;">Cancel</a>
        </div>
    </form>
</body>
</html>