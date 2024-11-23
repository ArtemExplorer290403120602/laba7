import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;

@WebServlet("/CapitalServlet")
public class CapitalServlet extends HttpServlet {
    private ServerSocket serverSocket;

    @Override
    public void init() throws ServletException {
        try {
            int port = 12346; // Порт для сокетного сервера
            serverSocket = new ServerSocket(port);
            new Thread(() -> {
                while (true) {
                    try {
                        Socket clientSocket = serverSocket.accept();
                        handleClient(clientSocket);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        } catch (IOException e) {
            throw new ServletException("Не удалось запустить сервер", e);
        }
    }

    private void handleClient(Socket clientSocket) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

            String country = in.readLine(); // Получаем страну от клиента
            String capital = getCapitalFromAPI(country); // Получаем столицу
            out.println(capital); // Отправляем столицу обратно клиенту

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getCapitalFromAPI(String country) {
        try {
            String urlString = "https://restcountries.com/v3.1/name/" + country + "?fullText=true";
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            // Проверяем код ответа
            if (conn.getResponseCode() == 200) {
                StringBuilder response = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                }

                // Обработка JSON ответа
                String jsonResponse = response.toString();
                // Извлечение столицы из JSON
                String capital = parseCapitalFromJSON(jsonResponse);
                return capital;
            } else {
                return "Столица не найдена";
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "Ошибка при получении данных с API";
        }
    }

    private String parseCapitalFromJSON(String jsonResponse) {
        // Извлекаем капитал из JSON
        String[] parts = jsonResponse.split("\"capital\":");
        if (parts.length > 1) {
            String capitalPart = parts[1].split(",")[0].replace("\"", "").trim();
            return capitalPart;
        }
        return "Столица не найдена";
    }

    @Override
    public void destroy() {
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String country = request.getParameter("country");
        String ip = request.getParameter("ip");
        String port = request.getParameter("port");

        // Установка соединения с сокетным сервером
        try (Socket socket = new Socket(ip, Integer.parseInt(port));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            out.println(country); // Отправка названия страны на сервер
            String capital = in.readLine(); // Получение столицы от сервера

            // Передача столицы на страницу результата
            request.setAttribute("capital", capital);
            request.getRequestDispatcher("country.jsp").forward(request, response);
        } catch (IOException e) {
            e.printStackTrace();
            request.setAttribute("capital", "Ошибка подключения к серверу");
            request.getRequestDispatcher("country.jsp").forward(request, response);
        }
    }
}