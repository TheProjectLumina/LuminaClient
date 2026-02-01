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

import static org.cloudburstmc.netty.channel.raknet.RakConstants.FLAG_ACK;
import static org.cloudburstmc.netty.channel.raknet.RakConstants.FLAG_NACK;
import static org.cloudburstmc.netty.channel.raknet.RakConstants.FLAG_VALID;

import org.cloudburstmc.netty.channel.raknet.RakDisconnectReason;
import org.cloudburstmc.netty.channel.raknet.config.RakChannelMetrics;
import org.cloudburstmc.netty.util.IntRange;

import java.util.Queue;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

public class RakAcknowledgeHandler extends SimpleChannelInboundHandler<ByteBuf> {

    private static final InternalLogger log = InternalLoggerFactory.getInstance(RakAcknowledgeHandler.class);
    public static final String NAME = "rak-acknowledge-handler";

    private final RakSessionCodec sessionCodec;

    public RakAcknowledgeHandler(RakSessionCodec sessionCodec) {
        this.sessionCodec = sessionCodec;
    }

    @Override
    public boolean acceptInboundMessage(Object msg) throws Exception {
        if (!super.acceptInboundMessage(msg)) {
            return false;
        }

        ByteBuf buffer = (ByteBuf) msg;
        byte potentialFlags = buffer.getByte(buffer.readerIndex());
        if ((potentialFlags & FLAG_VALID) == 0) {
            return false;
        }

        return (potentialFlags & FLAG_ACK) != 0 || (potentialFlags & FLAG_NACK) != 0;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf buffer) throws Exception {
        boolean nack = (buffer.readByte() & FLAG_NACK) != 0;
        int entriesCount = buffer.readUnsignedShort();

        Queue<IntRange> queue = this.sessionCodec.getAcknowledgeQueue(nack);
        for (int i = 0; i < entriesCount; i++) {
            boolean singleton = buffer.readBoolean();
            int start = buffer.readUnsignedMediumLE();
            // We don't need the upper limit if it's a singleton
            int end = singleton ? start : buffer.readUnsignedMediumLE();

            if (start <= end) {
                queue.offer(new IntRange(start, end));
                continue;
            }

            if (log.isTraceEnabled()) {
                log.trace("{} sent an IntRange with a start value {} greater than an end value of {}", sessionCodec.getChannel().remoteAddress(), start, end);
            }
            this.sessionCodec.disconnect(RakDisconnectReason.BAD_PACKET);
            return;
        }

        RakChannelMetrics metrics = this.sessionCodec.getMetrics();
        if (metrics != null) {
            if (nack) {
                metrics.nackIn(entriesCount);
            } else {
                metrics.ackIn(entriesCount);
            }
        }
    }
}
