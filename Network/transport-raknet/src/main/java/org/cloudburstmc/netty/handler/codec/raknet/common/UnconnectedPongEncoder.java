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

import org.cloudburstmc.netty.channel.raknet.RakPong;
import org.cloudburstmc.netty.channel.raknet.config.RakChannelOption;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.channel.socket.DatagramPacket;

@Sharable
public class UnconnectedPongEncoder extends ChannelOutboundHandlerAdapter {
    public static final UnconnectedPongEncoder INSTANCE = new UnconnectedPongEncoder();
    public static final String NAME = "rak-unconnected-pong-encoder";

    private UnconnectedPongEncoder() {
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (!(msg instanceof RakPong)) {
            ctx.write(msg, promise);
            return;
        }

        RakPong pong = (RakPong) msg;
        ByteBuf magicBuf = ctx.channel().config().getOption(RakChannelOption.RAK_UNCONNECTED_MAGIC);
        long guid = ctx.channel().config().getOption(RakChannelOption.RAK_GUID);

        ByteBuf pongData = pong.getPongData();
        ByteBuf pongBuffer = ctx.alloc().ioBuffer(magicBuf.readableBytes() + 19 + pongData.readableBytes());
        pongBuffer.writeByte(ID_UNCONNECTED_PONG);
        pongBuffer.writeLong(pong.getPingTime());
        pongBuffer.writeLong(guid);
        pongBuffer.writeBytes(magicBuf, magicBuf.readerIndex(), magicBuf.readableBytes());
        pongBuffer.writeShort(pongData.readableBytes());
        pongBuffer.writeBytes(pongData, pongData.readerIndex(), pongData.readableBytes());
        ctx.write(new DatagramPacket(pongBuffer, pong.getSender()), promise);
    }
}
