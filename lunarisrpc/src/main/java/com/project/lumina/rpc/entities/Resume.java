package com.project.lumina.rpc.entities;

import com.google.gson.annotations.SerializedName;

public class Resume {
    @SerializedName("token")
    private String token;

    @SerializedName("session_id")
    private String sessionId;

    @SerializedName("seq")
    private Integer seq;

    public Resume() {
    }

    public Resume(String token, String sessionId, Integer seq) {
        this.token = token;
        this.sessionId = sessionId;
        this.seq = seq;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public Integer getSeq() {
        return seq;
    }

    public void setSeq(Integer seq) {
        this.seq = seq;
    }
}