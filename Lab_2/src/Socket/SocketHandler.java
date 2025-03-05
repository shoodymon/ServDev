package Socket;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

public class SocketHandler {
    public static String readMessage(SocketChannel channel) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        int bytesRead = channel.read(buffer);

        if (bytesRead == -1) {
            throw new IOException("Соединение закрыто клиентом");
        }

        buffer.flip();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        return new String(bytes, StandardCharsets.UTF_8).trim();
    }

    public static void sendMessage(SocketChannel channel, String message) throws IOException {
        byte[] messageBytes = (message + "\n").getBytes(StandardCharsets.UTF_8);
        ByteBuffer buffer = ByteBuffer.wrap(messageBytes);
        channel.write(buffer);
    }

    public static String[] parseMessage(String rawMessage) {
        String[] parts = rawMessage.split(":", 2);
        return parts.length == 2 ? parts : new String[]{"", ""};
    }
}