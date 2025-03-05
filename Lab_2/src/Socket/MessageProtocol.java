package Socket;

public enum MessageProtocol {
    SUBSCRIBE("SUBSCRIBE"),
    UNSUBSCRIBE("UNSUBSCRIBE"),
    MESSAGE("MESSAGE"),
    ERROR("ERROR");

    private final String command;

    MessageProtocol(String command) {
        this.command = command;
    }

    public String getCommand() {
        return command;
    }

    public static MessageProtocol fromString(String command) {
        for (MessageProtocol protocol : values()) {
            if (protocol.command.equalsIgnoreCase(command)) {
                return protocol;
            }
        }
        return ERROR;
    }
}