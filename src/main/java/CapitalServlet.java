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

@WebServlet("/CapitalServlet") // Сервлет, который доступен по URL /CapitalServlet
public class CapitalServlet extends HttpServlet {
    private ServerSocket serverSocket; // Создание сервера сокетов

    @Override
    public void init() throws ServletException {
        try {
            int port = 12346; // Порт для сокетного сервера
            serverSocket = new ServerSocket(port); // Настройка сервера на прослушивание указанного порта
            new Thread(() -> {
                while (true) {
                    try {
                        // Принятие входящего соединения от клиента
                        Socket clientSocket = serverSocket.accept();
                        // Обработка клиента в отдельном потоке
                        handleClient(clientSocket);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start(); // Запуск потока в фоне
        } catch (IOException e) {
            throw new ServletException("Не удалось запустить сервер", e);
        }
    }

    // Метод для обработки запросов от клиентского сокета
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

    // Метод для получения столицы через внешний API
    private String getCapitalFromAPI(String country) {
        try {
            // URL для запроса к API
            String urlString = "https://restcountries.com/v3.1/name/" + country + "?fullText=true";
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection(); // Открытие соединения
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            // Проверка кода ответа
            if (conn.getResponseCode() == 200) { // Код 200 означает успешный ответ
                StringBuilder response = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line); // Считываем ответ
                    }
                }

                // Обработка JSON ответа
                String jsonResponse = response.toString();
                // Извлечение столицы из JSON
                String capital = parseCapitalFromJSON(jsonResponse);
                return capital; // Возвращаем столицу
            } else {
                return "Столица не найдена"; // Если код ответа не 200
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "Ошибка при получении данных с API"; // Ошибка в процессе получения данных
        }
    }

    // Метод для разбора JSON и извлечения столицы
    private String parseCapitalFromJSON(String jsonResponse) {
        // Извлекаем капитал из JSON
        String[] parts = jsonResponse.split("\"capital\":");
        if (parts.length > 1) {
            String capitalPart = parts[1].split(",")[0].replace("\"", "").trim();
            return capitalPart; // Возвращаем капитал
        }
        return "Столица не найдена"; // Если не удалось найти столицу
    }

    @Override
    public void destroy() {
        try {
            // Закрываем серверный сокет при разрушении сервлета
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Метод для обработки POST-запроса
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String country = request.getParameter("country"); // Получаем страну из параметров запроса
        String ip = request.getParameter("ip"); // Получаем IP-адрес сервера сокетов
        String port = request.getParameter("port"); // Получаем порт сервера сокетов

        // Установка соединения с сокетным сервером
        try (Socket socket = new Socket(ip, Integer.parseInt(port)); // Создаем соединение с указанным IP и портом
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