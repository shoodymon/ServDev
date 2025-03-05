package Client;

import Socket.MessageProtocol;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class MessageClient extends JFrame {
    private JTextArea messagesArea;
    private JComboBox<String> groupSelector;
    private MessageClientConnection clientConnection;

    public MessageClient() {
        initializeFrame();
        initializeComponents();
        setupConnection();
    }

    private void initializeFrame() {
        setTitle("Групповой мессенджер");
        setSize(500, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
    }

    private void initializeComponents() {
        // Область сообщений
        messagesArea = new JTextArea();
        messagesArea.setEditable(false);
        messagesArea.setLineWrap(true);
        messagesArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(messagesArea);
        add(scrollPane, BorderLayout.CENTER);

        // Панель управления
        JPanel controlPanel = new JPanel(new FlowLayout());

        // Селектор групп
        groupSelector = new JComboBox<>(ClientConfig.AVAILABLE_GROUPS);
        controlPanel.add(new JLabel("Группа:"));
        controlPanel.add(groupSelector);

        // Кнопки подписки/отписки
        JButton subscribeButton = new JButton("Подписаться");
        JButton unsubscribeButton = new JButton("Отписаться");

        subscribeButton.addActionListener(e -> subscribeToGroup());
        unsubscribeButton.addActionListener(e -> unsubscribeFromGroup());

        controlPanel.add(subscribeButton);
        controlPanel.add(unsubscribeButton);

        add(controlPanel, BorderLayout.SOUTH);
    }

    private void setupConnection() {
        try {
            clientConnection = new MessageClientConnection(
                    ClientConfig.SERVER_HOST,
                    ClientConfig.SERVER_PORT
            );

            // Запуск потока прослушивания сообщений
            new Thread(this::startMessageListener).start();
        } catch (IOException e) {
            showError("Ошибка подключения к серверу: " + e.getMessage());
        }
    }

    private void subscribeToGroup() {
        String selectedGroup = (String) groupSelector.getSelectedItem();
        if (selectedGroup != null) {
            try {
                clientConnection.sendMessage(
                        MessageProtocol.SUBSCRIBE.getCommand() + ":" + selectedGroup
                );
            } catch (IOException e) {
                showError("Ошибка подписки: " + e.getMessage());
            }
        }
    }

    private void unsubscribeFromGroup() {
        String selectedGroup = (String) groupSelector.getSelectedItem();
        if (selectedGroup != null) {
            try {
                clientConnection.sendMessage(
                        MessageProtocol.UNSUBSCRIBE.getCommand() + ":" + selectedGroup
                );
            } catch (IOException e) {
                showError("Ошибка отписки: " + e.getMessage());
            }
        }
    }

    private void startMessageListener() {
        while (true) {
            try {
                String message = clientConnection.receiveMessage();
                // Проверяем, что это сообщение группы
                if (message.startsWith(MessageProtocol.MESSAGE.getCommand() + ":")) {
                    String groupMessage = message.substring(
                            MessageProtocol.MESSAGE.getCommand().length() + 1
                    );

                    // Потокобезопасное обновление UI
                    SwingUtilities.invokeLater(() ->
                            messagesArea.append(groupMessage + "\n")
                    );
                }
            } catch (IOException e) {
                showError("Ошибка получения сообщения: " + e.getMessage());
                break;
            }
        }
    }

    private void showError(String message) {
        SwingUtilities.invokeLater(() ->
                JOptionPane.showMessageDialog(
                        this,
                        message,
                        "Ошибка",
                        JOptionPane.ERROR_MESSAGE
                )
        );
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MessageClient client = new MessageClient();
            client.setVisible(true);
        });
    }
}