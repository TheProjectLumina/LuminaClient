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

package com.project.lumina.rpc;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.project.lumina.rpc.callback.LunarisRPCCallback;
import com.project.lumina.rpc.entities.Presence;
import com.project.lumina.rpc.gateway.GatewayConnection;
import com.project.lumina.rpc.utils.RPCLogger;

public class LunarisRPC {

    private final GatewayConnection gatewayConnection;
    private final LunarisRPCCallback userCallback;

    public LunarisRPC(@NonNull String token, @Nullable LunarisRPCCallback callback) {
        if (token == null || token.isEmpty()) {
            RPCLogger.error("Token cannot be null or empty");
            throw new IllegalArgumentException("Token cannot be null or empty");
        }
        RPCLogger.info("LunarisRPC instance created");
        this.userCallback = callback;
        this.gatewayConnection = new GatewayConnection(token, createInternalCallback());
    }

    public void connect() {
        RPCLogger.info("Initiating connection to Discord Gateway");
        gatewayConnection.connect();
    }

    public void updatePresence(@NonNull Presence presence) {
        if (presence == null) {
            RPCLogger.error("Presence cannot be null");
            throw new IllegalArgumentException("Presence cannot be null");
        }
        RPCLogger.info("Updating presence");
        gatewayConnection.sendPresence(presence);
    }

    public boolean isConnected() {
        return gatewayConnection.isConnected();
    }

    public void disconnect() {
        RPCLogger.info("Disconnecting from Discord Gateway");
        gatewayConnection.close();
    }

    private LunarisRPCCallback createInternalCallback() {
        return new LunarisRPCCallback() {
            @Override
            public void onReady(com.google.gson.JsonObject user) {
                RPCLogger.info("Discord RPC ready");
                if (userCallback != null) {
                    userCallback.onReady(user);
                }
            }

            @Override
            public void onDisconnected() {
                RPCLogger.info("Discord RPC disconnected");
                if (userCallback != null) {
                    userCallback.onDisconnected();
                }
            }

            @Override
            public void onError(Exception error) {
                RPCLogger.error("Discord RPC error", error);
                if (userCallback != null) {
                    userCallback.onError(error);
                }
            }
        };
    }
}