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

import io.netty.channel.ChannelConfig;

public interface RakChannelConfig extends ChannelConfig {

    long getGuid();

    RakChannelConfig setGuid(long guid);

    int getMtu();

    RakChannelConfig setMtu(int mtu);

    int getProtocolVersion();

    RakChannelConfig setProtocolVersion(int protocolVersion);

    int getOrderingChannels();

    RakChannelConfig setOrderingChannels(int orderingChannels);

    RakChannelMetrics getMetrics();

    RakChannelConfig setMetrics(RakChannelMetrics metrics);

    long getSessionTimeout();

    RakChannelConfig setSessionTimeout(long timeout);

    boolean isAutoFlush();

    void setAutoFlush(boolean enable);

    int getFlushInterval();

    void setFlushInterval(int intervalMillis);

    void setMaxQueuedBytes(int maxQueuedBytes);

    int getMaxQueuedBytes();
}
