package com.project.lumina.rpc.entities;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public final class Metadata {

    @SerializedName("button_urls")
    private final List<String> buttonUrls;

    private Metadata(Builder builder) {
        this.buttonUrls = builder.buttonUrls;
    }

    public List<String> getButtonUrls() {
        return buttonUrls;
    }

    public static class Builder {
        private List<String> buttonUrls;

        public Builder setButtonUrls(List<String> buttonUrls) {
            this.buttonUrls = buttonUrls;
            return this;
        }

        public Metadata build() {
            return new Metadata(this);
        }
    }
}