package Server;

import Socket.MessageProtocol;
import Socket.SocketHandler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AsyncMessageServer {
    private Selector selector;
    private ServerSocketChannel serverSocketChannel;
    private MessageBroadcaster messageBroadcaster;
    private ScheduledExecutorService scheduledExecutor;

    public AsyncMessageServer() {
        messageBroadcaster = new MessageBroadcaster();
        scheduledExecutor = Executors.newScheduledThreadPool(2);
    }

    public void startServer() throws IOException {
        // Открытие селектора и серверного сокет-канала
        selector = Selector.open();
        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.socket().bind(new InetSocketAddress(ServerConfig.DEFAULT_PORT));
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        // Запуск потока рассылки сообщений
        scheduledExecutor.scheduleAtFixedRate(
                messageBroadcaster::broadcastMessages,
                0,
                ServerConfig.BROADCAST_INTERVAL_SECONDS,
                TimeUnit.SECONDS
        );

        System.out.println("Сервер запущен на порту " + ServerConfig.DEFAULT_PORT);

        try {
            while (true) {
                selector.select();
                Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();

                while (keyIterator.hasNext()) {
                    SelectionKey key = keyIterator.next();
                    keyIterator.remove();

                    if (!key.isValid()) continue;

                    if (key.isAcceptable()) {
                        acceptConnection(key);
                    } else if (key.isReadable()) {
                        readClientMessage(key);
                    }
                }
            }
        } finally {
            closeServer();
        }
    }

    private void acceptConnection(SelectionKey key) throws IOException {
        ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
        SocketChannel clientChannel = serverChannel.accept();
        clientChannel.configureBlocking(false);
        clientChannel.register(selector, SelectionKey.OP_READ);

        messageBroadcaster.addClient(clientChannel);
        System.out.println("Новое подключение: " + clientChannel);
    }

    private void readClientMessage(SelectionKey key) throws IOException {
        SocketChannel clientChannel = (SocketChannel) key.channel();

        try {
            String message = SocketHandler.readMessage(clientChannel);
            processClientMessage(clientChannel, message);
        } catch (IOException e) {
            // Обработка закрытия соединения
            messageBroadcaster.removeClient(clientChannel);
            clientChannel.close();
        }
    }

    private void processClientMessage(SocketChannel clientChannel, String message) throws IOException {
        String[] parts = SocketHandler.parseMessage(message);
        MessageProtocol protocol = MessageProtocol.fromString(parts[0]);
        String payload = parts[1];

        switch (protocol) {
            case SUBSCRIBE:
                messageBroadcaster.subscribeClient(clientChannel, payload);
                SocketHandler.sendMessage(clientChannel, "Подписка на группу: " + payload);
                break;
            case UNSUBSCRIBE:
                messageBroadcaster.unsubscribeClient(clientChannel, payload);
                SocketHandler.sendMessage(clientChannel, "Отписка от группы: " + payload);
                break;
            default:
                SocketHandler.sendMessage(clientChannel, "Неизвестная команда");
        }
    }

    private void closeServer() {
        try {
            if (selector != null) selector.close();
            if (serverSocketChannel != null) serverSocketChannel.close();
            scheduledExecutor.shutdown();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            new AsyncMessageServer().startServer();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
