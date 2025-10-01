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

import static org.cloudburstmc.netty.channel.raknet.RakConstants.DEFAULT_UNCONNECTED_MAGIC;

import org.cloudburstmc.netty.channel.raknet.RakConstants;
import org.cloudburstmc.netty.channel.raknet.RakServerChannel;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelOption;
import io.netty.channel.DefaultChannelConfig;

/**
 * The default {@link RakServerChannelConfig} implementation for RakNet server.
 */
public class DefaultRakServerConfig extends DefaultChannelConfig implements RakServerChannelConfig {

    private volatile int maxChannels;
    private volatile long guid = ThreadLocalRandom.current().nextLong();
    private volatile int[] supportedProtocols;
    private volatile int maxConnections;
    private volatile ByteBuf unconnectedMagic = Unpooled.wrappedBuffer(DEFAULT_UNCONNECTED_MAGIC);
    private volatile ByteBuf advertisement;
    private volatile boolean handlePing;
    private volatile int maxMtu = RakConstants.MAXIMUM_MTU_SIZE;
    private volatile int minMtu = RakConstants.MINIMUM_MTU_SIZE;
    private volatile int packetLimit = RakConstants.DEFAULT_PACKET_LIMIT;
    private volatile int globalPacketLimit = RakConstants.DEFAULT_GLOBAL_PACKET_LIMIT;
    private volatile RakServerMetrics metrics;
    private volatile boolean sendCookie;


    public DefaultRakServerConfig(RakServerChannel channel) {
        super(channel);
    }

