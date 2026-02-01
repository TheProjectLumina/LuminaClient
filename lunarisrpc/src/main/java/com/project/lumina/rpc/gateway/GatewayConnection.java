/*
 * ██╗     ██╗   ██╗███╗   ██╗ █████╗ ██████╗ ██╗███████╗██████╗ ██████╗  ██████╗
 * ██║     ██║   ██║████╗  ██║██╔══██╗██╔══██╗██║██╔════╝██╔══██╗██╔══██╗██╔════╝
 * ██║     ██║   ██║██╔██╗ ██║███████║██████╔╝██║███████╗██████╔╝██████╔╝██║
 * ██║     ██║   ██║██║╚██╗██║██╔══██║██╔══██╗██║╚════██║██╔══██╗██╔═══╝ ██║
 * ███████╗╚██████╔╝██║ ╚████║██║  ██║██║  ██║██║███████║██║  ██║██║     ╚██████╗
 * ╚══════╝ ╚═════╝ ╚═╝  ╚═══╝╚═╝  ╚═╝╚═╝  ╚═╝╚═╝╚══════╝╚═╝  ╚═╝╚═╝      ╚═════╝
 *
 * Implementation created under the law of TheProjectLumina org and the Team <3
 */

package com.project.lumina.rpc.gateway;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import com.project.lumina.rpc.callback.LunarisRPCCallback;
import com.project.lumina.rpc.entities.Identify;
import com.project.lumina.rpc.entities.OpCode;
import com.project.lumina.rpc.entities.Payload;
import com.project.lumina.rpc.entities.Presence;
import com.project.lumina.rpc.entities.Ready;
import com.project.lumina.rpc.entities.Resume;
import com.project.lumina.rpc.exception.AuthenticationException;
import com.project.lumina.rpc.exception.InvalidTokenException;
import com.project.lumina.rpc.exception.NetworkException;
import com.project.lumina.rpc.utils.RPCLogger;
import com.project.lumina.rpc.utils.ThreadUtils;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class GatewayConnection {

    private static final String DEFAULT_GATEWAY_URL = "wss://gateway.discord.gg/?v=10&encoding=json";
    private static final int RECONNECTABLE_CLOSE_CODE = 4000;
    private static final int AUTHENTICATION_FAILED_CODE = 4004;
    private static final int INVALID_TOKEN_CODE = 4014;
    private static final int RATE_LIMITED_CODE = 4008;
    private static final int DISALLOWED_INTENTS_CODE = 4014;

    public enum ConnectionState {
        DISCONNECTED,
        CONNECTING,
        CONNECTED
    }

    private final OkHttpClient client;
    private final Gson gson;
    private final String token;
    private final LunarisRPCCallback callback;

    private WebSocket webSocket;
    private Integer sequence;
    private String sessionId;
    private String resumeGatewayUrl;
    private long heartbeatInterval;
    private ConnectionState connectionState;
    private boolean shouldResume;

    private ScheduledExecutorService heartbeatExecutor;
    private ScheduledFuture<?> heartbeatTask;
    private Presence pendingPresence;


    public GatewayConnection(String token, LunarisRPCCallback callback) {
        this.token = token;
        this.callback = callback;
        this.connectionState = ConnectionState.DISCONNECTED;
        this.shouldResume = false;
        this.client = new OkHttpClient.Builder()
                .readTimeout(65, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .build();
        this.gson = new GsonBuilder()
                .serializeNulls()
                .create();
    }

    public void connect() {
        connect(DEFAULT_GATEWAY_URL);
    }

    public void connect(String gatewayUrl) {
        if (connectionState == ConnectionState.CONNECTING || connectionState == ConnectionState.CONNECTED) {
            RPCLogger.warn("Already connecting or connected, ignoring connect request");
            return;
        }

        connectionState = ConnectionState.CONNECTING;
        String url = gatewayUrl != null ? gatewayUrl : DEFAULT_GATEWAY_URL;
        RPCLogger.info("Connecting to gateway: " + url);

        ThreadUtils.executeInBackground(() -> {
            Request request = new Request.Builder()
                    .url(url)
                    .build();

            webSocket = client.newWebSocket(request, new GatewayWebSocketListener());
        });
    }

    public void send(Payload payload) {
        if (webSocket != null) {
            ThreadUtils.executeInBackground(() -> {
                String json = gson.toJson(payload);
                webSocket.send(json);
            });
        }
    }

    public void sendPresence(Presence presence) {
        if (presence == null) {
            RPCLogger.error("Presence cannot be null");
            throw new IllegalArgumentException("Presence cannot be null");
        }
        presence.validate();

        if (connectionState != ConnectionState.CONNECTED) {
            RPCLogger.info("Not connected, queuing presence update");
            pendingPresence = presence;
            return;
        }

        RPCLogger.info("Sending presence update to gateway");
        JsonElement presenceData = gson.toJsonTree(presence);
        Payload payload = new Payload(OpCode.PRESENCE_UPDATE.getValue(), presenceData, null, null);
        send(payload);
    }

    public boolean isConnected() {
        return connectionState == ConnectionState.CONNECTED;
    }

    public ConnectionState getConnectionState() {
        return connectionState;
    }

    @Nullable
    public String getSessionId() {
        return sessionId;
    }

    @Nullable
    public String getResumeGatewayUrl() {
        return resumeGatewayUrl;
    }

    public void close() {
        RPCLogger.info("Closing gateway connection");
        stopHeartbeat();

        ThreadUtils.executeInBackground(() -> {
            if (webSocket != null) {
                webSocket.close(1000, "Client disconnect");
                webSocket = null;
            }
        });

        clearSessionState();
        connectionState = ConnectionState.DISCONNECTED;
        shouldResume = false;
    }


    private void clearSessionState() {
        sessionId = null;
        resumeGatewayUrl = null;
        sequence = null;
        pendingPresence = null;
    }

    private void handleMessage(String json) {
        Payload payload = gson.fromJson(json, Payload.class);
        if (payload == null) {
            return;
        }

        if (payload.getS() != null) {
            sequence = payload.getS();
        }

        OpCode opCode = payload.getOpCode();
        if (opCode == null) {
            return;
        }

        switch (opCode) {
            case HELLO:
                handleHello(payload);
                break;
            case DISPATCH:
                handleDispatch(payload);
                break;
            case HEARTBEAT:
                sendHeartbeat();
                break;
            case HEARTBEAT_ACK:
                break;
            case RECONNECT:
                handleReconnect();
                break;
            case INVALID_SESSION:
                handleInvalidSession(payload);
                break;
            default:
                break;
        }
    }

    private void handleHello(Payload payload) {
        RPCLogger.info("Received HELLO from gateway");
        JsonElement data = payload.getD();
        if (data != null && data.isJsonObject()) {
            JsonObject obj = data.getAsJsonObject();
            if (obj.has("heartbeat_interval")) {
                heartbeatInterval = obj.get("heartbeat_interval").getAsLong();
                RPCLogger.info("Heartbeat interval: " + heartbeatInterval + "ms");
                startHeartbeat();
            }
        }

        if (shouldResume && sessionId != null) {
            RPCLogger.info("Attempting to resume session");
            sendResume();
        } else {
            RPCLogger.info("Sending IDENTIFY");
            sendIdentify();
        }
    }

    private void handleDispatch(Payload payload) {
        String eventType = payload.getT();
        if (eventType == null) {
            return;
        }

        switch (eventType) {
            case "READY":
                handleReady(payload);
                break;
            case "RESUMED":
                RPCLogger.info("Session resumed successfully");
                connectionState = ConnectionState.CONNECTED;
                notifyReady(null);
                sendPendingPresence();
                break;
            default:
                break;
        }
    }

    private void handleReady(Payload payload) {
        RPCLogger.info("Received READY event");
        JsonObject userData = null;
        JsonElement data = payload.getD();
        if (data != null && data.isJsonObject()) {
            Ready ready = gson.fromJson(data, Ready.class);
            if (ready != null) {
                sessionId = ready.getSessionId();
                resumeGatewayUrl = ready.getResumeGatewayUrl();
                userData = ready.getUser();
                RPCLogger.info("Session ID: " + sessionId);
            }
        }

        connectionState = ConnectionState.CONNECTED;
        RPCLogger.info("Gateway connection established");
        notifyReady(userData);
        sendPendingPresence();
    }


    private void handleReconnect() {
        RPCLogger.info("Gateway requested reconnect");
        shouldResume = true;
        if (webSocket != null) {
            webSocket.close(1000, "Reconnect requested");
        }
        reconnect();
    }

    private void handleInvalidSession(Payload payload) {
        RPCLogger.warn("Received INVALID_SESSION");
        JsonElement data = payload.getD();
        boolean resumable = data != null && data.isJsonPrimitive() && data.getAsBoolean();

        if (resumable && sessionId != null) {
            RPCLogger.info("Session is resumable");
            shouldResume = true;
        } else {
            RPCLogger.info("Session not resumable, clearing state");
            shouldResume = false;
            clearSessionState();
        }

        reconnect();
    }

    private void reconnect() {
        RPCLogger.info("Reconnecting to gateway");
        stopHeartbeat();
        connectionState = ConnectionState.DISCONNECTED;

        String url = shouldResume && resumeGatewayUrl != null ? resumeGatewayUrl : DEFAULT_GATEWAY_URL;
        connect(url);
    }

    private void sendIdentify() {
        Identify identify = Identify.fromToken(token);
        JsonElement identifyData = gson.toJsonTree(identify);
        Payload payload = new Payload(OpCode.IDENTIFY.getValue(), identifyData, null, null);
        send(payload);
    }

    private void sendResume() {
        Resume resume = new Resume(token, sessionId, sequence);
        JsonElement resumeData = gson.toJsonTree(resume);
        Payload payload = new Payload(OpCode.RESUME.getValue(), resumeData, null, null);
        send(payload);
    }

    private void sendHeartbeat() {
        JsonElement seqData = sequence != null ? gson.toJsonTree(sequence) : null;
        Payload payload = new Payload(OpCode.HEARTBEAT.getValue(), seqData, null, null);
        send(payload);
    }

    private void startHeartbeat() {
        stopHeartbeat();
        heartbeatExecutor = Executors.newSingleThreadScheduledExecutor();
        heartbeatTask = heartbeatExecutor.scheduleWithFixedDelay(
                this::sendHeartbeat,
                heartbeatInterval,
                heartbeatInterval,
                TimeUnit.MILLISECONDS
        );
    }

    private void stopHeartbeat() {
        if (heartbeatTask != null) {
            heartbeatTask.cancel(false);
            heartbeatTask = null;
        }
        if (heartbeatExecutor != null) {
            heartbeatExecutor.shutdown();
            heartbeatExecutor = null;
        }
    }

    private void sendPendingPresence() {
        if (pendingPresence != null) {
            Presence presence = pendingPresence;
            pendingPresence = null;
            sendPresence(presence);
        }
    }


    private class GatewayWebSocketListener extends WebSocketListener {
        @Override
        public void onOpen(@NonNull WebSocket webSocket, @NonNull Response response) {
            RPCLogger.info("WebSocket connection opened");
        }

        @Override
        public void onMessage(@NonNull WebSocket webSocket, @NonNull String text) {
            handleMessage(text);
        }

        @Override
        public void onClosing(@NonNull WebSocket webSocket, int code, @NonNull String reason) {
            webSocket.close(code, reason);
        }

        @Override
        public void onClosed(@NonNull WebSocket webSocket, int code, @NonNull String reason) {
            RPCLogger.info("WebSocket closed - Code: " + code + ", Reason: " + reason);
            boolean wasConnected = connectionState == ConnectionState.CONNECTED;
            connectionState = ConnectionState.DISCONNECTED;
            stopHeartbeat();

            Exception error = mapCloseCodeToException(code, reason);
            if (error != null) {
                RPCLogger.error("Close code mapped to error", error);
                notifyError(error);
                return;
            }

            if (code == RECONNECTABLE_CLOSE_CODE || shouldResume) {
                RPCLogger.info("Reconnectable close code, will attempt reconnect");
                shouldResume = true;
                reconnect();
            } else {
                if (wasConnected) {
                    notifyDisconnected();
                }
            }
        }

        @Override
        public void onFailure(@NonNull WebSocket webSocket, @NonNull Throwable t, @Nullable Response response) {
            RPCLogger.error("WebSocket connection failed", t);
            connectionState = ConnectionState.DISCONNECTED;
            stopHeartbeat();

            Exception error;
            if (t instanceof IOException) {
                error = NetworkException.fromIOException((IOException) t);
            } else {
                error = new NetworkException("Connection failed: " + t.getMessage(), t);
            }
            notifyError(error);
        }
    }

    private Exception mapCloseCodeToException(int code, String reason) {
        switch (code) {
            case AUTHENTICATION_FAILED_CODE:
                return new AuthenticationException("Authentication failed: " + reason);
            case INVALID_TOKEN_CODE:
                return new InvalidTokenException("Invalid token provided");
            case RATE_LIMITED_CODE:
                return new AuthenticationException("Rate limited by Discord");
            default:
                return null;
        }
    }

    private void notifyReady(JsonObject user) {
        if (callback != null) {
            ThreadUtils.postToMainThread(() -> callback.onReady(user));
        }
    }

    private void notifyDisconnected() {
        if (callback != null) {
            ThreadUtils.postToMainThread(callback::onDisconnected);
        }
    }

    private void notifyError(Exception error) {
        if (callback != null) {
            ThreadUtils.postToMainThread(() -> callback.onError(error));
        }
    }
}