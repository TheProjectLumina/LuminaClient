package com.project.lumina.rpc.utils;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class ThreadUtils {

    private static final Handler mainHandler = new Handler(Looper.getMainLooper());
    private static final ExecutorService backgroundExecutor = Executors.newCachedThreadPool();

    private ThreadUtils() {
    }

    public static boolean isMainThread() {
        return Looper.myLooper() == Looper.getMainLooper();
    }

    public static void postToMainThread(Runnable runnable) {
        if (runnable == null) {
            return;
        }
        if (isMainThread()) {
            runnable.run();
        } else {
            mainHandler.post(runnable);
        }
    }

    public static void executeInBackground(Runnable runnable) {
        if (runnable == null) {
            return;
        }
        backgroundExecutor.execute(runnable);
    }

    public static Handler getMainHandler() {
        return mainHandler;
    }
}