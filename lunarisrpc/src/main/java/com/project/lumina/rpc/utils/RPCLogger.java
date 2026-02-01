package com.project.lumina.rpc.utils;

import android.util.Log;

public class RPCLogger {

    private static final String TAG = "LunarisRPC";
    private static boolean enabled = true;
    private static LogCallback callback;

    public interface LogCallback {
        void onLog(String level, String message);
    }

    public static void setEnabled(boolean enabled) {
        RPCLogger.enabled = enabled;
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void setCallback(LogCallback callback) {
        RPCLogger.callback = callback;
    }

    public static void info(String message) {
        log("INFO", message);
    }

    public static void warn(String message) {
        log("WARN", message);
    }

    public static void error(String message) {
        log("ERROR", message);
    }

    public static void error(String message, Throwable throwable) {
        log("ERROR", message + " - " + throwable.getMessage());
        if (throwable != null) {
            log("ERROR", "Stack trace: " + Log.getStackTraceString(throwable));
        }
    }

    public static void debug(String message) {
        log("DEBUG", message);
    }

    private static void log(String level, String message) {
        if (!enabled) {
            return;
        }

        String fullMessage = "[" + TAG + "] " + message;

        switch (level) {
            case "ERROR":
                Log.e(TAG, message);
                break;
            case "WARN":
                Log.w(TAG, message);
                break;
            case "DEBUG":
                Log.d(TAG, message);
                break;
            default:
                Log.i(TAG, message);
                break;
        }

        if (callback != null) {
            callback.onLog(level, fullMessage);
        }
    }
}