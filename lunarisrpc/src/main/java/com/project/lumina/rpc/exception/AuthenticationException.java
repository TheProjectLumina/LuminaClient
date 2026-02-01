package com.project.lumina.rpc.exception;

public class AuthenticationException extends DiscordRPCException {

    public AuthenticationException(String message) {
        super(message);
    }

    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}