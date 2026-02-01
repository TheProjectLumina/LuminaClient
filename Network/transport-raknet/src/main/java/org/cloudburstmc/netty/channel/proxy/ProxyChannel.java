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

package org.cloudburstmc.netty.channel.proxy;

import org.cloudburstmc.netty.handler.codec.raknet.ProxyInboundRouter;
import org.cloudburstmc.netty.handler.codec.raknet.ProxyOutboundRouter;
import org.cloudburstmc.netty.util.RakUtils;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Objects;

import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelConfig;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelId;
import io.netty.channel.ChannelMetadata;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelProgressivePromise;
import io.netty.channel.ChannelPromise;
import io.netty.channel.DefaultChannelPipeline;
import io.netty.channel.EventLoop;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;

public abstract class ProxyChannel<T extends Channel> implements Channel {

    /**
     * TODO: consider config mixing with parent
     */
    protected final T channel;
    protected final ChannelPipeline pipeline;

    protected ProxyChannel(T parent) {
        Objects.requireNonNull(parent, "parent");
        this.channel = parent;
        this.pipeline = this.newChannelPipeline();
        parent.pipeline().addLast(ProxyInboundRouter.NAME, new ProxyInboundRouter(this));
        this.pipeline.addLast(ProxyOutboundRouter.NAME, new ProxyOutboundRouter(this));
    }

    public void onCloseTriggered(ChannelPromise promise) {
        this.channel.close(this.correctPromise(promise));
    }

    public ChannelPromise correctPromise(ChannelPromise remotePromise) {
        ChannelPromise localPromise = this.channel.newPromise();
        localPromise.addListener(future -> {
            if (future.isSuccess()) {
                remotePromise.trySuccess();
            } else {
                remotePromise.tryFailure(future.cause());
            }
        });
        return localPromise;
    }

    protected DefaultChannelPipeline newChannelPipeline() {
        return RakUtils.newChannelPipeline(this);
    }

    protected final ChannelPipeline internalPipeline() {
        return this.channel.pipeline();
    }

    @Override
    public ChannelId id() {
        return this.channel.id();
    }

    @Override
    public EventLoop eventLoop() {
        return this.channel.eventLoop();
    }

    @Override
    public Channel parent() {
        return this.channel;
    }

    @Override
    public ChannelConfig config() {
        return this.channel.config();
    }

    @Override
    public boolean isOpen() {
        return this.channel.isOpen();
    }

    @Override
    public boolean isRegistered() {
        return this.channel.isRegistered();
    }

    @Override
    public boolean isActive() {
        return this.channel.isActive();
    }

    @Override
    public ChannelMetadata metadata() {
        return this.channel.metadata();
    }

    @Override
    public InetSocketAddress localAddress() {
        return (InetSocketAddress) this.channel.localAddress();
    }

    @Override
    public InetSocketAddress remoteAddress() {
        return (InetSocketAddress) this.channel.remoteAddress();
    }

    @Override
    public ChannelFuture closeFuture() {
        return this.channel.closeFuture();
    }

    @Override
    public boolean isWritable() {
        return this.channel.isWritable();
    }

    @Override
    public long bytesBeforeUnwritable() {
        return this.channel.bytesBeforeUnwritable();
    }

    @Override
    public long bytesBeforeWritable() {
        return this.channel.bytesBeforeWritable();
    }

    @Override
    public Unsafe unsafe() {
        return this.channel.unsafe();
    }

    @Override
    public ChannelPipeline pipeline() {
        return this.pipeline;
    }

    @Override
    public ByteBufAllocator alloc() {
        return this.config().getAllocator();
    }

    @Override
    public ChannelFuture bind(SocketAddress localAddress) {
        return this.pipeline.bind(localAddress);
    }

    @Override
    public ChannelFuture connect(SocketAddress remoteAddress) {
        return this.pipeline.connect(remoteAddress);
    }

    @Override
    public ChannelFuture connect(SocketAddress remoteAddress, SocketAddress localAddress) {
        return this.pipeline.connect(remoteAddress, localAddress);
    }

    @Override
    public ChannelFuture disconnect() {
        return this.pipeline.disconnect();
    }

    @Override
    public ChannelFuture close() {
        return this.pipeline.close();
    }

    @Override
    public ChannelFuture deregister() {
        return this.pipeline.deregister();
    }

    @Override
    public ChannelFuture bind(SocketAddress localAddress, ChannelPromise promise) {
        return this.pipeline.bind(localAddress, promise);
    }

    @Override
    public ChannelFuture connect(SocketAddress remoteAddress, ChannelPromise promise) {
        return this.pipeline.connect(remoteAddress, promise);
    }

    @Override
    public ChannelFuture connect(SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) {
        return this.pipeline.connect(remoteAddress, localAddress, promise);
    }

    @Override
    public ChannelFuture disconnect(ChannelPromise promise) {
        return this.pipeline.disconnect(promise);
    }

    @Override
    public ChannelFuture close(ChannelPromise promise) {
        return this.pipeline.close(promise);
    }

    @Override
    public ChannelFuture deregister(ChannelPromise promise) {
        return this.pipeline.deregister(promise);
    }

    @Override
    public Channel read() {
        this.pipeline.read();
        return this;
    }

    @Override
    public ChannelFuture write(Object msg) {
        return this.pipeline.write(msg);
    }

    @Override
    public ChannelFuture write(Object msg, ChannelPromise promise) {
        return this.pipeline.write(msg, promise);
    }

    @Override
    public Channel flush() {
        this.pipeline.flush();
        return this;
    }

    @Override
    public ChannelFuture writeAndFlush(Object msg, ChannelPromise promise) {
        return this.pipeline.writeAndFlush(msg, promise);
    }

    @Override
    public ChannelFuture writeAndFlush(Object msg) {
        return this.pipeline.writeAndFlush(msg);
    }

    @Override
    public ChannelPromise newPromise() {
        return this.pipeline.newPromise();
    }

    @Override
    public ChannelProgressivePromise newProgressivePromise() {
        return this.pipeline.newProgressivePromise();
    }

    @Override
    public ChannelFuture newSucceededFuture() {
        return this.pipeline.newSucceededFuture();
    }

    @Override
    public ChannelFuture newFailedFuture(Throwable cause) {
        return this.pipeline.newFailedFuture(cause);
    }

    @Override
    public ChannelPromise voidPromise() {
        return this.pipeline.voidPromise();
    }

    @Override
    public <U> Attribute<U> attr(AttributeKey<U> key) {
        return this.channel.attr(key);
    }

    @Override
    public <U> boolean hasAttr(AttributeKey<U> key) {
        return this.channel.hasAttr(key);
    }

    @Override
    public int compareTo(Channel o) {
        return this.channel.compareTo(o);
    }
}
