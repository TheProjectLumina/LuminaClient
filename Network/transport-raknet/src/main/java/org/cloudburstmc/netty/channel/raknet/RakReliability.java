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

package org.cloudburstmc.netty.channel.raknet;

public enum RakReliability {
    UNRELIABLE(false, false, false, false),
    UNRELIABLE_SEQUENCED(false, false, true, false),
    RELIABLE(true, false, false, false),
    RELIABLE_ORDERED(true, true, false, false),
    RELIABLE_SEQUENCED(true, false, true, false),
    UNRELIABLE_WITH_ACK_RECEIPT(false, false, false, true),
    UNRELIABLE_SEQUENCED_WITH_ACK_RECEIPT(false, false, true, true),
    RELIABLE_WITH_ACK_RECEIPT(true, false, false, true),
    RELIABLE_ORDERED_WITH_ACK_RECEIPT(true, true, false, true),
    RELIABLE_SEQUENCED_WITH_ACK_RECEIPT(true, false, true, true);

    private static final RakReliability[] VALUES = values();

    final boolean reliable;
    final boolean ordered;
    final boolean sequenced;
    final boolean withAckReceipt;
    final int size;

    RakReliability(boolean reliable, boolean ordered, boolean sequenced, boolean withAckReceipt) {
        this.reliable = reliable;
        this.ordered = ordered;
        this.sequenced = sequenced;
        this.withAckReceipt = withAckReceipt;

        int size = 0;
        if (this.reliable) {
            size += 3;
        }

        if (this.sequenced) {
            size += 3;
        }

        if (this.ordered) {
            size += 4;
        }
        this.size = size;
    }

    public static RakReliability fromId(int id) {
        if (id < 0 || id > 7) {
            return null;
        }
        return VALUES[id];
    }

    public int getSize() {
        return size;
    }

    public boolean isOrdered() {
        return ordered;
    }

    public boolean isReliable() {
        return reliable;
    }

    public boolean isSequenced() {
        return sequenced;
    }

    public boolean isWithAckReceipt() {
        return withAckReceipt;
    }
}