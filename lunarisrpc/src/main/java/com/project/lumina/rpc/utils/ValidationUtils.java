package com.project.lumina.rpc.utils;

public final class ValidationUtils {

    private ValidationUtils() {
    }

    public static String truncateText(String text, int maxLength) {
        if (text == null) {
            return null;
        }
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength);
    }

    public static String normalizeAssetName(String name) {
        if (name == null) {
            return null;
        }
        if (name.trim().isEmpty()) {
            return null;
        }
        return name;
    }

    public static void validateApplicationId(String id) {
        if (id == null || id.isEmpty()) {
            return;
        }
        for (int i = 0; i < id.length(); i++) {
            if (!Character.isDigit(id.charAt(i))) {
                throw new IllegalArgumentException("Application ID must contain only numeric characters");
            }
        }
    }
}