package com.project.lumina.rpc.entities;

import com.google.gson.annotations.SerializedName;

public class Identify {
    @SerializedName("token")
    private String token;

    @SerializedName("properties")
    private Properties properties;

    @SerializedName("capabilities")
    private int capabilities;

    @SerializedName("compress")
    private boolean compress;

    public Identify() {
    }

    public Identify(String token, Properties properties, int capabilities, boolean compress) {
        this.token = token;
        this.properties = properties;
        this.capabilities = capabilities;
        this.compress = compress;
    }

    public static Identify fromToken(String token) {
        return new Identify(token, Properties.createDefault(), 65, false);
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public int getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(int capabilities) {
        this.capabilities = capabilities;
    }

    public boolean isCompress() {
        return compress;
    }

    public void setCompress(boolean compress) {
        this.compress = compress;
    }
}