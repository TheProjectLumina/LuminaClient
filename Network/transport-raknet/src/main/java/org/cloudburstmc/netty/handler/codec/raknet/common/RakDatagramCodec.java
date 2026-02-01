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

import org.cloudburstmc.netty.channel.raknet.packet.EncapsulatedPacket;
import org.cloudburstmc.netty.channel.raknet.packet.RakDatagramPacket;

import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.CompositeByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

public class RakDatagramCodec extends MessageToMessageCodec<ByteBuf, RakDatagramPacket> {
    public static final String NAME = "rak-datagram-codec";

    private static final InternalLogger log = InternalLoggerFactory.getInstance(RakDatagramCodec.class);

    public RakDatagramCodec() {
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, RakDatagramPacket packet, List<Object> out) throws Exception {
        ByteBuf header = ctx.alloc().ioBuffer(4);
        header.writeByte(packet.getFlags());
        header.writeMediumLE(packet.getSequenceIndex());

        // Use a composite buffer so we don't have to do any memory copying.
        CompositeByteBuf buf = ctx.alloc().compositeBuffer((packet.getPackets().size() * 2) + 1);
        buf.addComponent(true, header);

        for (EncapsulatedPacket encapsulated : packet.getPackets()) {
            encapsulated.encode(buf);
        }
        out.add(buf);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf buffer, List<Object> list) throws Exception {
        byte potentialFlags = buffer.getByte(buffer.readerIndex());
        if ((potentialFlags & FLAG_VALID) == 0) {
            // Not a RakNet datagram
            list.add(buffer.retain());
            return;
        }

        if ((potentialFlags & FLAG_ACK) != 0 || (potentialFlags & FLAG_NACK) != 0) {
            // Do not handle Acknowledge packets here
            list.add(buffer.retain());
            return;
        }

        RakDatagramPacket packet = RakDatagramPacket.newInstance();
        try {
            packet.setFlags(buffer.readByte());
            packet.setSequenceIndex(buffer.readUnsignedMediumLE());
            while (buffer.isReadable()) {
                EncapsulatedPacket encapsulated = EncapsulatedPacket.newInstance();
                try {
                    encapsulated.decode(buffer);
                    packet.getPackets().add(encapsulated.retain());
                } catch (Throwable t) {
                    log.error("Error decoding encapsulated packet", t); // TODO: this is just temporary for debugging
                    throw t;
                } finally {
                    encapsulated.release();
                }
            }
            list.add(packet.retain());
        } finally {
            packet.release();
        }
    }
}
