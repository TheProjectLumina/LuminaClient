package com.project.lumina.rpc.entities;

import com.google.gson.annotations.SerializedName;
import com.project.lumina.rpc.utils.ValidationUtils;
import java.util.Arrays;
import java.util.List;

public class Activity {
    @SerializedName("name")
    private String name;

    @SerializedName("state")
    private String state;

    @SerializedName("details")
    private String details;

    @SerializedName("type")
    private Integer type;

    @SerializedName("application_id")
    private String applicationId;

    @SerializedName("timestamps")
    private Timestamps timestamps;

    @SerializedName("assets")
    private Assets assets;

    @SerializedName("buttons")
    private List<String> buttons;

    @SerializedName("metadata")
    private Metadata metadata;

    private Activity(Builder builder) {
        this.name = builder.name;
        this.state = builder.state;
        this.details = builder.details;
        this.type = builder.type;
        this.applicationId = builder.applicationId;
        this.timestamps = builder.timestamps;
        this.assets = builder.assets;
        this.buttons = builder.buttons;
        this.metadata = builder.metadata;
    }

    public String getName() {
        return name;
    }

    public String getState() {
        return state;
    }

    public String getDetails() {
        return details;
    }

    public Integer getType() {
        return type;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public Timestamps getTimestamps() {
        return timestamps;
    }

    public Assets getAssets() {
        return assets;
    }

    public List<String> getButtons() {
        return buttons;
    }

    public Metadata getMetadata() {
        return metadata;
    }

    public void validate() {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Activity name is required");
        }
    }

    public static class Builder {
        private String name;
        private String state;
        private String details;
        private Integer type;
        private String applicationId;
        private Timestamps timestamps;
        private Assets assets;
        private List<String> buttons;
        private Metadata metadata;

        public Builder() {
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setState(String state) {
            this.state = state;
            return this;
        }

        public Builder setDetails(String details) {
            this.details = details;
            return this;
        }

        public Builder setType(int type) {
            this.type = type;
            return this;
        }

        public Builder setApplicationId(String applicationId) {
            ValidationUtils.validateApplicationId(applicationId);
            this.applicationId = applicationId;
            return this;
        }

        public Builder setTimestamps(Timestamps timestamps) {
            this.timestamps = timestamps;
            return this;
        }

        public Builder setAssets(Assets assets) {
            this.assets = assets;
            return this;
        }

        public Builder setButtons(List<String> buttons) {
            this.buttons = buttons;
            return this;
        }

        public Builder setMetadata(Metadata metadata) {
            this.metadata = metadata;
            return this;
        }

        public Builder setButton(String label, String url) {
            this.buttons = Arrays.asList(label);
            this.metadata = new Metadata.Builder()
                    .setButtonUrls(Arrays.asList(url))
                    .build();
            return this;
        }

        public Builder setButtons(String label1, String url1, String label2, String url2) {
            this.buttons = Arrays.asList(label1, label2);
            this.metadata = new Metadata.Builder()
                    .setButtonUrls(Arrays.asList(url1, url2))
                    .build();
            return this;
        }

        public Activity build() {
            return new Activity(this);
        }
    }
}