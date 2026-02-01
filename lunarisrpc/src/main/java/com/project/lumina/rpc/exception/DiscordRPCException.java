package com.project.lumina.rpc.exception;

public class DiscordRPCException extends Exception {

    public DiscordRPCException(String message) {
        super(message);
    }

    public DiscordRPCException(String message, Throwable cause) {
        super(message, cause);
    }
}