    @Override
    public Map<ChannelOption<?>, Object> getOptions() {
        return getOptions(
                super.getOptions(),
                RakChannelOption.RAK_GUID, RakChannelOption.RAK_MAX_CHANNELS, RakChannelOption.RAK_MAX_CONNECTIONS, RakChannelOption.RAK_SUPPORTED_PROTOCOLS, RakChannelOption.RAK_UNCONNECTED_MAGIC,
                RakChannelOption.RAK_ADVERTISEMENT, RakChannelOption.RAK_HANDLE_PING, RakChannelOption.RAK_PACKET_LIMIT, RakChannelOption.RAK_GLOBAL_PACKET_LIMIT, RakChannelOption.RAK_SEND_COOKIE,
                RakChannelOption.RAK_SERVER_METRICS, RakChannelOption.RAK_IP_DONT_FRAGMENT);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getOption(ChannelOption<T> option) {
        if (option == RakChannelOption.RAK_MAX_MTU) {
            return (T) Integer.valueOf(this.getMaxMtu());
        }
        if (option == RakChannelOption.RAK_MIN_MTU) {
            return (T) Integer.valueOf(this.getMinMtu());
        }
        if (option == RakChannelOption.RAK_GUID) {
            return (T) Long.valueOf(this.getGuid());
        }
        if (option == RakChannelOption.RAK_MAX_CHANNELS) {
            return (T) Integer.valueOf(this.getMaxChannels());
        }
        if (option == RakChannelOption.RAK_MAX_CONNECTIONS) {
            return (T) Integer.valueOf(this.getMaxConnections());
        }
        if (option == RakChannelOption.RAK_SUPPORTED_PROTOCOLS) {
            return (T) this.getSupportedProtocols();
        }
        if (option == RakChannelOption.RAK_UNCONNECTED_MAGIC) {
            return (T) this.getUnconnectedMagic();
        }
        if (option == RakChannelOption.RAK_ADVERTISEMENT) {
            return (T) this.getAdvertisement();
        }
        if (option == RakChannelOption.RAK_HANDLE_PING) {
            return (T) Boolean.valueOf(this.getHandlePing());
        }
        if (option == RakChannelOption.RAK_PACKET_LIMIT) {
            return (T) Integer.valueOf(this.getPacketLimit());
        }
        if (option == RakChannelOption.RAK_GLOBAL_PACKET_LIMIT) {
            return (T) Integer.valueOf(this.getGlobalPacketLimit());
        }
        if (option == RakChannelOption.RAK_SERVER_METRICS) {
            return (T) this.getMetrics();
        }
        if (option == RakChannelOption.RAK_SEND_COOKIE) {
            return (T) Boolean.valueOf(this.sendCookie);
        }
        return this.channel.parent().config().getOption(option);
    }

    @Override
    public <T> boolean setOption(ChannelOption<T> option, T value) {
        this.validate(option, value);

        if (option == RakChannelOption.RAK_GUID) {
            this.setGuid((Long) value);
        } else if (option == RakChannelOption.RAK_MAX_CHANNELS) {
            this.setMaxChannels((Integer) value);
        } else if (option == RakChannelOption.RAK_MAX_CONNECTIONS) {
            this.setMaxConnections((Integer) value);
        } else if (option == RakChannelOption.RAK_SUPPORTED_PROTOCOLS) {
            this.setSupportedProtocols((int[]) value);
        } else if (option == RakChannelOption.RAK_UNCONNECTED_MAGIC) {
            this.setUnconnectedMagic((ByteBuf) value);
        } else if (option == RakChannelOption.RAK_ADVERTISEMENT) {
            this.setAdvertisement((ByteBuf) value);
        } else if (option == RakChannelOption.RAK_HANDLE_PING) {
            this.setHandlePing((Boolean) value);
        } else if (option == RakChannelOption.RAK_MAX_MTU) {
            this.setMaxMtu((Integer) value);
        } else if (option == RakChannelOption.RAK_MIN_MTU) {
            this.setMinMtu((Integer) value);
        } else if (option == RakChannelOption.RAK_PACKET_LIMIT) {
            this.setPacketLimit((Integer) value);
        } else if (option == RakChannelOption.RAK_GLOBAL_PACKET_LIMIT) {
            this.setGlobalPacketLimit((Integer) value);
        } else if (option == RakChannelOption.RAK_SEND_COOKIE) {
            this.setSendCookie((Boolean) value);
        } else if (option == RakChannelOption.RAK_SERVER_METRICS) {
            this.setMetrics((RakServerMetrics) value);
        } else {
            return this.channel.parent().config().setOption(option, value);
        }
        return true;
    }

    @Override
    public int getMaxChannels() {
        return maxChannels;
    }

    @Override
    public RakServerChannelConfig setMaxChannels(int maxChannels) {
        if (maxChannels < 1 || maxChannels > 256) {
            throw new IllegalArgumentException("maxChannels can only be a value between 1 and 256");
        }
        this.maxChannels = maxChannels;
        return this;
    }

    @Override
    public long getGuid() {
        return this.guid;
    }

    @Override
    public RakServerChannelConfig setGuid(long guid) {
        this.guid = guid;
        return this;
    }

    @Override
    public int[] getSupportedProtocols() {
        return this.supportedProtocols;
    }

    @Override
    public RakServerChannelConfig setSupportedProtocols(int[] supportedProtocols) {
        if (supportedProtocols == null) {
            this.supportedProtocols = null;
        } else {
            this.supportedProtocols = Arrays.copyOf(supportedProtocols, supportedProtocols.length);
            Arrays.sort(this.supportedProtocols);
        }
        return this;
    }

    @Override
    public int getMaxConnections() {
        return this.maxConnections;
    }

    @Override
    public RakServerChannelConfig setMaxConnections(int maxConnections) {
        this.maxConnections = maxConnections;
        return this;
    }

    @Override
    public ByteBuf getUnconnectedMagic() {
        return this.unconnectedMagic.slice();
    }

    @Override
    public RakServerChannelConfig setUnconnectedMagic(ByteBuf unconnectedMagic) {
        if (unconnectedMagic.readableBytes() < 16) {
            throw new IllegalArgumentException("Unconnected magic must at least be 16 bytes");
        }
        this.unconnectedMagic = unconnectedMagic.copy().asReadOnly();
        return this;
    }

    @Override
    public ByteBuf getAdvertisement() {
        return this.advertisement;
    }

    @Override
    public RakServerChannelConfig setAdvertisement(ByteBuf advertisement) {
        this.advertisement = advertisement.copy().asReadOnly();
        return this;
    }

    @Override
    public boolean getHandlePing() {
        return this.handlePing;
    }

    @Override
    public RakServerChannelConfig setHandlePing(boolean handlePing) {
        this.handlePing = handlePing;
        return this;
    }

    @Override
    public RakServerChannelConfig setMaxMtu(int maxMtu) {
        this.maxMtu = maxMtu;
        return this;
    }

    @Override
    public int getMaxMtu() {
        return this.maxMtu;
    }

    @Override
    public RakServerChannelConfig setMinMtu(int minMtu) {
        this.minMtu = minMtu;
        return this;
    }

    @Override
    public int getMinMtu() {
        return this.minMtu;
    }

    @Override
    public void setPacketLimit(int limit) {
        this.packetLimit = limit;
    }

    @Override
    public int getPacketLimit() {
        return this.packetLimit;
    }

    @Override
    public int getGlobalPacketLimit() {
        return globalPacketLimit;
    }

    @Override
    public void setGlobalPacketLimit(int globalPacketLimit) {
        this.globalPacketLimit = globalPacketLimit;
    }

    @Override
    public void setSendCookie(boolean sendCookie) {
        this.sendCookie = sendCookie;
    }

    @Override
    public boolean getSendCookie() {
        return this.sendCookie;
    }

    @Override
    public void setMetrics(RakServerMetrics metrics) {
        this.metrics = metrics;
    }

    @Override
    public RakServerMetrics getMetrics() {
        return this.metrics;
    }

}
