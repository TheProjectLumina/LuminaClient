package com.project.lumina.rpc.entities;

import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;

public class Payload {
    @SerializedName("op")
    private Integer op;

    @SerializedName("d")
    private JsonElement d;

    @SerializedName("s")
    private Integer s;

    @SerializedName("t")
    private String t;

    public Payload() {
    }

    public Payload(Integer op, JsonElement d, Integer s, String t) {
        this.op = op;
        this.d = d;
        this.s = s;
        this.t = t;
    }

    public Integer getOp() {
        return op;
    }

    public void setOp(Integer op) {
        this.op = op;
    }

    public OpCode getOpCode() {
        return op != null ? OpCode.fromValue(op) : null;
    }

    public JsonElement getD() {
        return d;
    }

    public void setD(JsonElement d) {
        this.d = d;
    }

    public Integer getS() {
        return s;
    }

    public void setS(Integer s) {
        this.s = s;
    }

    public String getT() {
        return t;
    }

    public void setT(String t) {
        this.t = t;
    }
}