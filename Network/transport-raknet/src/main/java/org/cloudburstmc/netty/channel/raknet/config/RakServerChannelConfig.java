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
import io.netty.channel.ChannelConfig;

public interface RakServerChannelConfig extends ChannelConfig {

    int getMaxChannels();

    RakServerChannelConfig setMaxChannels(int maxChannels);

    long getGuid();

    RakServerChannelConfig setGuid(long guid);

    int[] getSupportedProtocols();

    RakServerChannelConfig setSupportedProtocols(int[] supportedProtocols);

    int getMaxConnections();

    RakServerChannelConfig setMaxConnections(int maxConnections);

    ByteBuf getUnconnectedMagic();

    RakServerChannelConfig setUnconnectedMagic(ByteBuf unconnectedMagic);

    ByteBuf getAdvertisement();

    RakServerChannelConfig setAdvertisement(ByteBuf advertisement);

    boolean getHandlePing();

    RakServerChannelConfig setHandlePing(boolean handlePing);

    int getMaxMtu();

    RakServerChannelConfig setMaxMtu(int mtu);

    int getMinMtu();

    RakServerChannelConfig setMinMtu(int mtu);

    int getPacketLimit();

    void setPacketLimit(int limit);

    int getGlobalPacketLimit();

    void setGlobalPacketLimit(int limit);

    void setSendCookie(boolean sendCookie);

    boolean getSendCookie();

    void setMetrics(RakServerMetrics metrics);

    RakServerMetrics getMetrics();

}
