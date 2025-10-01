/*
 * Copyright 2024 CloudburstMC
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

import java.net.InetAddress;
import java.net.InetSocketAddress;

public interface RakServerMetrics {

    default void channelOpen(InetSocketAddress address) {
    }

    default void channelClose(InetSocketAddress address) {
    }

    default void unconnectedPing(InetSocketAddress address) {
    }

    default void connectionInitPacket(InetSocketAddress address, int packetId) {
    }

    default void addressBlocked(InetAddress address) {
    }

    default void addressUnblocked(InetAddress address) {
    }
}
