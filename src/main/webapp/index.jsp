<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<%@ page import="java.util.List" %>
<%@ page import="org.example.Book" %>
<%
    List<Book> books = (List<Book>) request.getAttribute("books");
    if (books == null) {
        response.sendRedirect(request.getContextPath() + "/books");
        return;
    }
%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<link rel="stylesheet" type="text/css" href="css/style.css">
<title>Book List</title>
<style>
    body {
        font-family: Arial, sans-serif;
        max-width: 1000px;
        margin: 50px auto;
        padding: 20px;
        background-color: #f5f5f5;
    }
    h1 {
        color: #333;
        text-align: center;
    }
    h2 {
        color: #555;
        margin-top: 40px;
    }
    .book-table {
        width: 100%;
        border-collapse: collapse;
        background-color: white;
        box-shadow: 0 2px 4px rgba(0,0,0,0.1);
        margin-bottom: 30px;
    }
    .book-table th {
        background-color: #4CAF50;
        color: white;
        padding: 12px;
        text-align: left;
    }
    .book-table td {
        padding: 12px;
        border-bottom: 1px solid #ddd;
    }
    .book-table tr:hover {
        background-color: #f5f5f5;
    }
    .book-form {
        background-color: white;
        padding: 30px;
        border-radius: 8px;
        box-shadow: 0 2px 8px rgba(0,0,0,0.1);
        max-width: 600px;
    }
    .form-group {
        margin-bottom: 20px;
    }
    .book-form label {
        display: block;
        margin-bottom: 8px;
        color: #333;
        font-weight: 600;
        font-size: 14px;
    }
    .book-form input[type="text"] {
        width: 100%;
        padding: 12px 15px;
        border: 2px solid #e0e0e0;
        border-radius: 5px;
        box-sizing: border-box;
        font-size: 15px;
        transition: border-color 0.3s;
    }
    .book-form input[type="text"]:focus {
        outline: none;
        border-color: #4CAF50;
    }
    .book-form input[type="submit"] {
        margin-top: 10px;
        background-color: #4CAF50;
        color: white;
        padding: 12px 30px;
        border: none;
        border-radius: 5px;
        cursor: pointer;
        font-size: 16px;
        font-weight: 600;
        transition: background-color 0.3s;
    }
    .book-form input[type="submit"]:hover {
        background-color: #45a049;
    }
    .action-buttons {
        display: flex;
        gap: 8px;
        align-items: center;
    }
    .btn {
        padding: 8px 16px;
        border: none;
        border-radius: 5px;
        cursor: pointer;
        text-decoration: none;
        font-size: 14px;
        display: inline-block;
        white-space: nowrap;
    }
    .btn-edit {
        background-color: #2196F3;
        color: white;
    }
    .btn-edit:hover {
        background-color: #0b7dda;
    }
    .btn-delete {
        background-color: #f44336;
        color: white;
    }
    .btn-delete:hover {
        background-color: #da190b;
    }
    .action-buttons form {
        margin: 0;
        display: inline-block;
    }
</style>
<script>
    function confirmDelete(bookTitle) {
        return confirm('Are you sure you want to delete "' + bookTitle + '"?');
    }
</script>
</head>
<body>
    <h1>📚 Book Management System</h1>
    
    <table class="book-table">
        <thead>
            <tr>
                <th>ID</th>
                <th>Title</th>
                <th>Author</th>
                <th>Actions</th>
            </tr>
        </thead>
        <tbody>
            <%
                if (books.isEmpty()) {
            %>
            <tr>
                <td colspan="4" style="text-align: center; color: #999;">No books available. Add one below!</td>
            </tr>
            <%
                } else {
                    for (Book book : books) {
            %>
            <tr>
                <td><%= book.getId() %></td>
                <td><%= book.getTitle() %></td>
                <td><%= book.getAuthor() %></td>
                <td>
                    <div class="action-buttons">
                        <a href="books?action=edit&id=<%= book.getId() %>" class="btn btn-edit">✏️ Edit</a>
                        <form action="books" method="POST" onsubmit="return confirmDelete('<%= book.getTitle() %>')">
                            <input type="hidden" name="id" value="<%= book.getId() %>">
                            <input type="hidden" name="action" value="delete">
                            <button type="submit" class="btn btn-delete">🗑️ Delete</button>
                        </form>
                    </div>
                </td>
            </tr>
            <%
                    }
                }
            %>
        </tbody>
    </table>

    <h2>➕ Add New Book</h2>
    <form action="books" method="POST" class="book-form">
        <label>Title:</label>
        <input type="text" name="title" required placeholder="Enter book title">
        
        <label>Author:</label>
        <input type="text" name="author" required placeholder="Enter author name">
        
        <input type="hidden" name="action" value="add">
        <input type="submit" value="Add Book">
    </form>
</body>
</html>