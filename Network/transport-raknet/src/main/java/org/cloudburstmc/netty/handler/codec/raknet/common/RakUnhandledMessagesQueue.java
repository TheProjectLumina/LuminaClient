/*
 * Copyright 2023 CloudburstMC
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

package org.cloudburstmc.netty.handler.codec.raknet.common;

import org.cloudburstmc.netty.channel.raknet.RakChannel;
import org.cloudburstmc.netty.channel.raknet.packet.EncapsulatedPacket;

import java.util.Queue;
import java.util.concurrent.TimeUnit;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.concurrent.ScheduledFuture;
import io.netty.util.internal.PlatformDependent;

public class RakUnhandledMessagesQueue extends SimpleChannelInboundHandler<EncapsulatedPacket> {
    public static final String NAME = "rak-unhandled-messages-queue";

    private final RakChannel channel;
    private final Queue<EncapsulatedPacket> messages = PlatformDependent.newMpscQueue();
    private ScheduledFuture<?> future;

    public RakUnhandledMessagesQueue(RakChannel channel) {
        this.channel = channel;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        this.future = ctx.channel().eventLoop().scheduleAtFixedRate(() -> this.trySendMessages(ctx),
                0, 50, TimeUnit.MILLISECONDS);
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        if (this.future != null) {
            this.future.cancel(false);
            this.future = null;
        }

        EncapsulatedPacket message;
        while ((message = this.messages.poll()) != null) {
            ReferenceCountUtil.release(message);
        }
    }

    private void trySendMessages(ChannelHandlerContext ctx) {
        if (!this.channel.isActive()) {
            return;
        }

        EncapsulatedPacket message;
        while ((message = this.messages.poll()) != null) {
            ctx.fireChannelRead(message);
        }

        ctx.pipeline().remove(this);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, EncapsulatedPacket msg) throws Exception {
        if (!this.channel.isActive()) {
            this.messages.offer(msg.retain());
            return;
        }

        this.trySendMessages(ctx);
        ctx.fireChannelRead(msg.retain());
    }
}
