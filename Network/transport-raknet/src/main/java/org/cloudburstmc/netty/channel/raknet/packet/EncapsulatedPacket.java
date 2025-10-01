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

package org.cloudburstmc.netty.channel.raknet.packet;

import org.cloudburstmc.netty.channel.raknet.RakConstants;
import org.cloudburstmc.netty.channel.raknet.RakReliability;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.CompositeByteBuf;
import io.netty.util.AbstractReferenceCounted;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.internal.ObjectPool;

public class EncapsulatedPacket extends AbstractReferenceCounted {

    private static final ObjectPool<EncapsulatedPacket> RECYCLER = ObjectPool.newPool(EncapsulatedPacket::new);

    private final ObjectPool.Handle<EncapsulatedPacket> handle;
    private RakReliability reliability;
    private int reliabilityIndex;
    private int sequenceIndex;
    private int orderingIndex;
    private short orderingChannel;
    private boolean split;
    private int partCount;
    private int partId;
    private int partIndex;
    private ByteBuf buffer;
    private boolean needsBAS;

    public static EncapsulatedPacket newInstance() {
        return RECYCLER.get();
    }

    private EncapsulatedPacket(ObjectPool.Handle<EncapsulatedPacket> handle) {
        this.handle = handle;
    }

    public void encode(CompositeByteBuf buffer) {
        RakReliability reliability = this.reliability;
        ByteBuf header = buffer.alloc().ioBuffer(3 + reliability.getSize() + (this.split ? 10 : 0));

        int flags = this.reliability.ordinal() << 5;
        if (this.split) {
            flags |= RakConstants.FLAG_PACKET_PAIR;
        }
        if (needsBAS){
            flags |= RakConstants.FLAG_NEEDS_B_AND_AS;
        }
        header.writeByte(flags);
        header.writeShort(this.buffer.readableBytes() << 3); // size

        if (reliability.isReliable()) {
            header.writeMediumLE(this.reliabilityIndex);
        }

        if (reliability.isSequenced()) {
            header.writeMediumLE(this.sequenceIndex);
        }

        if (reliability.isOrdered() || reliability.isSequenced()) {
            header.writeMediumLE(this.orderingIndex);
            header.writeByte(this.orderingChannel);
        }

        if (this.split) {
            header.writeInt(this.partCount);
            header.writeShort(this.partId);
            header.writeInt(this.partIndex);
        }

        buffer.addComponent(true, header);
        buffer.addComponent(true, this.buffer.retain());
    }

    public void decode(ByteBuf buf) {
        byte flags = buf.readByte();
        this.reliability = RakReliability.fromId(flags >>> 5);
        this.split = (flags & RakConstants.FLAG_PACKET_PAIR) != 0;
        this.needsBAS = (flags & RakConstants.FLAG_NEEDS_B_AND_AS) != 0;
        int size = (buf.readUnsignedShort() + 7) >> 3;

        if (this.reliability.isReliable()) {
            this.reliabilityIndex = buf.readUnsignedMediumLE();
        }

        if (this.reliability.isSequenced()) {
            this.sequenceIndex = buf.readUnsignedMediumLE();
        }

        if (this.reliability.isOrdered() || this.reliability.isSequenced()) {
            this.orderingIndex = buf.readUnsignedMediumLE();
            this.orderingChannel = buf.readUnsignedByte();
        }

        if (this.split) {
            this.partCount = buf.readInt();
            this.partId = buf.readUnsignedShort();
            this.partIndex = buf.readInt();
        }

        // Slice the buffer to use less memory
        this.buffer = buf.readRetainedSlice(size);
    }

    public int getSize() {
        // Include back of the envelope calculation
        return 3 + this.reliability.getSize() + (this.split ? 10 : 0) + this.buffer.readableBytes();
    }

    public EncapsulatedPacket fromSplit(ByteBuf reassembled) {
        EncapsulatedPacket packet = newInstance();
        packet.reliability = this.reliability;
        packet.reliabilityIndex = this.reliabilityIndex;
        packet.sequenceIndex = this.sequenceIndex;
        packet.orderingIndex = this.orderingIndex;
        packet.orderingChannel = this.orderingChannel;
        packet.buffer = reassembled;
        return packet;
    }


    @Override
    protected void deallocate() {
        ReferenceCountUtil.release(this.buffer);
        this.reliability = null;
        this.reliabilityIndex = 0;
        this.sequenceIndex = 0;
        this.orderingIndex = 0;
        this.orderingChannel = 0;
        this.split = false;
        this.partCount = 0;
        this.partId = 0;
        this.partIndex = 0;
        this.buffer = null;
        setRefCnt(1);
        this.handle.recycle(this);
    }

    @Override
    public EncapsulatedPacket touch(Object o) {
        this.buffer.touch();
        return this;
    }

    @Override
    public EncapsulatedPacket retain() {
        return (EncapsulatedPacket) super.retain();
    }

    public RakReliability getReliability() {
        return reliability;
    }

    public void setReliability(RakReliability reliability) {
        this.reliability = reliability;
    }

    public int getReliabilityIndex() {
        return this.reliabilityIndex;
    }

    public void setReliabilityIndex(int reliabilityIndex) {
        this.reliabilityIndex = reliabilityIndex;
    }

    public int getSequenceIndex() {
        return this.sequenceIndex;
    }

    public void setSequenceIndex(int sequenceIndex) {
        this.sequenceIndex = sequenceIndex;
    }

    public int getOrderingIndex() {
        return this.orderingIndex;
    }

    public void setOrderingIndex(int orderingIndex) {
        this.orderingIndex = orderingIndex;
    }

    public short getOrderingChannel() {
        return this.orderingChannel;
    }

    public void setOrderingChannel(short orderingChannel) {
        this.orderingChannel = orderingChannel;
    }

    public boolean isSplit() {
        return this.split;
    }

    public void setSplit(boolean split) {
        this.split = split;
    }

    public int getPartCount() {
        return this.partCount;
    }

    public void setPartCount(int partCount) {
        this.partCount = partCount;
    }

    public int getPartId() {
        return this.partId;
    }

    public void setPartId(int partId) {
        this.partId = partId;
    }

    public int getPartIndex() {
        return this.partIndex;
    }

    public void setPartIndex(int partIndex) {
        this.partIndex = partIndex;
    }

    public ByteBuf getBuffer() {
        return this.buffer;
    }

    public void setBuffer(ByteBuf buffer) {
        this.buffer = buffer;
    }

    public boolean isNeedsBAS() {
        return this.needsBAS;
    }

    public void setNeedsBAS(boolean needsBAS) {
        this.needsBAS = needsBAS;
    }

    public RakMessage toMessage() {
        return new RakMessage(buffer, reliability);
    }

    @Override
    public String toString() {
        return "EncapsulatedPacket{" +
                "reliability=" + reliability +
                ", reliabilityIndex=" + reliabilityIndex +
                ", sequenceIndex=" + sequenceIndex +
                ", orderingIndex=" + orderingIndex +
                ", orderingChannel=" + orderingChannel +
                ", split=" + split +
                ", partCount=" + partCount +
                ", partId=" + partId +
                ", partIndex=" + partIndex +
                ", buffer=" + ByteBufUtil.hexDump(buffer) +
                '}';
    }
}

