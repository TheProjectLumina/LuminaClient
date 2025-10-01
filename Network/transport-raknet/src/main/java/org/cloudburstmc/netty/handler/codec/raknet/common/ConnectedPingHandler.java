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

package org.cloudburstmc.netty.handler.codec.raknet.common;

import static org.cloudburstmc.netty.channel.raknet.RakConstants.ID_CONNECTED_PING;
import static org.cloudburstmc.netty.channel.raknet.RakConstants.ID_CONNECTED_PONG;

import org.cloudburstmc.netty.channel.raknet.RakPriority;
import org.cloudburstmc.netty.channel.raknet.RakReliability;
import org.cloudburstmc.netty.channel.raknet.packet.EncapsulatedPacket;
import org.cloudburstmc.netty.channel.raknet.packet.RakMessage;
import org.cloudburstmc.netty.handler.codec.raknet.AdvancedChannelInboundHandler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

public class ConnectedPingHandler extends AdvancedChannelInboundHandler<EncapsulatedPacket> {
    public static final String NAME = "rak-connected-ping-handler";

    @Override
    protected boolean acceptInboundMessage(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (!super.acceptInboundMessage(ctx, msg)) {
            return false;
        }

        ByteBuf buf = ((EncapsulatedPacket) msg).getBuffer();
        return buf.getUnsignedByte(buf.readerIndex()) == ID_CONNECTED_PING;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, EncapsulatedPacket packet) throws Exception {
        ByteBuf buf = packet.getBuffer();
        buf.readUnsignedByte(); // Packet ID
        long pingTime = buf.readLong();

        ByteBuf replyBuffer = ctx.alloc().ioBuffer(17);
        replyBuffer.writeByte(ID_CONNECTED_PONG);
        replyBuffer.writeLong(pingTime);
        replyBuffer.writeLong(System.currentTimeMillis());
        ctx.writeAndFlush(new RakMessage(replyBuffer, RakReliability.UNRELIABLE, RakPriority.IMMEDIATE));
    }
}
