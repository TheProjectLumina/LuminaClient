package com.project.lumina.rpc.entities;

import com.google.gson.annotations.SerializedName;

public class Properties {
    @SerializedName("os")
    private String os;

    @SerializedName("browser")
    private String browser;

    @SerializedName("device")
    private String device;

    public Properties() {
    }

    public Properties(String os, String browser, String device) {
        this.os = os;
        this.browser = browser;
        this.device = device;
    }

    public static Properties createDefault() {
        return new Properties("Android", "Discord Android", "Android");
    }

    public String getOs() {
        return os;
    }

    public void setOs(String os) {
        this.os = os;
    }

    public String getBrowser() {
        return browser;
    }

    public void setBrowser(String browser) {
        this.browser = browser;
    }

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }
}