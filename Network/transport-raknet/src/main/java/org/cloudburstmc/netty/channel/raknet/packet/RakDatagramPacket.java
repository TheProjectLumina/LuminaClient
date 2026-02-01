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

import static org.cloudburstmc.netty.channel.raknet.RakConstants.FLAG_CONTINUOUS_SEND;
import static org.cloudburstmc.netty.channel.raknet.RakConstants.FLAG_NEEDS_B_AND_AS;
import static org.cloudburstmc.netty.channel.raknet.RakConstants.FLAG_VALID;
import static org.cloudburstmc.netty.channel.raknet.RakConstants.RAKNET_DATAGRAM_HEADER_SIZE;

import java.util.ArrayList;
import java.util.List;

import io.netty.util.AbstractReferenceCounted;
import io.netty.util.internal.ObjectPool;

public class RakDatagramPacket extends AbstractReferenceCounted {

    private static final ObjectPool<RakDatagramPacket> RECYCLER = ObjectPool.newPool(RakDatagramPacket::new);

    private final ObjectPool.Handle<RakDatagramPacket> handle;
    private final List<EncapsulatedPacket> packets = new ArrayList<>();
    private byte flags = FLAG_VALID | FLAG_NEEDS_B_AND_AS;
    private long sendTime;
    private long nextSend;
    private int sequenceIndex = -1;

    public static RakDatagramPacket newInstance() {
        return RECYCLER.get();
    }

    private RakDatagramPacket(ObjectPool.Handle<RakDatagramPacket> handle) {
        this.handle = handle;
    }

    @Override
    public RakDatagramPacket retain() {
        super.retain();
        return this;
    }

    @Override
    public RakDatagramPacket retain(int increment) {
        super.retain(increment);
        return this;
    }

    @Override
    public RakDatagramPacket touch(Object hint) {
        for (EncapsulatedPacket packet : this.packets) {
            packet.touch(hint);
        }
        return this;
    }

    public boolean tryAddPacket(EncapsulatedPacket packet, int mtu) {
        if (this.getSize() + packet.getSize() > mtu - RAKNET_DATAGRAM_HEADER_SIZE) {
            return false;
        }

        this.packets.add(packet);
        if (packet.isSplit()) {
            flags |= FLAG_CONTINUOUS_SEND;
        }
        return true;
    }

    @Override
    public boolean release() {
        return super.release();
    }

    @Override
    protected void deallocate() {
        for (EncapsulatedPacket packet : this.packets) {
            packet.release();
        }
        this.packets.clear();
        this.flags = FLAG_VALID | FLAG_NEEDS_B_AND_AS;
        this.sendTime = 0;
        this.nextSend = 0;
        this.sequenceIndex = -1;
        setRefCnt(1);
        this.handle.recycle(this);
    }

    public int getSize() {
        int size = RAKNET_DATAGRAM_HEADER_SIZE;
        for (EncapsulatedPacket packet : this.packets) {
            size += packet.getSize();
        }
        return size;
    }

    public List<EncapsulatedPacket> getPackets() {
        return this.packets;
    }

    public byte getFlags() {
        return this.flags;
    }

    public void setFlags(byte flags) {
        this.flags = flags;
    }

    public long getSendTime() {
        return sendTime;
    }

    public void setSendTime(long sendTime) {
        this.sendTime = sendTime;
    }

    public long getNextSend() {
        return this.nextSend;
    }

    public void setNextSend(long nextSend) {
        this.nextSend = nextSend;
    }

    public int getSequenceIndex() {
        return this.sequenceIndex;
    }

    public void setSequenceIndex(int sequenceIndex) {
        this.sequenceIndex = sequenceIndex;
    }

    @Override
    public String toString() {
        return "RakDatagramPacket{" +
                "handle=" + handle +
                ", packets=" + packets +
                ", flags=" + flags +
                ", sendTime=" + sendTime +
                ", nextSend=" + nextSend +
                ", sequenceIndex=" + sequenceIndex +
                '}';
    }
}