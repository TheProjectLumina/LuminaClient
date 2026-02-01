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


import static org.cloudburstmc.netty.channel.raknet.RakConstants.ID_CONNECTION_REQUEST;
import static org.cloudburstmc.netty.channel.raknet.RakConstants.ID_CONNECTION_REQUEST_ACCEPTED;
import static org.cloudburstmc.netty.channel.raknet.RakConstants.ID_CONNECTION_REQUEST_FAILED;
import static org.cloudburstmc.netty.channel.raknet.RakConstants.ID_NEW_INCOMING_CONNECTION;
import static org.cloudburstmc.netty.channel.raknet.RakConstants.IPV4_MESSAGE_SIZE;
import static org.cloudburstmc.netty.channel.raknet.RakConstants.LOCAL_ADDRESS;

import org.cloudburstmc.netty.channel.raknet.RakChannel;
import org.cloudburstmc.netty.channel.raknet.RakPriority;
import org.cloudburstmc.netty.channel.raknet.RakReliability;
import org.cloudburstmc.netty.channel.raknet.config.RakChannelOption;
import org.cloudburstmc.netty.channel.raknet.packet.EncapsulatedPacket;
import org.cloudburstmc.netty.channel.raknet.packet.RakMessage;
import org.cloudburstmc.netty.util.RakUtils;

import java.net.InetSocketAddress;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.SimpleChannelInboundHandler;

public class RakClientOnlineInitialHandler extends SimpleChannelInboundHandler<EncapsulatedPacket> {
    public static final String NAME = "rak-client-online-initial-handler";

    private final RakChannel rakChannel;
    private final ChannelPromise successPromise;

    public RakClientOnlineInitialHandler(RakChannel rakChannel, ChannelPromise promise) {
        this.rakChannel = rakChannel;
        this.successPromise = promise;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        this.sendConnectionRequest(ctx);
    }

    private void sendConnectionRequest(ChannelHandlerContext ctx) {
        long guid = this.rakChannel.config().getOption(RakChannelOption.RAK_GUID);

        ByteBuf buffer = ctx.alloc().ioBuffer(18);
        buffer.writeByte(ID_CONNECTION_REQUEST);
        buffer.writeLong(guid);
        buffer.writeLong(System.currentTimeMillis());
        buffer.writeBoolean(false);
        ctx.writeAndFlush(new RakMessage(buffer, RakReliability.RELIABLE_ORDERED, RakPriority.IMMEDIATE));
    }

    private void onSuccess(ChannelHandlerContext ctx) {
        // At this point connection is fully initialized.
        Channel channel = ctx.channel();
        channel.pipeline().remove(RakClientOfflineHandler.NAME);
        channel.pipeline().remove(RakClientOnlineInitialHandler.NAME);
        this.successPromise.trySuccess();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, EncapsulatedPacket message) throws Exception {
        ByteBuf buf = message.getBuffer();
        int packetId = buf.getUnsignedByte(buf.readerIndex());

        switch (packetId) {
            case ID_CONNECTION_REQUEST_ACCEPTED:
                this.onConnectionRequestAccepted(ctx, buf);
                this.onSuccess(ctx);
                break;
            case ID_CONNECTION_REQUEST_FAILED:
                this.successPromise.tryFailure(new IllegalStateException("Connection denied"));
                break;
            default:
                ctx.fireChannelRead(message.retain());
                break;
        }
    }

    private void onConnectionRequestAccepted(ChannelHandlerContext ctx, ByteBuf buf) {
        buf.skipBytes(1);

        boolean compatibilityMode = this.rakChannel.config().getOption(RakChannelOption.RAK_COMPATIBILITY_MODE);
        if (compatibilityMode) {
            RakUtils.skipAddress(buf); // Client address
        } else {
            RakUtils.readAddress(buf); // Client address
        }

        buf.readUnsignedShort(); // System index

        // Address + 2 * Long - Minimum amount of data
        int required = IPV4_MESSAGE_SIZE + 16;

        long pingTime = 0;
        while (buf.isReadable(required)) {
            if (compatibilityMode) {
                RakUtils.skipAddress(buf);
            } else {
                RakUtils.readAddress(buf);
            }
        }
        pingTime = buf.readLong();
        buf.readLong();

        ByteBuf buffer = ctx.alloc().ioBuffer();
        buffer.writeByte(ID_NEW_INCOMING_CONNECTION);
        RakUtils.writeAddress(buffer, (InetSocketAddress) ctx.channel().remoteAddress());
        for (int i = 0; i < this.rakChannel.config().getOption(RakChannelOption.RAK_CLIENT_INTERNAL_ADDRESSES); i++) {
            RakUtils.writeAddress(buffer, LOCAL_ADDRESS);
        }
        buffer.writeLong(pingTime);
        buffer.writeLong(System.currentTimeMillis());
        ctx.writeAndFlush(new RakMessage(buffer, RakReliability.RELIABLE_ORDERED, RakPriority.IMMEDIATE));
    }
}
