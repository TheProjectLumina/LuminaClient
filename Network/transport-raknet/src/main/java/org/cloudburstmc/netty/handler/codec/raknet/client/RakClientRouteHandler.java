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

package org.cloudburstmc.netty.handler.codec.raknet.client;

import org.cloudburstmc.netty.channel.raknet.RakClientChannel;
import org.cloudburstmc.netty.handler.codec.raknet.common.UnconnectedPongDecoder;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.util.concurrent.PromiseCombiner;

public class RakClientRouteHandler extends ChannelDuplexHandler {

    public static final String NAME = "rak-client-route-handler";
    private final RakClientChannel channel;

    public RakClientRouteHandler(RakClientChannel channel) {
        this.channel = channel;
    }

    @Override
    public void connect(ChannelHandlerContext ctx, SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) throws Exception {
        if (!(remoteAddress instanceof InetSocketAddress)) {
            promise.tryFailure(new IllegalArgumentException("Provided remote address must be InetSocketAddress"));
            return;
        }

        if (this.channel.parent().isActive()) {
            throw new IllegalStateException("Channel is already bound!");
        }

        ChannelFuture parentFuture = this.channel.parent().connect(remoteAddress, localAddress);
        parentFuture.addListener(future -> {
            if (future.isSuccess()) {
                this.channel.rakPipeline().addAfter(UnconnectedPongDecoder.NAME,
                        RakClientOfflineHandler.NAME, new RakClientOfflineHandler(channel, this.channel.getConnectPromise()));
            }
        });

        PromiseCombiner combiner = new PromiseCombiner(this.channel.eventLoop());
        combiner.add(parentFuture);
        combiner.add((ChannelFuture) this.channel.getConnectPromise());
        combiner.finish(promise);
    }

    @Override
    public void read(ChannelHandlerContext ctx) throws Exception {
        // Ignore
    }
}
