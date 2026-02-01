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

import java.nio.channels.ClosedChannelException;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandler;

public class ProxyInboundRouter implements ChannelInboundHandler {

    public static final String NAME = "rak-proxy-inbound-router";
    private final ProxyChannel<?> proxiedChannel;

    public ProxyInboundRouter(ProxyChannel<?> proxiedChannel) {
        this.proxiedChannel = proxiedChannel;
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        this.proxiedChannel.pipeline().fireChannelRegistered();
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        this.proxiedChannel.pipeline().fireChannelUnregistered();
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        // Ignore
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // Ignore
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        this.proxiedChannel.pipeline().fireChannelInactive();
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        // Ignore
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        this.proxiedChannel.pipeline().fireChannelRead(msg);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        this.proxiedChannel.pipeline().fireChannelReadComplete();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object msg) throws Exception {
        this.proxiedChannel.pipeline().fireUserEventTriggered(msg);
    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        this.proxiedChannel.pipeline().fireChannelWritabilityChanged();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable throwable) throws Exception {
        if (!(throwable instanceof ClosedChannelException)) {
            this.proxiedChannel.pipeline().fireExceptionCaught(throwable);
        }
    }
}
