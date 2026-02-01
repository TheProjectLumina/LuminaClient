package com.project.lumina.rpc.exception;

import java.io.IOException;

public class NetworkException extends DiscordRPCException {

    public NetworkException(String message) {
        super(message);
    }

    public NetworkException(String message, Throwable cause) {
        super(message, cause);
    }

    public static NetworkException fromIOException(IOException e) {
        return new NetworkException("Network error: " + e.getMessage(), e);
    }
}