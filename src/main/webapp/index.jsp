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
<!-- ✅ CORRECTION 1 : Ajout de lang="fr" -->
<html lang="fr">
<head>
<meta charset="UTF-8">
<link rel="stylesheet" type="text/css" href="css/style.css">
<title>Book List</title>
<style>
    body {
        font-family: Arial, sans-serif;
        max-width: 1000px;
        margin: 20px auto;
        padding: 20px;
    }
    h1 {
        color: #000;
        text-align: center;
    }
    h2 {
        color: #000;
        margin-top: 30px;
    }
    .book-table {
        width: 100%;
        border-collapse: collapse;
        margin-bottom: 20px;
    }
    .book-table th {
        background-color: #ddd;
        padding: 10px;
        text-align: left;
        border: 1px solid #ccc;
    }
    .book-table td {
        padding: 10px;
        border: 1px solid #ccc;
    }
    .book-form {
        background-color: #fff;
        padding: 20px;
        max-width: 600px;
    }
    .form-group {
        margin-bottom: 20px;
    }
    .book-form label {
        display: block;
        margin-bottom: 10px;
        font-weight: bold;
    }
    .book-form input[type="text"] {
        width: 100%;
        padding: 8px;
        border: 1px solid #ccc;
        box-sizing: border-box;
        margin-top: 5px;
    }
    .book-form input[type="submit"] {
        margin-top: 15px;
        background-color: #ddd;
        color: black;
        padding: 10px 20px;
        border: 1px solid #ccc;
        cursor: pointer;
    }
    .action-buttons {
        display: flex;
        gap: 5px;
    }
    .btn {
        padding: 5px 10px;
        border: 1px solid #ccc;
        cursor: pointer;
        text-decoration: none;
        font-size: 14px;
        display: inline-block;
        background-color: #eee;
        color: black;
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
    <h1>Book Management System</h1>
    
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
                <td colspan="4" style="text-align: center;">No books available. Add one below!</td>
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
                        <a href="books?action=edit&id=<%= book.getId() %>" class="btn">Edit</a>
                        <form action="books" method="POST" onsubmit="return confirmDelete('<%= book.getTitle() %>')">
                            <input type="hidden" name="id" value="<%= book.getId() %>">
                            <input type="hidden" name="action" value="delete">
                            <button type="submit" class="btn">Delete</button>
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

    <h2>Add New Book</h2>
    <form action="books" method="POST" class="book-form">
        <div class="form-group">
            <!-- ✅ CORRECTION 2 : Ajout de for="title" dans le label -->
            <label for="title">Title:</label>
            <input type="text" id="title" name="title" required placeholder="Enter book title">
        </div>
        
        <div class="form-group">
            <!-- ✅ CORRECTION 3 : Ajout de for="author" dans le label -->
            <label for="author">Author:</label>
            <input type="text" id="author" name="author" required placeholder="Enter author name">
        </div>
        
        <input type="hidden" name="action" value="add">
        <input type="submit" value="Add Book">
    </form>
</body>
</html>