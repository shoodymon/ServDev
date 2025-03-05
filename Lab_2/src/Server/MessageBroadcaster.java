package Server;

import Socket.MessageProtocol;
import Socket.SocketHandler;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MessageBroadcaster {
    private final Map<SocketChannel, Set<String>> clientSubscriptions = new ConcurrentHashMap<>();
    private final Map<String, String> groupMessages = new HashMap<>();

    public MessageBroadcaster() {
        // Инициализация групповых сообщений
        for (String group : ServerConfig.MESSAGE_GROUPS) {
            groupMessages.put(group, generateGroupMessage(group));
        }
    }

    public void addClient(SocketChannel channel) {
        clientSubscriptions.put(channel, new HashSet<>());
    }

    public void removeClient(SocketChannel channel) {
        clientSubscriptions.remove(channel);
    }

    public void subscribeClient(SocketChannel channel, String group) {
        clientSubscriptions.get(channel).add(group);
    }

    public void unsubscribeClient(SocketChannel channel, String group) {
        clientSubscriptions.get(channel).remove(group);
    }

    public void broadcastMessages() {
        clientSubscriptions.forEach((channel, groups) -> {
            groups.forEach(group -> {
                if (groupMessages.containsKey(group)) {
                    try {
                        SocketHandler.sendMessage(channel,
                                MessageProtocol.MESSAGE.getCommand() + ":" +
                                        groupMessages.get(group)
                        );
                    } catch (IOException e) {
                        System.err.println("Ошибка при отправке сообщения: " + e.getMessage());
                    }
                }
            });
        });

        // Обновляем сообщения групп
        updateGroupMessages();
    }

    private void updateGroupMessages() {
        for (String group : ServerConfig.MESSAGE_GROUPS) {
            groupMessages.put(group, generateGroupMessage(group));
        }
    }

    private String generateGroupMessage(String group) {
        return String.format("Уведомление для %s от %s",
                group,
                new Date().toString()
        );
    }

    public Map<SocketChannel, Set<String>> getClientSubscriptions() {
        return clientSubscriptions;
    }
}