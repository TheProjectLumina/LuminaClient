package com.project.lumina.rpc.entities;

import com.google.gson.annotations.SerializedName;
import com.project.lumina.rpc.utils.ValidationUtils;

public final class Assets {

    private static final int MAX_TEXT_LENGTH = 128;

    @SerializedName("large_image")
    private final String largeImage;

    @SerializedName("small_image")
    private final String smallImage;

    @SerializedName("large_text")
    private final String largeText;

    @SerializedName("small_text")
    private final String smallText;

    private Assets(Builder builder) {
        this.largeImage = builder.largeImage;
        this.smallImage = builder.smallImage;
        this.largeText = builder.largeText;
        this.smallText = builder.smallText;
    }

    public String getLargeImage() {
        return largeImage;
    }

    public String getSmallImage() {
        return smallImage;
    }

    public String getLargeText() {
        return largeText;
    }

    public String getSmallText() {
        return smallText;
    }

    public static class Builder {
        private String largeImage;
        private String smallImage;
        private String largeText;
        private String smallText;

        public Builder setLargeImage(String largeImage) {
            this.largeImage = ValidationUtils.normalizeAssetName(largeImage);
            return this;
        }

        public Builder setSmallImage(String smallImage) {
            this.smallImage = ValidationUtils.normalizeAssetName(smallImage);
            return this;
        }

        public Builder setLargeText(String largeText) {
            this.largeText = ValidationUtils.truncateText(largeText, MAX_TEXT_LENGTH);
            return this;
        }

        public Builder setSmallText(String smallText) {
            this.smallText = ValidationUtils.truncateText(smallText, MAX_TEXT_LENGTH);
            return this;
        }

        public Assets build() {
            return new Assets(this);
        }
    }
}