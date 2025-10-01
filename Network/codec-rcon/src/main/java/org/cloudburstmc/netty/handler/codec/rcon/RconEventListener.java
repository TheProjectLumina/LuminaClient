package org.cloudburstmc.netty.handler.codec.rcon;

public interface RconEventListener {

    String onMessage(String message);
}
