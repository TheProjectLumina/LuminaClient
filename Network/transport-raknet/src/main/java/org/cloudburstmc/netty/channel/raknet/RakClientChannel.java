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

package org.cloudburstmc.netty.channel.raknet;

import org.cloudburstmc.netty.channel.proxy.ProxyChannel;
import org.cloudburstmc.netty.channel.raknet.config.DefaultRakClientConfig;
import org.cloudburstmc.netty.channel.raknet.config.RakChannelConfig;
import org.cloudburstmc.netty.handler.codec.raknet.ProxyInboundRouter;
import org.cloudburstmc.netty.handler.codec.raknet.client.RakClientProxyRouteHandler;
import org.cloudburstmc.netty.handler.codec.raknet.client.RakClientRouteHandler;
import org.cloudburstmc.netty.handler.codec.raknet.common.UnconnectedPingEncoder;
import org.cloudburstmc.netty.handler.codec.raknet.common.UnconnectedPongDecoder;

import io.netty.channel.ChannelMetadata;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import io.netty.channel.socket.DatagramChannel;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

public class RakClientChannel extends ProxyChannel<DatagramChannel> implements RakChannel {

    private static final InternalLogger log = InternalLoggerFactory.getInstance(RakClientChannel.class);
    private static final ChannelMetadata metadata = new ChannelMetadata(true);

    /**
     * Implementation of simple RakClient which is able to connect to only one server during lifetime.
     */
    private final RakChannelConfig config;
    private final ChannelPromise connectPromise;

    public RakClientChannel(DatagramChannel channel) {
        super(channel);
        this.config = new DefaultRakClientConfig(this);

        this.pipeline().addLast(RakClientRouteHandler.NAME, new RakClientRouteHandler(this));
        // Transforms DatagramPacket to ByteBuf if channel has been already connected
        this.rakPipeline().addFirst(RakClientProxyRouteHandler.NAME, new RakClientProxyRouteHandler(this));
        // Encodes to buffer and sends RakPing.
        this.rakPipeline().addBefore(ProxyInboundRouter.NAME, UnconnectedPingEncoder.NAME, new UnconnectedPingEncoder(this));
        // Decodes received unconnected pong to RakPong.
        this.rakPipeline().addAfter(UnconnectedPingEncoder.NAME, UnconnectedPongDecoder.NAME, new UnconnectedPongDecoder(this));

        this.connectPromise = this.newPromise();
        this.connectPromise.addListener(future -> {
            if (future.isSuccess()) {
                this.onConnectionEstablished();
            } else {
                this.close();
            }
        });
    }

    /**
     * Setup online phase handlers
     */
    private void onConnectionEstablished() {
        // Send fireChannelActive() to user pipeline
        this.pipeline().fireChannelActive();
    }

    @Override
    public RakChannelConfig config() {
        return this.config;
    }

    public ChannelPromise getConnectPromise() {
        return this.connectPromise;
    }

    @Override
    public boolean isActive() {
        return super.isActive() && this.connectPromise.isSuccess();
    }

    @Override
    public ChannelPipeline rakPipeline() {
        return this.parent().pipeline();
    }

    @Override
    public ChannelMetadata metadata() {
        return metadata;
    }
}
