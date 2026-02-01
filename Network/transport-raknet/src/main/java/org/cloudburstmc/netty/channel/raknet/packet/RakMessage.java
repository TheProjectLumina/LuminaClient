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

import org.cloudburstmc.netty.channel.raknet.RakPriority;
import org.cloudburstmc.netty.channel.raknet.RakReliability;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.DefaultByteBufHolder;

/**
 * Representation of a RakNet Packet
 */
public final class RakMessage extends DefaultByteBufHolder {
    private final RakReliability reliability;
    private final RakPriority priority;
    private final int channel;

    public RakMessage(ByteBuf payloadBuffer) {
        this(payloadBuffer, RakReliability.RELIABLE_ORDERED, RakPriority.NORMAL, 0);
    }

    public RakMessage(ByteBuf payloadBuffer, RakReliability reliability) {
        this(payloadBuffer, reliability, RakPriority.NORMAL, 0);
    }

    public RakMessage(ByteBuf payloadBuffer, RakReliability reliability, RakPriority priority) {
        this(payloadBuffer, reliability, priority, 0);
    }

    public RakMessage(ByteBuf payloadBuffer, RakReliability reliability, RakPriority priority, int channel) {
        super(payloadBuffer);
        this.reliability = reliability;
        this.priority = priority;
        this.channel = channel;
    }

    /**
     * Returns the reliability of the message
     *
     * @return reliability
     */
    public RakReliability reliability() {
        return reliability;
    }

    /**
     * Returns the priority of the message
     *
     * @return priority
     */
    public RakPriority priority() {
        return priority;
    }

    /**
     * Returns the channel of the message
     *
     * @return channel
     */
    public int channel() {
        return channel;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        RakMessage message = (RakMessage) o;

        if (reliability != message.reliability) {
            return false;
        }

        if (priority != message.priority) {
            return false;
        }

        if (channel != message.channel) {
            return false;
        }

        return content().equals(message.content());
    }

    @Override
    public int hashCode() {
        int result = reliability.hashCode();
        result = 31 * result + priority.hashCode();
        result = 31 * result + channel;
        result = 31 * result + content().hashCode();
        return result;
    }

    @Override
    public RakMessage copy() {
        return (RakMessage) super.copy();
    }

    @Override
    public RakMessage duplicate() {
        return (RakMessage) super.duplicate();
    }

    @Override
    public RakMessage retainedDuplicate() {
        return (RakMessage) super.retainedDuplicate();
    }

    @Override
    public RakMessage replace(ByteBuf content) {
        return new RakMessage(content, reliability, priority, channel);
    }

    @Override
    public RakMessage retain() {
        return (RakMessage) super.retain();
    }

    @Override
    public RakMessage touch() {
        return (RakMessage) super.touch();
    }

    @Override
    public String toString() {
        return "RakMessage{" +
                "reliability=" + reliability +
                ", priority=" + priority +
                ", channel=" + channel +
                "}";
    }
}
