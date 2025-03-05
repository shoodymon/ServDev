package Client;

import java.io.*;
        import java.net.Socket;

public class MessageClientConnection {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    public MessageClientConnection(String host, int port) throws IOException {
        socket = new Socket(host, port);

        // Создаем reader и writer для синхронного обмена сообщениями
        in = new BufferedReader(
                new InputStreamReader(socket.getInputStream())
        );
        out = new PrintWriter(
                new OutputStreamWriter(socket.getOutputStream()),
                true
        );
    }

    public void sendMessage(String message) throws IOException {
        out.println(message);
    }

    public String receiveMessage() throws IOException {
        return in.readLine();
    }

    public void close() throws IOException {
        if (socket != null) socket.close();
    }
}