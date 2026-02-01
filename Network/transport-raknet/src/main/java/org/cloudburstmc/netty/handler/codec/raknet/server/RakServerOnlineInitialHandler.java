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

import static org.cloudburstmc.netty.channel.raknet.RakConstants.ID_CONNECTION_REQUEST;
import static org.cloudburstmc.netty.channel.raknet.RakConstants.ID_CONNECTION_REQUEST_ACCEPTED;
import static org.cloudburstmc.netty.channel.raknet.RakConstants.ID_CONNECTION_REQUEST_FAILED;
import static org.cloudburstmc.netty.channel.raknet.RakConstants.ID_NEW_INCOMING_CONNECTION;
import static org.cloudburstmc.netty.channel.raknet.RakConstants.LOCAL_IP_ADDRESSES_V4;
import static org.cloudburstmc.netty.channel.raknet.RakConstants.LOCAL_IP_ADDRESSES_V6;

import org.cloudburstmc.netty.channel.raknet.RakChildChannel;
import org.cloudburstmc.netty.channel.raknet.RakDisconnectReason;
import org.cloudburstmc.netty.channel.raknet.RakPriority;
import org.cloudburstmc.netty.channel.raknet.RakReliability;
import org.cloudburstmc.netty.channel.raknet.config.RakChannelOption;
import org.cloudburstmc.netty.channel.raknet.config.RakServerChannelConfig;
import org.cloudburstmc.netty.channel.raknet.config.RakServerMetrics;
import org.cloudburstmc.netty.channel.raknet.packet.EncapsulatedPacket;
import org.cloudburstmc.netty.channel.raknet.packet.RakMessage;
import org.cloudburstmc.netty.handler.codec.raknet.common.RakSessionCodec;
import org.cloudburstmc.netty.util.RakUtils;

import java.net.Inet6Address;
import java.net.InetSocketAddress;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

@Sharable
public class RakServerOnlineInitialHandler extends SimpleChannelInboundHandler<EncapsulatedPacket> {
    public static final String NAME = "rak-server-online-initial-handler";
    private static final InternalLogger log = InternalLoggerFactory.getInstance(RakServerOnlineInitialHandler.class);

    private final RakChildChannel channel;

    public RakServerOnlineInitialHandler(RakChildChannel channel) {
        this.channel = channel;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, EncapsulatedPacket message) throws Exception {
        ByteBuf buf = message.getBuffer();
        int packetId = buf.getUnsignedByte(buf.readerIndex());

        RakServerMetrics metrics = this.channel.parent().config().getOption(RakChannelOption.RAK_SERVER_METRICS);

        switch (packetId) {
            case ID_CONNECTION_REQUEST:
                if (metrics != null) metrics.connectionInitPacket(this.channel.remoteAddress(), ID_CONNECTION_REQUEST);
                this.onConnectionRequest(ctx, buf);
                break;
            case ID_NEW_INCOMING_CONNECTION:
                if (metrics != null) metrics.connectionInitPacket(this.channel.remoteAddress(), ID_NEW_INCOMING_CONNECTION);

                buf.skipBytes(1);
                // We have connected and no longer need this handler
                ctx.pipeline().remove(this);
                channel.eventLoop().execute(() -> {
                    channel.setActive(true); // make sure this is set on same thread
                    channel.pipeline().fireChannelActive();
                });
                break;
            default:
                ctx.fireChannelRead(message.retain());
                break;
        }
    }

    private void onConnectionRequest(ChannelHandlerContext ctx, ByteBuf buffer) {
        buffer.skipBytes(1);
        long guid = this.channel.config().getGuid();
        long serverGuid = buffer.readLong();
        long timestamp = buffer.readLong();
        boolean security = buffer.readBoolean();

        if (serverGuid != guid || security) {
            this.sendConnectionRequestFailed(ctx, guid);
        } else {
            this.sendConnectionRequestAccepted(ctx, timestamp);
        }
    }

    private void sendConnectionRequestAccepted(ChannelHandlerContext ctx, long time) {
        InetSocketAddress address = this.channel.remoteAddress();
        boolean ipv6 = address.getAddress() instanceof Inet6Address;
        ByteBuf outBuf = ctx.alloc().ioBuffer(ipv6 ? 628 : 166);

        outBuf.writeByte(ID_CONNECTION_REQUEST_ACCEPTED);
        RakUtils.writeAddress(outBuf, address);
        outBuf.writeShort(0); // System index
        for (InetSocketAddress socketAddress : ipv6 ? LOCAL_IP_ADDRESSES_V6 : LOCAL_IP_ADDRESSES_V4) {
            RakUtils.writeAddress(outBuf, socketAddress);
        }
        outBuf.writeLong(time);
        outBuf.writeLong(System.currentTimeMillis());

        ctx.writeAndFlush(new RakMessage(outBuf, RakReliability.UNRELIABLE, RakPriority.IMMEDIATE));
    }

    private void sendConnectionRequestFailed(ChannelHandlerContext ctx, long guid) {
        ByteBuf magicBuf = ((RakServerChannelConfig) ctx.channel().config()).getUnconnectedMagic();
        int length = 9 + magicBuf.readableBytes();

        ByteBuf reply = ctx.alloc().ioBuffer(length);
        reply.writeByte(ID_CONNECTION_REQUEST_FAILED);
        reply.writeBytes(magicBuf, magicBuf.readerIndex(), magicBuf.readableBytes());
        reply.writeLong(guid);

        sendRaw(ctx, reply);
        ctx.fireUserEventTriggered(RakDisconnectReason.CONNECTION_REQUEST_FAILED).close();
    }

    private void sendRaw(ChannelHandlerContext ctx, ByteBuf buf) {
        ctx.pipeline().context(RakSessionCodec.NAME).writeAndFlush(buf);
    }
}
