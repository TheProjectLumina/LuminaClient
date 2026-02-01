package org.cloudburstmc.netty.handler.codec.rcon;

public class RconMessage {
    public static final int AUTH = 3;
    public static final int AUTH_RESPONSE = 2;
    public static final int EXECCOMMAND = 2;
    public static final int RESPONSE_VALUE = 0;

    private final int id;
    private final int type;
    private final String body;

    public RconMessage(int id, int type, String body) {
        this.id = id;
        this.type = type;
        this.body = body;
    }

    public int getId() {
        return id;
    }

    public int getType() {
        return type;
    }

    public String getBody() {
        return body;
    }
}
