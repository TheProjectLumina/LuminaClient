package com.project.lumina.rpc.entities;

import com.google.gson.annotations.SerializedName;

public enum OpCode {
    @SerializedName("0")
    DISPATCH(0),
    @SerializedName("1")
    HEARTBEAT(1),
    @SerializedName("2")
    IDENTIFY(2),
    @SerializedName("3")
    PRESENCE_UPDATE(3),
    @SerializedName("6")
    RESUME(6),
    @SerializedName("7")
    RECONNECT(7),
    @SerializedName("9")
    INVALID_SESSION(9),
    @SerializedName("10")
    HELLO(10),
    @SerializedName("11")
    HEARTBEAT_ACK(11);

    private final int value;

    OpCode(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static OpCode fromValue(int value) {
        for (OpCode opCode : values()) {
            if (opCode.value == value) {
                return opCode;
            }
        }
        return null;
    }
}