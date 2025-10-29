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
import static org.cloudburstmc.netty.channel.raknet.RakConstants.MTU_SIZES;
import static org.cloudburstmc.netty.channel.raknet.RakConstants.SESSION_TIMEOUT_MS;
import static org.cloudburstmc.netty.channel.raknet.RakConstants.TIME_BETWEEN_SEND_CONNECTION_ATTEMPTS_MS;

import java.util.Map;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;

/**
 * The extended implementation of {@link RakChannelConfig} based on {@link DefaultRakSessionConfig} used by client.
 */
public class DefaultRakClientConfig extends DefaultRakSessionConfig {

    private volatile ByteBuf unconnectedMagic = Unpooled.wrappedBuffer(DEFAULT_UNCONNECTED_MAGIC);
    private volatile long connectTimeout = SESSION_TIMEOUT_MS;
    private volatile long sessionTimeout = SESSION_TIMEOUT_MS;
    private volatile long serverGuid;
    private volatile boolean compatibilityMode = false;
    private volatile Integer[] mtuSizes = MTU_SIZES;
    private volatile int clientInternalAddresses = 10;
    private volatile int timeBetweenSendConnectionAttemptsMS = TIME_BETWEEN_SEND_CONNECTION_ATTEMPTS_MS;

    public DefaultRakClientConfig(Channel channel) {
        super(channel);
    }

    @Override
    public Map<ChannelOption<?>, Object> getOptions() {
        return this.getOptions(
            super.getOptions(), 
            RakChannelOption.RAK_UNCONNECTED_MAGIC, RakChannelOption.RAK_CONNECT_TIMEOUT, RakChannelOption.RAK_REMOTE_GUID, RakChannelOption.RAK_SESSION_TIMEOUT, RakChannelOption.RAK_COMPATIBILITY_MODE,
            RakChannelOption.RAK_MTU_SIZES, RakChannelOption.RAK_IP_DONT_FRAGMENT, RakChannelOption.RAK_CLIENT_INTERNAL_ADDRESSES, RakChannelOption.RAK_TIME_BETWEEN_SEND_CONNECTION_ATTEMPTS_MS);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getOption(ChannelOption<T> option) {
        if (option == RakChannelOption.RAK_UNCONNECTED_MAGIC) {
            return (T) this.getUnconnectedMagic();
        } else if (option == RakChannelOption.RAK_CONNECT_TIMEOUT) {
            return (T) Long.valueOf(this.getConnectTimeout());
        } else if (option == RakChannelOption.RAK_REMOTE_GUID) {
            return (T) Long.valueOf(this.getServerGuid());
        } else if (option == RakChannelOption.RAK_SESSION_TIMEOUT) {
            return (T) Long.valueOf(this.getSessionTimeout());
        } else if (option == RakChannelOption.RAK_COMPATIBILITY_MODE) {
            return (T) Boolean.valueOf(this.isCompatibilityMode());
        } else if (option == RakChannelOption.RAK_MTU_SIZES) {
            return (T) this.getMtuSizes();
        } else if (option == RakChannelOption.RAK_CLIENT_INTERNAL_ADDRESSES) {
            return (T) Integer.valueOf(this.clientInternalAddresses);
        } else if (option == RakChannelOption.RAK_TIME_BETWEEN_SEND_CONNECTION_ATTEMPTS_MS) {
            return (T) Integer.valueOf(this.timeBetweenSendConnectionAttemptsMS);
        }
        return super.getOption(option);
    }

    @Override
    public <T> boolean setOption(ChannelOption<T> option, T value) {
        this.validate(option, value);

        if (option == RakChannelOption.RAK_UNCONNECTED_MAGIC) {
            this.setUnconnectedMagic((ByteBuf) value);
            return true;
        } else if (option == RakChannelOption.RAK_CONNECT_TIMEOUT) {
            this.setConnectTimeout((Long) value);
            return true;
        } else if (option == RakChannelOption.RAK_REMOTE_GUID) {
            this.setServerGuid((Long) value);
            return true;
        } else if (option == RakChannelOption.RAK_SESSION_TIMEOUT) {
            this.setSessionTimeout((Long) value);
            return true;
        } else if (option == RakChannelOption.RAK_COMPATIBILITY_MODE) {
            this.setCompatibilityMode((Boolean) value);
            return true;
        } else if (option == RakChannelOption.RAK_MTU_SIZES) {
            this.setMtuSizes((Integer[]) value);
            return true;
        } else if (option == RakChannelOption.RAK_CLIENT_INTERNAL_ADDRESSES) {
            this.setClientInternalAddresses((Integer) value);
            return true;
        } else if (option == RakChannelOption.RAK_TIME_BETWEEN_SEND_CONNECTION_ATTEMPTS_MS) {
            this.setTimeBetweenSendConnectionAttemptsMS((Integer) value);
            return true;
        }
        return super.setOption(option, value);
    }

    public ByteBuf getUnconnectedMagic() {
        return this.unconnectedMagic.slice();
    }

    public RakServerChannelConfig setUnconnectedMagic(ByteBuf unconnectedMagic) {
        if (unconnectedMagic.readableBytes() < 16) {
            throw new IllegalArgumentException("Unconnect magic must at least be 16 bytes");
        }
        this.unconnectedMagic = unconnectedMagic.copy().asReadOnly();
        return null;
    }

    public long getConnectTimeout() {
        return this.connectTimeout;
    }

    public DefaultRakClientConfig setConnectTimeout(long connectTimeout) {
        this.connectTimeout = connectTimeout;
        return this;
    }

    public long getServerGuid() {
        return this.serverGuid;
    }

    public DefaultRakClientConfig setServerGuid(long serverGuid) {
        this.serverGuid = serverGuid;
        return this;
    }

    @Override
    public RakChannelConfig setSessionTimeout(long timeout) {
        this.sessionTimeout = timeout;
        return this;
    }

    @Override
    public long getSessionTimeout() {
        return this.sessionTimeout;
    }

    public boolean isCompatibilityMode() {
        return this.compatibilityMode;
    }

    public void setCompatibilityMode(boolean enable) {
        this.compatibilityMode = enable;
    }

    public Integer[] getMtuSizes() {
        return this.mtuSizes.clone();
    }

    public void setMtuSizes(Integer[] mtuSizes) {
        this.mtuSizes = mtuSizes.clone();
    }

    public int getClientInternalAddresses() {
        return this.clientInternalAddresses;
    }

    public void setClientInternalAddresses(int clientInternalAddresses) {
        this.clientInternalAddresses = clientInternalAddresses;
    }

    public int getTimeBetweenSendConnectionAttemptsMS() {
        return this.timeBetweenSendConnectionAttemptsMS;
    }

    public void setTimeBetweenSendConnectionAttemptsMS(int timeBetweenSendConnectionAttemptsMS) {
        this.timeBetweenSendConnectionAttemptsMS = timeBetweenSendConnectionAttemptsMS;
    }
}
