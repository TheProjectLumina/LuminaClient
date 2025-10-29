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

package org.cloudburstmc.netty.handler.codec.raknet;

import org.cloudburstmc.netty.channel.proxy.ProxyChannel;

import java.net.PortUnreachableException;
import java.net.SocketAddress;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandler;
import io.netty.channel.ChannelPromise;

public class ProxyOutboundRouter implements ChannelOutboundHandler {

    public static final String NAME = "rak-proxy-outbound-router";
    private final ProxyChannel<?> proxiedChannel;

    public ProxyOutboundRouter(ProxyChannel<?> proxiedChannel) {
        this.proxiedChannel = proxiedChannel;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        // Ignore
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        // Ignore
    }

    @Override
    public void bind(ChannelHandlerContext ctx, SocketAddress address, ChannelPromise promise) throws Exception {
        this.proxiedChannel.parent().bind(address, this.proxiedChannel.correctPromise(promise));
    }

    @Override
    public void connect(ChannelHandlerContext ctx, SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) throws Exception {
        this.proxiedChannel.parent().connect(remoteAddress, localAddress, this.proxiedChannel.correctPromise(promise));
    }

    @Override
    public void close(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        this.proxiedChannel.onCloseTriggered(promise);
    }

    @Override
    public void disconnect(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        this.proxiedChannel.parent().disconnect(this.proxiedChannel.correctPromise(promise));
    }

    @Override
    public void deregister(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        this.proxiedChannel.parent().deregister(this.proxiedChannel.correctPromise(promise));
    }

    @Override
    public void read(ChannelHandlerContext ctx) throws Exception {
        this.proxiedChannel.parent().read();
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        this.proxiedChannel.parent().write(msg, this.proxiedChannel.correctPromise(promise));
    }

    @Override
    public void flush(ChannelHandlerContext ctx) throws Exception {
        this.proxiedChannel.parent().flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable throwable) throws Exception {
        if (!(throwable instanceof PortUnreachableException)) {
            ctx.fireExceptionCaught(throwable);
        }
    }
}
