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

package org.cloudburstmc.netty.util;

import org.cloudburstmc.netty.channel.raknet.packet.EncapsulatedPacket;

import java.util.Objects;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.util.AbstractReferenceCounted;
import io.netty.util.IllegalReferenceCountException;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.ReferenceCounted;

public class SplitPacketHelper extends AbstractReferenceCounted {
    private final EncapsulatedPacket[] packets;
    private final long created = System.currentTimeMillis();

    public SplitPacketHelper(long expectedLength) {
        if (expectedLength < 2) {
            throw new IllegalArgumentException("expectedLength must be greater than 1");
        }
        this.packets = new EncapsulatedPacket[(int) expectedLength];
    }

    public EncapsulatedPacket add(EncapsulatedPacket packet, ByteBufAllocator alloc) {
        Objects.requireNonNull(packet, "packet cannot be null");
        if (!packet.isSplit()) throw new IllegalArgumentException("Packet is not split");
        if (this.refCnt() <= 0) throw new IllegalReferenceCountException(this.refCnt());
        if (packet.getPartIndex() < 0 || packet.getPartIndex() >= this.packets.length) {
            throw new IllegalArgumentException(String.format("Split packet part index out of range. Got %s, expected 0-%s",
                    packet.getPartIndex(), this.packets.length - 1));
        }

        int partIndex = packet.getPartIndex();
        if (this.packets[partIndex] != null) {
            // Duplicate
            return null;
        }
        // Retain the packet so it can be reassembled later.
        this.packets[partIndex] = packet.retain();

        int sz = 0;
        for (EncapsulatedPacket netPacket : this.packets) {
            if (netPacket == null) {
                return null;
            }
            sz += netPacket.getBuffer().readableBytes();
        }

        // We can't use a composite buffer as the native code will choke on it
        ByteBuf reassembled = alloc.ioBuffer(sz);
        for (EncapsulatedPacket netPacket : this.packets) {
            ByteBuf buf = netPacket.getBuffer();
            reassembled.writeBytes(buf, buf.readerIndex(), buf.readableBytes());
        }

        return packet.fromSplit(reassembled);
    }

    public boolean expired() {
        // If we're waiting on a split packet for more than 30 seconds, the client on the other end is either severely
        // lagging, or has died.
        if (this.refCnt() <= 0) throw new IllegalReferenceCountException(this.refCnt());
        return System.currentTimeMillis() - created >= 30000;
    }

    @Override
    protected void deallocate() {
        for (EncapsulatedPacket packet : this.packets) {
            ReferenceCountUtil.release(packet);
        }
    }

    @Override
    public ReferenceCounted touch(Object hint) {
        throw new UnsupportedOperationException();
    }
}
