/*
 * Copyright 2022 CloudburstMC
 *
 * CloudburstMC licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package org.cloudburstmc.netty.channel.raknet.config;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelOption;

public class RakChannelOption<T> extends ChannelOption<T> {

    /**
     * TODO: max amount of child channels per server
     */
    public static final ChannelOption<Integer> RAK_MAX_CHANNELS =
            valueOf(RakChannelOption.class, "RAK_MAX_CHANNELS");

    /**
     * Maximum amount of ordering channels each RakNet connection can have. (1-256)
     */
    public static final ChannelOption<Integer> RAK_ORDERING_CHANNELS =
            valueOf(RakChannelOption.class, "RAK_ORDERING_CHANNELS");

    /**
     * MTU that the RakNet client will use when initially connecting.
     */
    public static final ChannelOption<Integer> RAK_MTU =
            valueOf(RakChannelOption.class, "RAK_MTU");

    /**
     * Maximum allowed MTU that the RakNet server connection can use
     */
    public static final ChannelOption<Integer> RAK_MAX_MTU =
            valueOf(RakChannelOption.class, "RAK_MAX_MTU");

    /**
     * Minimum allowed MTU that the RakNet server connection can use
     */
    public static final ChannelOption<Integer> RAK_MIN_MTU =
            valueOf(RakChannelOption.class, "RAK_MIN_MTU");

    /**
     * Unique ID of the RakNet peer sent.
     */
    public static final ChannelOption<Long> RAK_GUID =
            valueOf(RakChannelOption.class, "RAK_GUID");

    /**
     * Unique ID of the remote RakNet peer sent. Usually used in client implementation.
     */
    public static final ChannelOption<Long> RAK_REMOTE_GUID =
            valueOf(RakChannelOption.class, "RAK_REMOTE_GUID");

    /**
     * Maximum allowed connections to the RakNet server. Subsequent connections will be denied.
     */
    public static final ChannelOption<Integer> RAK_MAX_CONNECTIONS =
            valueOf(RakChannelOption.class, "RAK_MAX_CONNECTIONS");

    /**
     * RakNet protocol version to send to remote peer.
     */
    public static final ChannelOption<Integer> RAK_PROTOCOL_VERSION =
            valueOf(RakChannelOption.class, "RAK_PROTOCOL_VERSION");

    /**
     * Versions supported by the RakNet server.
     * <p>
     * By default, all protocol versions will be supported.
     */
    public static final ChannelOption<int[]> RAK_SUPPORTED_PROTOCOLS =
            valueOf(RakChannelOption.class, "RAK_SUPPORTED_PROTOCOLS");

    /**
     * Magic used to make unconnected packets unique.
     */
    public static final ChannelOption<ByteBuf> RAK_UNCONNECTED_MAGIC =
            valueOf(RakChannelOption.class, "RAK_UNCONNECTED_MAGIC");

    /**
     * Timeout delay used during client offline phase in millis.
     */
    public static final ChannelOption<Long> RAK_CONNECT_TIMEOUT =
            valueOf(RakChannelOption.class, "RAK_CONNECT_TIMEOUT");

    /**
     * RakMetrics instance used for session
     */
    public static final ChannelOption<RakChannelMetrics> RAK_METRICS =
            valueOf(RakChannelOption.class, "RAK_METRICS");

    /**
     * RakMetrics instance used for server
     */
    public static final ChannelOption<RakServerMetrics> RAK_SERVER_METRICS =
            valueOf(RakChannelOption.class, "RAK_SERVER_METRICS");

    /**
     * The advertisement sent to clients pinging a server.
     */
    public static final ChannelOption<ByteBuf> RAK_ADVERTISEMENT =
            valueOf(RakChannelOption.class, "RAK_ADVERTISEMENT");

    /**
     * Enables custom handling for RakNet packets in the server implementation.
     */
    public static final ChannelOption<Boolean> RAK_HANDLE_PING =
            valueOf(RakChannelOption.class, "RAK_HANDLE_PING");

    /**
     * Time after session is closed due to no activity.
     */
    public static final ChannelOption<Long> RAK_SESSION_TIMEOUT =
            valueOf(RakChannelOption.class, "RAK_SESSION_TIMEOUT");

    /**
     * Whether RakNet will flush outgoing messages autoamatically every RAK_FLUSH_INTERVAL or flushing has to be triggered by the user.
     */
    public static final ChannelOption<Boolean> RAK_AUTO_FLUSH =
            valueOf(RakChannelOption.class, "RAK_AUTO_FLUSH");

    /**
     * Interval in milliseconds of how often auto flushing should happen.
     * Default is 10ms.
     */
    public static final ChannelOption<Integer> RAK_FLUSH_INTERVAL =
            valueOf(RakChannelOption.class, "RAK_FLUSH_INTERVAL");

    /**
     * A number of datagram packets each address can send within one RakNet tick (10ms).
     * Default is 120 packets.
     */
    public static final ChannelOption<Integer> RAK_PACKET_LIMIT =
            valueOf(RakChannelOption.class, "RAK_PACKET_LIMIT");

    /**
     * A number of all datagrams that will be handled within one RakNet tick before server starts dropping any incoming data.
     * Default is 100_000 (RAK_PACKET_LIMIT * 0.56 * 1500 different connections).
     */
    public static final ChannelOption<Integer> RAK_GLOBAL_PACKET_LIMIT =
            valueOf(RakChannelOption.class, "RAK_GLOBAL_PACKET_LIMIT");

    /**
     * Whether the client should be run in compatibility mode for closer behavior to the vanilla client RakNet implementation.
     */
    public static final ChannelOption<Boolean> RAK_COMPATIBILITY_MODE =
            valueOf(RakChannelOption.class, "RAK_COMPATIBILITY_MODE");

    /**
     * Whether to send a cookie to the client during the connection process.
     */
    public static final ChannelOption<Boolean> RAK_SEND_COOKIE =
            valueOf(RakChannelOption.class, "RAK_SEND_COOKIE");

    /**
     * An array of MTU sizes that the RakNet client will use when initially connecting.
     */
    public static final ChannelOption<Integer[]> RAK_MTU_SIZES =
            valueOf(RakChannelOption.class, "RAK_MTU_SIZES");

    /**
     * Whether to use the IP_DONT_FRAGMENT option for the client channel.
     */
    public static final ChannelOption<Boolean> RAK_IP_DONT_FRAGMENT =
            valueOf(RakChannelOption.class, "RAK_IP_DONT_FRAGMENT");

    /**
     * The amount of internal addresses to send from the client in New Incoming Connection packets.
     */
    public static final ChannelOption<Integer> RAK_CLIENT_INTERNAL_ADDRESSES =
            valueOf(RakChannelOption.class, "RAK_CLIENT_INTERNAL_ADDRESSES");


    /**
     * The time between the client sending connection attempts in milliseconds.
     */
    public static final ChannelOption<Integer> RAK_TIME_BETWEEN_SEND_CONNECTION_ATTEMPTS_MS =
            valueOf(RakChannelOption.class, "RAK_TIME_BETWEEN_SEND_CONNECTION_ATTEMPTS_MS");

    /**
     * Maximum amount of bytes that can be queued in a single RakNet session.
     */
    public static final ChannelOption<Integer> RAK_MAX_QUEUED_BYTES =
            valueOf(RakChannelOption.class, "RAK_MAX_QUEUED_BYTES");

    @SuppressWarnings("deprecation")
    protected RakChannelOption() {
        super(null);
    }
}
