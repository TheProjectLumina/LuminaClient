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

import static org.cloudburstmc.netty.channel.raknet.RakConstants.ID_UNCONNECTED_PONG;

import org.cloudburstmc.netty.channel.raknet.RakClientChannel;
import org.cloudburstmc.netty.channel.raknet.RakPong;
import org.cloudburstmc.netty.channel.raknet.config.RakChannelOption;
import org.cloudburstmc.netty.handler.codec.raknet.AdvancedChannelInboundHandler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;

public class UnconnectedPongDecoder extends AdvancedChannelInboundHandler<DatagramPacket> {
    public static final String NAME = "rak-unconnected-pong-deencoder";

    private final RakClientChannel channel;

    public UnconnectedPongDecoder(RakClientChannel channel) {
        this.channel = channel;
    }

    @Override
    protected boolean acceptInboundMessage(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (!super.acceptInboundMessage(ctx, msg)) {
            return false;
        }

        DatagramPacket packet = (DatagramPacket) msg;
        ByteBuf buf = packet.content();
        return buf.isReadable() && buf.getUnsignedByte(buf.readerIndex()) == ID_UNCONNECTED_PONG;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket packet) throws Exception {
        ByteBuf buf = packet.content();
        buf.readUnsignedByte(); // Packet ID

        long pingTime = buf.readLong();
        long guid = buf.readLong();

        ByteBuf magicBuf = this.channel.config().getOption(RakChannelOption.RAK_UNCONNECTED_MAGIC);
        if (!buf.isReadable(magicBuf.readableBytes()) || !ByteBufUtil.equals(buf.readSlice(magicBuf.readableBytes()), magicBuf)) {
            // Magic does not match
            return;
        }

        ByteBuf pongData = Unpooled.EMPTY_BUFFER;
        if (buf.isReadable(2)) { // Length
            pongData = buf.readRetainedSlice(buf.readUnsignedShort());
        }
        ctx.fireChannelRead(new RakPong(pingTime, guid, pongData, packet.sender()));
    }
}
