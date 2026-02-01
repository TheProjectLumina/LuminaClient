package com.project.lumina.rpc.entities;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

public class Ready {
    @SerializedName("session_id")
    private String sessionId;

    @SerializedName("resume_gateway_url")
    private String resumeGatewayUrl;

    @SerializedName("user")
    private JsonObject user;

    public Ready() {
    }

    public Ready(String sessionId, String resumeGatewayUrl, JsonObject user) {
        this.sessionId = sessionId;
        this.resumeGatewayUrl = resumeGatewayUrl;
        this.user = user;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getResumeGatewayUrl() {
        return resumeGatewayUrl;
    }

    public void setResumeGatewayUrl(String resumeGatewayUrl) {
        this.resumeGatewayUrl = resumeGatewayUrl;
    }

    public JsonObject getUser() {
        return user;
    }

    public void setUser(JsonObject user) {
        this.user = user;
    }
}