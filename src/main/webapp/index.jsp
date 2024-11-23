<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <title>Введите IP и порт сервера</title>
</head>
<body>
<h2>Введите IP и порт сервера</h2>
<form action="country.jsp" method="post">
    <label for="ip">IP:</label>
    <input type="text" id="ip" name="ip" required>
    <label for="port">Порт:</label>
    <input type="number" id="port" name="port" required>
    <input type="submit" value="Подключиться">
</form>
</body>
</html>