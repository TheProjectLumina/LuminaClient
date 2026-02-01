package com.project.lumina.rpc.callback;

import com.google.gson.JsonObject;

public interface LunarisRPCCallback {

    void onReady(JsonObject user);

    void onDisconnected();

    void onError(Exception error);
}