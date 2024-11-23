<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
  <title>Введите страну</title>
</head>
<body>
<h2>Введите страну</h2>
<form action="CapitalServlet" method="post">
  <label for="country">Страна:</label>
  <input type="text" id="country" name="country" required>
  <input type="hidden" name="ip" value="<%= request.getParameter("ip") %>">
  <input type="hidden" name="port" value="<%= request.getParameter("port") %>">
  <input type="submit" value="Получить столицу">
</form>

<% String capital = (String) request.getAttribute("capital"); %>
<% if (capital != null) { %>
<h3>Результат</h3>
<p>Столица: <%= capital %></p>
<% } %>
</body>
</html>