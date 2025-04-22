import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;

public class Main {
    private static final int PORT = 8080;

    public static void main(String[] args) throws IOException {
        // 1. Открываем неблокирующий серверный сокет
        ServerSocketChannel serverSocket = ServerSocketChannel.open();
        serverSocket.bind(new InetSocketAddress(PORT));
        serverSocket.configureBlocking(false);

        // 2. Создаём селектор для обработки событий
        Selector selector = Selector.open();
        serverSocket.register(selector, SelectionKey.OP_ACCEPT);

        System.out.println("Сервер запущен на порту " + PORT);

        while (true) {
            selector.select(); // Ожидаем события
            for (SelectionKey key : selector.selectedKeys()) {
                if (key.isAcceptable()) accept(selector, serverSocket);
                if (key.isReadable()) handleRequest(key);
            }
            selector.selectedKeys().clear();
        }
    }

    private static void accept(Selector selector, ServerSocketChannel serverSocket) throws IOException {
        SocketChannel client = serverSocket.accept(); // Принимаем подключение
        client.configureBlocking(false); // Делаем неблокирующим
        client.register(selector, SelectionKey.OP_READ); // Ждём, когда клиент пришлёт данные
        System.out.println("Новое соединение: " + client.getRemoteAddress());
    }

    private static void handleRequest(SelectionKey key) throws IOException {
        SocketChannel client = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        int bytesRead = client.read(buffer);

        if (bytesRead == -1) {
            client.close();
            return;
        }

        buffer.flip();
        String request = new String(buffer.array(), 0, bytesRead);
        System.out.println("Запрос:\n" + request);

        // Отправляем простой HTTP-ответ
        String response = "HTTP/1.1 200 OK\r\nContent-Length: 13\r\n\r\nHello, Habr!";
        ByteBuffer responseBuffer = ByteBuffer.wrap(response.getBytes());
        client.write(responseBuffer);
        client.close(); // Закрываем соединение
    }
}