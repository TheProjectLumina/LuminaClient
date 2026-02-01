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

import static org.cloudburstmc.netty.channel.raknet.RakConstants.ID_UNCONNECTED_PING;

import org.cloudburstmc.netty.channel.raknet.RakClientChannel;
import org.cloudburstmc.netty.channel.raknet.RakPing;
import org.cloudburstmc.netty.channel.raknet.config.RakChannelOption;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.channel.socket.DatagramPacket;

public class UnconnectedPingEncoder extends ChannelOutboundHandlerAdapter {
    public static final String NAME = "rak-unconnected-ping-encoder";

    private final RakClientChannel channel;

    public UnconnectedPingEncoder(RakClientChannel channel) {
        this.channel = channel;
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (!(msg instanceof RakPing)) {
            ctx.write(msg, promise);
            return;
        }

        RakPing ping = (RakPing) msg;
        ByteBuf magicBuf = this.channel.config().getOption(RakChannelOption.RAK_UNCONNECTED_MAGIC);
        long guid = this.channel.config().getOption(RakChannelOption.RAK_GUID);

        ByteBuf pingBuffer = ctx.alloc().ioBuffer(magicBuf.readableBytes() + 17);
        pingBuffer.writeByte(ID_UNCONNECTED_PING);
        pingBuffer.writeLong(ping.getPingTime());
        pingBuffer.writeBytes(magicBuf, magicBuf.readerIndex(), magicBuf.readableBytes());
        pingBuffer.writeLong(guid);
        ctx.write(new DatagramPacket(pingBuffer, ping.getSender()), promise);
    }
}
