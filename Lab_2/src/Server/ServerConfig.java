package Server;

public class ServerConfig {
    public static final int DEFAULT_PORT = 9000;
    public static final int BROADCAST_INTERVAL_SECONDS = 5;

    // Настройки групп сообщений
    public static final String[] MESSAGE_GROUPS = {"group1", "group2", "group3"};

    // Время жизни сессии
    public static final long SESSION_TIMEOUT_MS = 30 * 60 * 1000; // 30 минут
}