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

import org.cloudburstmc.netty.channel.raknet.config.DefaultRakSessionConfig;
import org.cloudburstmc.netty.channel.raknet.config.RakChannelConfig;
import org.cloudburstmc.netty.handler.codec.raknet.common.ConnectedPingHandler;
import org.cloudburstmc.netty.handler.codec.raknet.common.ConnectedPongHandler;
import org.cloudburstmc.netty.handler.codec.raknet.common.DisconnectNotificationHandler;
import org.cloudburstmc.netty.handler.codec.raknet.common.RakAcknowledgeHandler;
import org.cloudburstmc.netty.handler.codec.raknet.common.RakDatagramCodec;
import org.cloudburstmc.netty.handler.codec.raknet.common.RakSessionCodec;
import org.cloudburstmc.netty.handler.codec.raknet.common.RakUnhandledMessagesQueue;
import org.cloudburstmc.netty.handler.codec.raknet.server.RakChildDatagramHandler;
import org.cloudburstmc.netty.handler.codec.raknet.server.RakServerOnlineInitialHandler;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.NonWritableChannelException;
import java.util.function.Consumer;

import io.netty.channel.AbstractChannel;
import io.netty.channel.ChannelMetadata;
import io.netty.channel.ChannelOutboundBuffer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import io.netty.channel.DefaultChannelPipeline;
import io.netty.channel.EventLoop;
import io.netty.util.ReferenceCountUtil;

public class RakChildChannel extends AbstractChannel implements RakChannel {

    private static final ChannelMetadata metadata = new ChannelMetadata(true);

    private final RakChannelConfig config;
    private final InetSocketAddress remoteAddress;
    private final InetSocketAddress localAddress;
    private final DefaultChannelPipeline rakPipeline;
    private volatile boolean open = true;
    private volatile boolean active;

    RakChildChannel(InetSocketAddress remoteAddress, InetSocketAddress localAddress, RakServerChannel parent, long guid, int version, int mtu, Consumer<RakChannel> childConsumer) {
        super(parent);
        this.remoteAddress = remoteAddress;
        this.localAddress = localAddress;
        this.config = new DefaultRakSessionConfig(this);
        this.config.setGuid(guid);
        this.config.setProtocolVersion(version);
        this.config.setMtu(mtu);
        // Allow user to configure the child channel before we initialize pipeline
        // This is not the same as bootstrap.childOption() as Bootstrap does not allow setting options per channel
        if (childConsumer != null) {
            childConsumer.accept(this);
        }
        // Create an internal pipeline for RakNet session logic to take place. We use the parent channel to ensure
        // this all occurs on the parent event loop so the connection is not slowed down by any user code.
        // (compression, encryption, etc.)
        this.rakPipeline = new RakChannelPipeline(parent, this);
        this.rakPipeline.addLast(RakChildDatagramHandler.NAME, new RakChildDatagramHandler(this));

        // Setup session/online phase
        RakSessionCodec sessionCodec = new RakSessionCodec(this);
        this.rakPipeline.addLast(RakDatagramCodec.NAME, new RakDatagramCodec());
        this.rakPipeline.addLast(RakAcknowledgeHandler.NAME, new RakAcknowledgeHandler(sessionCodec));
        this.rakPipeline.addLast(RakSessionCodec.NAME, sessionCodec);
        // This handler auto-removes once ConnectionRequest is received
        this.rakPipeline.addLast(ConnectedPingHandler.NAME, new ConnectedPingHandler());
        this.rakPipeline.addLast(ConnectedPongHandler.NAME, new ConnectedPongHandler(sessionCodec));
        this.rakPipeline.addLast(DisconnectNotificationHandler.NAME, DisconnectNotificationHandler.INSTANCE);
        this.rakPipeline.addLast(RakServerOnlineInitialHandler.NAME, new RakServerOnlineInitialHandler(this));
        this.rakPipeline.addLast(RakUnhandledMessagesQueue.NAME, new RakUnhandledMessagesQueue(this));
        this.rakPipeline.fireChannelRegistered();
        this.rakPipeline.fireChannelActive();
    }

    @Override
    public ChannelPipeline rakPipeline() {
        return rakPipeline;
    }

    @Override
    public SocketAddress localAddress0() {
        return this.localAddress;
    }

    @Override
    public SocketAddress remoteAddress0() {
        return this.remoteAddress;
    }

    @Override
    public InetSocketAddress localAddress() {
        return (InetSocketAddress) super.localAddress();
    }

    @Override
    public InetSocketAddress remoteAddress() {
        return (InetSocketAddress) super.remoteAddress();
    }

    @Override
    public RakChannelConfig config() {
        return this.config;
    }

    @Override
    public ChannelMetadata metadata() {
        return metadata;
    }

    @Override
    protected void doBind(SocketAddress socketAddress) throws Exception {
        throw new UnsupportedOperationException("Can not bind child channel!");
    }

    @Override
    protected void doBeginRead() throws Exception {
        // Ignore
    }

    @Override
    protected void doWrite(ChannelOutboundBuffer in) throws Exception {
        if (!this.open) {
            throw new ClosedChannelException();
        } else if (!active) {
            throw new NonWritableChannelException();
        }
        ClosedChannelException exception = null;
        for (; ; ) {
            Object msg = in.current();
            if (msg == null) {
                break;
            }
            try {
                if (this.parent().isOpen()) {
                    this.rakPipeline.write(ReferenceCountUtil.retain(msg));
                    in.remove();
                } else {
                    if (exception == null) {
                        exception = new ClosedChannelException();
                    }
                    in.remove(exception);
                }
            } catch (Throwable cause) {
                in.remove(cause);
            }
        }
        this.rakPipeline.flush();
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    protected void doDisconnect() throws Exception {
        this.close();
    }

    @Override
    protected void doClose() throws Exception {
        this.open = false;
    }

    @Override
    public boolean isActive() {
        return this.isOpen() && this.active;
    }

    @Override
    public boolean isOpen() {
        return this.open;
    }

    @Override
    protected boolean isCompatible(EventLoop eventLoop) {
        return true;
    }

    @Override
    protected AbstractUnsafe newUnsafe() {
        return new AbstractUnsafe() {
            @Override
            public void connect(SocketAddress socketAddress, SocketAddress socketAddress1, ChannelPromise channelPromise) {
                throw new UnsupportedOperationException("Can not connect child channel!");
            }
        };
    }
}
