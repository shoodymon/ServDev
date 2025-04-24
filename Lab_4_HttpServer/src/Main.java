import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
    private static final int PORT = 8080;
    private static final int BUFFER_SIZE = 8192;

    // Храним буферы и состояние для каждого соединения
    private static final Map<SocketChannel, ClientState> clientStates = new HashMap<>();

    // Шаблон для разбора первой строки HTTP-запроса
    private static final Pattern REQUEST_LINE_PATTERN =
            Pattern.compile("^(GET|POST|PUT|DELETE|HEAD|OPTIONS) ([^ ]+) (HTTP/\\d\\.\\d)$");

    public static void main(String[] args) throws IOException {
        // Создаем неблокирующий серверный сокет
        ServerSocketChannel serverSocket = ServerSocketChannel.open();
        serverSocket.bind(new InetSocketAddress(PORT));
        serverSocket.configureBlocking(false);

        // Создаем селектор для обработки событий
        Selector selector = Selector.open();
        serverSocket.register(selector, SelectionKey.OP_ACCEPT);

        System.out.println("HTTP-сервер запущен на порту " + PORT);

        try {
            while (true) {
                if (selector.select() > 0) {
                    var selectedKeys = selector.selectedKeys();
                    var iterator = selectedKeys.iterator();

                    while (iterator.hasNext()) {
                        SelectionKey key = iterator.next();
                        iterator.remove();

                        if (!key.isValid()) {
                            continue;
                        }

                        if (key.isAcceptable()) {
                            acceptConnection(selector, serverSocket);
                        } else if (key.isReadable()) {
                            readRequest(key);
                        } else if (key.isWritable()) {
                            writeResponse(key);
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Ошибка работы сервера: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Закрываем все ресурсы
            for (var client : clientStates.keySet()) {
                try {
                    client.close();
                } catch (IOException e) {
                    // Игнорируем
                }
            }
            clientStates.clear();

            try {
                serverSocket.close();
                selector.close();
            } catch (IOException e) {
                // Игнорируем
            }
        }
    }

    private static void acceptConnection(Selector selector, ServerSocketChannel serverSocket) throws IOException {
        SocketChannel client = serverSocket.accept();
        if (client != null) {
            client.configureBlocking(false);
            SelectionKey key = client.register(selector, SelectionKey.OP_READ);

            // Создаем буфер и состояние для нового клиента
            ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
            clientStates.put(client, new ClientState(buffer));

            System.out.println("Новое соединение: " + client.getRemoteAddress());
        }
    }

    private static void readRequest(SelectionKey key) throws IOException {
        SocketChannel client = (SocketChannel) key.channel();
        ClientState state = clientStates.get(client);

        if (state == null) {
            key.cancel();
            client.close();
            return;
        }

        ByteBuffer buffer = state.getBuffer();

        // Готовим буфер для чтения с канала
        buffer.compact();

        int bytesRead = client.read(buffer);

        if (bytesRead == -1) {
            // Клиент закрыл соединение
            closeConnection(key);
            return;
        }

        if (bytesRead > 0) {
            // Готовим буфер для чтения из него
            buffer.flip();

            // Пытаемся распарсить HTTP-запрос
            if (parseHttpRequest(state, buffer)) {
                // Запрос полностью прочитан, подготавливаем ответ
                prepareResponse(state, client);

                // Переключаемся на запись
                key.interestOps(SelectionKey.OP_WRITE);
            }
        }
    }

    private static boolean parseHttpRequest(ClientState state, ByteBuffer buffer) {
        // Если заголовки еще не прочитаны
        if (!state.isHeadersParsed()) {
            // Преобразуем буфер в строку для парсинга
            String content = new String(buffer.array(), 0, buffer.limit());

            // Ищем конец заголовков (пустая строка, отделяющая заголовки от тела)
            int headerEnd = content.indexOf("\r\n\r\n");

            if (headerEnd >= 0) {
                // Парсим заголовки
                String[] headers = content.substring(0, headerEnd).split("\r\n");

                if (headers.length > 0) {
                    // Парсим первую строку запроса
                    Matcher matcher = REQUEST_LINE_PATTERN.matcher(headers[0]);
                    if (matcher.find()) {
                        state.setMethod(matcher.group(1));
                        state.setPath(matcher.group(2));
                        state.setHttpVersion(matcher.group(3));

                        // Парсим остальные заголовки
                        for (int i = 1; i < headers.length; i++) {
                            String header = headers[i];
                            int colonIndex = header.indexOf(':');
                            if (colonIndex > 0) {
                                String name = header.substring(0, colonIndex).trim();
                                String value = header.substring(colonIndex + 1).trim();
                                state.getHeaders().put(name, value);
                            }
                        }

                        // Отмечаем, что заголовки прочитаны
                        state.setHeadersParsed(true);

                        // Если есть тело, то определяем его длину
                        if (state.getHeaders().containsKey("Content-Length")) {
                            try {
                                int contentLength = Integer.parseInt(state.getHeaders().get("Content-Length"));
                                state.setContentLength(contentLength);

                                // Проверяем, есть ли уже часть тела в буфере
                                if (buffer.limit() > headerEnd + 4) {
                                    // Копируем тело запроса в отдельный буфер
                                    int bodyStart = headerEnd + 4;
                                    int bodyLength = buffer.limit() - bodyStart;

                                    if (bodyLength > 0) {
                                        ByteBuffer bodyBuffer = ByteBuffer.allocate(contentLength);
                                        bodyBuffer.put(buffer.array(), bodyStart, bodyLength);
                                        state.setBodyBuffer(bodyBuffer);

                                        // Если тело полностью прочитано, то возвращаем true
                                        return bodyLength >= contentLength;
                                    }
                                }

                                // Тело еще не прочитано полностью
                                return state.getContentLength() == 0;
                            } catch (NumberFormatException e) {
                                // Ошибка парсинга Content-Length
                                state.setStatusCode(400);
                                return true;
                            }
                        } else {
                            // Нет тела запроса
                            return true;
                        }
                    } else {
                        // Некорректная первая строка запроса
                        state.setStatusCode(400);
                        return true;
                    }
                }
            }

            // Заголовки еще не дочитаны
            return false;
        } else if (state.getContentLength() > 0) {
            // Читаем тело запроса
            ByteBuffer bodyBuffer = state.getBodyBuffer();
            if (bodyBuffer == null) {
                bodyBuffer = ByteBuffer.allocate(state.getContentLength());
                state.setBodyBuffer(bodyBuffer);
            }

            // Копируем данные из входного буфера в буфер тела
            int remaining = bodyBuffer.remaining();
            if (remaining > 0) {
                int toCopy = Math.min(remaining, buffer.remaining());
                byte[] tmp = new byte[toCopy];
                buffer.get(tmp);
                bodyBuffer.put(tmp);

                // Проверяем, полностью ли прочитано тело
                return !bodyBuffer.hasRemaining();
            }

            return true;
        }

        return true;
    }

    private static void prepareResponse(ClientState state, SocketChannel client) throws IOException {
        // Логируем запрос
        System.out.println("\n--- " + new java.util.Date() + " ---");
        System.out.println(state.getMethod() + " " + state.getPath() + " " + state.getHttpVersion());

        // Подготавливаем ответ
        String content;

        if (state.getStatusCode() == 200) {
            // Обработка маршрутов в зависимости от пути
            if ("/".equals(state.getPath())) {
                // Главная страница с информацией о сервере
                content = "<html><body style='font-family: Arial, sans-serif; margin: 20px; max-width: 800px;'>"
                        + "<h1>Привет от NIO HTTP-сервера!</h1>"
                        + "<p>Это демонстрационный неблокирующий HTTP-сервер на Java NIO.</p>"

                        + "<h2>Информация о запросе:</h2>"
                        + "<ul>"
                        + "<li><strong>Метод:</strong> " + state.getMethod() + "</li>"
                        + "<li><strong>Путь:</strong> " + state.getPath() + "</li>"
                        + "<li><strong>HTTP-версия:</strong> " + state.getHttpVersion() + "</li>"
                        + "<li><strong>Удаленный адрес:</strong> " + client.getRemoteAddress() + "</li>"
                        + "<li><strong>Время обработки:</strong> " + (System.currentTimeMillis() - state.getStartTime()) + " мс</li>"
                        + "</ul>"

                        + "<h2>Заголовки запроса:</h2>"
                        + "<table border='1' cellpadding='5' style='border-collapse: collapse;'>"
                        + "<tr><th>Заголовок</th><th>Значение</th></tr>";

                for (Map.Entry<String, String> header : state.getHeaders().entrySet()) {
                    content += "<tr><td>" + header.getKey() + "</td><td>" + header.getValue() + "</td></tr>";
                }

                content += "</table>"

                        + "<h2>Статистика сервера:</h2>"
                        + "<ul>"
                        + "<li><strong>Активных соединений:</strong> " + clientStates.size() + "</li>"
                        + "<li><strong>Свободная память:</strong> " + (Runtime.getRuntime().freeMemory() / (1024 * 1024)) + " МБ</li>"
                        + "<li><strong>Общая память:</strong> " + (Runtime.getRuntime().totalMemory() / (1024 * 1024)) + " МБ</li>"
                        + "<li><strong>Доступных процессоров:</strong> " + Runtime.getRuntime().availableProcessors() + "</li>"
                        + "</ul>"

                        + "<h2>Тестовые маршруты:</h2>"
                        + "<ul>"
                        + "<li><a href='/echo?param=test&another=value'>Echo API</a> - возвращает все данные запроса</li>"
                        + "<li><a href='/delay'>Задержка</a> - имитация длительной обработки (3 секунды)</li>"
                        + "<li><a href='/json'>JSON API</a> - пример ответа в формате JSON</li>"
                        + "<li><a href='/stream'>Большой ответ</a> - генерация большого ответа</li>"
                        + "</ul>"

                        + "<h2>Тестирование POST-запроса:</h2>"
                        + "<form action='/echo' method='POST' style='border: 1px solid #ccc; padding: 15px; border-radius: 5px;'>"
                        + "<div style='margin-bottom: 10px;'>"
                        + "<label for='name'>Имя:</label><br>"
                        + "<input type='text' id='name' name='name' value='Пользователь' style='width: 300px; padding: 5px;'>"
                        + "</div>"
                        + "<div style='margin-bottom: 10px;'>"
                        + "<label for='message'>Сообщение:</label><br>"
                        + "<textarea id='message' name='message' style='width: 300px; height: 100px; padding: 5px;'>Тестовое сообщение</textarea>"
                        + "</div>"
                        + "<input type='submit' value='Отправить POST' style='padding: 8px 15px; background-color: #4CAF50; color: white; border: none; cursor: pointer;'>"
                        + "</form>"

                        + "</body></html>";

            } else if ("/echo".equals(state.getPath()) || state.getPath().startsWith("/echo?")) {
                // Echo API - возвращает все данные запроса
                content = "<html><body style='font-family: Arial, sans-serif; margin: 20px;'>"
                        + "<h1>Echo API</h1>"
                        + "<p><a href='/'>← Вернуться на главную</a></p>"

                        + "<h2>Информация о запросе:</h2>"
                        + "<pre style='background-color: #f5f5f5; padding: 15px; border-radius: 5px;'>"
                        + "Метод: " + state.getMethod() + "\n"
                        + "Путь: " + state.getPath() + "\n"
                        + "HTTP версия: " + state.getHttpVersion() + "\n"
                        + "</pre>"

                        + "<h2>Заголовки запроса:</h2>"
                        + "<pre style='background-color: #f5f5f5; padding: 15px; border-radius: 5px;'>";

                for (Map.Entry<String, String> header : state.getHeaders().entrySet()) {
                    content += header.getKey() + ": " + header.getValue() + "\n";
                }

                content += "</pre>";

                // Разбор параметров запроса
                if (state.getPath().contains("?")) {
                    content += "<h2>Параметры запроса:</h2>"
                            + "<pre style='background-color: #f5f5f5; padding: 15px; border-radius: 5px;'>";

                    String query = state.getPath().substring(state.getPath().indexOf('?') + 1);
                    String[] params = query.split("&");
                    for (String param : params) {
                        String[] keyValue = param.split("=", 2);
                        if (keyValue.length == 2) {
                            content += keyValue[0] + ": " + keyValue[1] + "\n";
                        } else {
                            content += keyValue[0] + "\n";
                        }
                    }

                    content += "</pre>";
                }

                // Тело запроса, если есть
                if (state.getBodyBuffer() != null && state.getBodyBuffer().position() > 0) {
                    content += "<h2>Тело запроса:</h2>"
                            + "<pre style='background-color: #f5f5f5; padding: 15px; border-radius: 5px;'>";
                    ByteBuffer bodyBuffer = state.getBodyBuffer();
                    bodyBuffer.flip();
                    byte[] bodyBytes = new byte[bodyBuffer.limit()];
                    bodyBuffer.get(bodyBytes);
                    content += new String(bodyBytes);
                    content += "</pre>";

                    // Если это POST-запрос формы, расшифруем его
                    if ("POST".equals(state.getMethod()) &&
                            state.getHeaders().getOrDefault("Content-Type", "").contains("application/x-www-form-urlencoded")) {

                        content += "<h2>Данные формы:</h2>"
                                + "<table border='1' cellpadding='5' style='border-collapse: collapse;'>"
                                + "<tr><th>Поле</th><th>Значение</th></tr>";

                        String formData = new String(bodyBytes);
                        String[] pairs = formData.split("&");
                        for (String pair : pairs) {
                            String[] keyValue = pair.split("=", 2);
                            if (keyValue.length == 2) {
                                try {
                                    String key = java.net.URLDecoder.decode(keyValue[0], "UTF-8");
                                    String value = java.net.URLDecoder.decode(keyValue[1], "UTF-8");
                                    content += "<tr><td>" + key + "</td><td>" + value + "</td></tr>";
                                } catch (Exception e) {
                                    content += "<tr><td>" + keyValue[0] + "</td><td>" + keyValue[1] + "</td></tr>";
                                }
                            }
                        }

                        content += "</table>";
                    }
                }

                content += "</body></html>";

            } else if ("/delay".equals(state.getPath())) {
                // Имитация длительной обработки
                try {
                    Thread.sleep(3000); // 3 секунды задержки
                } catch (InterruptedException e) {
                    // Игнорируем
                }

                content = "<html><body style='font-family: Arial, sans-serif; margin: 20px;'>"
                        + "<h1>Delayed Response</h1>"
                        + "<p><a href='/'>← Вернуться на главную</a></p>"
                        + "<p>Этот ответ был задержан на 3 секунды.</p>"
                        + "<p>Благодаря неблокирующему характеру NIO-сервера, задержка на этом маршруте "
                        + "не блокирует обработку других запросов. Вы можете открыть несколько вкладок и "
                        + "убедиться, что другие запросы обрабатываются параллельно.</p>"
                        + "</body></html>";

            } else if ("/json".equals(state.getPath())) {
                // Пример JSON API
                content = "{"
                        + "\"status\": \"success\","
                        + "\"message\": \"Это пример JSON ответа\","
                        + "\"timestamp\": " + System.currentTimeMillis() + ","
                        + "\"data\": {"
                        + "\"server\": \"NIO HTTP Server\","
                        + "\"version\": \"1.0\","
                        + "\"features\": [\"non-blocking\", \"scalable\", \"efficient\"]"
                        + "}"
                        + "}";

                // Устанавливаем Content-Type для JSON
                state.setContentType("application/json");

            } else if ("/stream".equals(state.getPath())) {
                // Генерация большого ответа - например, список из 1000 элементов
                StringBuilder largeContent = new StringBuilder();
                largeContent.append("<html><body style='font-family: Arial, sans-serif; margin: 20px;'>");
                largeContent.append("<h1>Большой ответ</h1>");
                largeContent.append("<p><a href='/'>← Вернуться на главную</a></p>");
                largeContent.append("<p>Это пример большого ответа, демонстрирующий работу с большими объемами данных.</p>");
                largeContent.append("<h2>Список 1000 элементов:</h2>");
                largeContent.append("<ul>");

                for (int i = 1; i <= 1000; i++) {
                    largeContent.append("<li>Элемент #").append(i).append(": ");
                    largeContent.append("Smth text.");
                    largeContent.append("Smth info.");
                    largeContent.append("</li>");
                }

                largeContent.append("</ul>");
                largeContent.append("</body></html>");

                content = largeContent.toString();

            } else {
                // Обработка всех остальных путей
                content = "<html><body style='font-family: Arial, sans-serif; margin: 20px;'>"
                        + "<h1>Привет от NIO HTTP-сервера!</h1>"
                        + "<p><a href='/'>← Вернуться на главную</a></p>"
                        + "<p>Вы запросили путь: <code>" + state.getPath() + "</code></p>"
                        + "<p>Метод: <code>" + state.getMethod() + "</code></p>"
                        + "<p>Версия HTTP: <code>" + state.getHttpVersion() + "</code></p>";

                // Выводим заголовки
                content += "<h2>Заголовки запроса:</h2><ul>";
                for (Map.Entry<String, String> header : state.getHeaders().entrySet()) {
                    content += "<li><b>" + header.getKey() + ":</b> " + header.getValue() + "</li>";
                }
                content += "</ul>";

                if (state.getBodyBuffer() != null && state.getBodyBuffer().position() > 0) {
                    content += "<h2>Тело запроса:</h2><pre style='background-color: #f5f5f5; padding: 15px;'>";
                    ByteBuffer bodyBuffer = state.getBodyBuffer();
                    bodyBuffer.flip();
                    byte[] bodyBytes = new byte[bodyBuffer.limit()];
                    bodyBuffer.get(bodyBytes);
                    content += new String(bodyBytes);
                    content += "</pre>";
                }

                content += "</body></html>";
            }
        } else {
            // Ошибочный ответ
            content = "<html><body style='font-family: Arial, sans-serif; margin: 20px;'>"
                    + "<h1>Ошибка " + state.getStatusCode() + " - " + getStatusMessage(state.getStatusCode()) + "</h1>"
                    + "<p>К сожалению, ваш запрос не может быть обработан.</p>"
                    + "<p><a href='/'>← Вернуться на главную</a></p>"
                    + "</body></html>";
        }

        // Формируем HTTP-заголовки ответа
        StringBuilder responseBuilder = new StringBuilder();
        responseBuilder.append("HTTP/1.1 ")
                .append(state.getStatusCode())
                .append(" ")
                .append(getStatusMessage(state.getStatusCode()))
                .append("\r\n");

        // Устанавливаем content-type
        if (state.getContentType() != null) {
            responseBuilder.append("Content-Type: ").append(state.getContentType()).append("\r\n");
        } else {
            responseBuilder.append("Content-Type: text/html; charset=UTF-8\r\n");
        }

        responseBuilder.append("Content-Length: ").append(content.getBytes().length).append("\r\n");

        // Поддержка keep-alive
        if ("keep-alive".equalsIgnoreCase(state.getHeaders().getOrDefault("Connection", ""))) {
            responseBuilder.append("Connection: keep-alive\r\n");
            state.setKeepAlive(true);
        } else {
            responseBuilder.append("Connection: close\r\n");
        }

        // Добавляем Server и Date заголовки
        responseBuilder.append("Server: Java-NIO-HTTP-Server/1.0\r\n");
        responseBuilder.append("Date: ").append(new java.util.Date()).append("\r\n");

        responseBuilder.append("\r\n");
        responseBuilder.append(content);

        // Создаем буфер ответа
        state.setResponseBuffer(ByteBuffer.wrap(responseBuilder.toString().getBytes()));
    }

    private static void writeResponse(SelectionKey key) throws IOException {
        SocketChannel client = (SocketChannel) key.channel();
        ClientState state = clientStates.get(client);

        if (state == null || state.getResponseBuffer() == null) {
            closeConnection(key);
            return;
        }

        ByteBuffer buffer = state.getResponseBuffer();

        // Пишем данные в канал
        client.write(buffer);

        // Проверяем, полностью ли отправлен ответ
        if (!buffer.hasRemaining()) {
            // Ответ полностью отправлен, закрываем соединение
            closeConnection(key);
        }
    }

    private static void closeConnection(SelectionKey key) throws IOException {
        SocketChannel client = (SocketChannel) key.channel();

        System.out.println("Закрытие соединения: " + client.getRemoteAddress());

        // Отменяем ключ, чтобы он был удален из селектора
        key.cancel();

        // Закрываем канал
        client.close();

        // Удаляем состояние клиента
        clientStates.remove(client);
    }

    private static String getStatusMessage(int statusCode) {
        switch (statusCode) {
            case 200: return "OK";
            case 400: return "Bad Request";
            case 404: return "Not Found";
            case 500: return "Internal Server Error";
            default: return "Unknown";
        }
    }

    // Класс для хранения состояния клиентского соединения
    private static class ClientState {
        private ByteBuffer buffer;
        private boolean headersParsed = false;
        private String method;
        private String path;
        private String httpVersion;
        private final Map<String, String> headers = new HashMap<>();
        private int statusCode = 200;
        private int contentLength = 0;
        private ByteBuffer bodyBuffer;
        private ByteBuffer responseBuffer;
        private String contentType = null;
        private long startTime = System.currentTimeMillis();
        private boolean keepAlive = false;

        public ClientState(ByteBuffer buffer) {
            this.buffer = buffer;
        }

        public ByteBuffer getBuffer() {
            return buffer;
        }

        public boolean isHeadersParsed() {
            return headersParsed;
        }

        public void setHeadersParsed(boolean headersParsed) {
            this.headersParsed = headersParsed;
        }

        public String getMethod() {
            return method;
        }

        public void setMethod(String method) {
            this.method = method;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public String getHttpVersion() {
            return httpVersion;
        }

        public void setHttpVersion(String httpVersion) {
            this.httpVersion = httpVersion;
        }

        public Map<String, String> getHeaders() {
            return headers;
        }

        public int getStatusCode() {
            return statusCode;
        }

        public void setStatusCode(int statusCode) {
            this.statusCode = statusCode;
        }

        public int getContentLength() {
            return contentLength;
        }

        public void setContentLength(int contentLength) {
            this.contentLength = contentLength;
        }

        public ByteBuffer getBodyBuffer() {
            return bodyBuffer;
        }

        public void setBodyBuffer(ByteBuffer bodyBuffer) {
            this.bodyBuffer = bodyBuffer;
        }

        public ByteBuffer getResponseBuffer() {
            return responseBuffer;
        }

        public void setResponseBuffer(ByteBuffer responseBuffer) {
            this.responseBuffer = responseBuffer;
        }

        public String getContentType() {
            return contentType;
        }

        public void setContentType(String contentType) {
            this.contentType = contentType;
        }

        public long getStartTime() {
            return startTime;
        }

        public boolean isKeepAlive() {
            return keepAlive;
        }

        public void setKeepAlive(boolean keepAlive) {
            this.keepAlive = keepAlive;
        }

        // Метод для сброса состояния при keep-alive
        public void reset() {
            this.headersParsed = false;
            this.method = null;
            this.path = null;
            this.httpVersion = null;
            this.headers.clear();
            this.statusCode = 200;
            this.contentLength = 0;
            this.bodyBuffer = null;
            this.responseBuffer = null;
            this.contentType = null;
            this.startTime = System.currentTimeMillis();
            this.buffer.clear();
        }
    }
}