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

package org.cloudburstmc.netty.handler.codec.raknet.server;

import org.cloudburstmc.netty.channel.raknet.RakChildChannel;
import org.cloudburstmc.netty.channel.raknet.config.RakChannelMetrics;

import java.nio.channels.ClosedChannelException;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.channel.socket.DatagramPacket;

public class RakChildDatagramHandler extends ChannelOutboundHandlerAdapter {

    public static final String NAME = "rak-child-datagram-handler";
    private final RakChildChannel channel;
    private volatile boolean canFlush = false;

    public RakChildDatagramHandler(RakChildChannel channel) {
        this.channel = channel;
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        boolean isDatagram = msg instanceof DatagramPacket;
        if (!isDatagram && !(msg instanceof ByteBuf)) {
            ctx.write(msg, promise);
            return;
        }

        this.canFlush = true;
        promise.trySuccess();
        DatagramPacket datagram = isDatagram ? (DatagramPacket) msg :
                new DatagramPacket((ByteBuf) msg, this.channel.remoteAddress(), this.channel.localAddress());

        RakChannelMetrics metrics = this.channel.config().getMetrics();
        if (metrics != null) {
            metrics.bytesOut(datagram.content().readableBytes());
        }

        Channel parent = this.channel.parent().parent();

        parent.write(datagram).addListener((ChannelFuture future) -> {
            if (!future.isSuccess() && !(future.cause() instanceof ClosedChannelException)) {
                this.channel.pipeline().fireExceptionCaught(future.cause());
                this.channel.close();
            }
        });
    }

    @Override
    public void flush(ChannelHandlerContext ctx) throws Exception {
        if (this.canFlush) {
            this.canFlush = false;
            ctx.flush();
        }
    }
}
