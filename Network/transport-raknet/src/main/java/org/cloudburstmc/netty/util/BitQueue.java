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

public class BitQueue {

    private long[] queue;
    private int head;
    private int tail;

    public BitQueue() {
        this(0);
    }

    public BitQueue(int capacity) {
        capacity = RakUtils.powerOfTwoCeiling(capacity);
        if (capacity <= 0) {
            capacity = 64;
        }

        this.queue = new long[((capacity + 63) >> 6)];
        this.head = 0;
        this.tail = 0;
    }

    public void add(boolean bit) {
        if (((this.head + 1) & ((this.queue.length << 6) - 1)) == this.tail) {
            this.resize(this.queue.length << 7);
        }

        int by = this.head >> 6;
        long bi = 1L << (this.head & 63);
        this.queue[by] ^= ((bit ? 0xFFFFFFFFFFFFFFFFL : 0) ^ this.queue[by]) & bi;
        this.head = (this.head + 1) & ((this.queue.length << 6) - 1);
    }

    private void resize(int capacity) {
        long[] newQueue = new long[(capacity + 63) >> 6];
        int size = this.size();

        if ((this.tail & 63) == 0) {
            if (this.head > this.tail) {
                int srcPos = this.tail >> 6;
                int length = (this.head - this.tail + 63) >> 6;
                System.arraycopy(this.queue, srcPos, newQueue, 0, length);
            } else if (this.head < this.tail) {
                int length = this.tail >> 6;
                int adjustedPos = ((this.queue.length << 6) - this.tail + 63) >> 6;
                System.arraycopy(this.queue, length, newQueue, 0, adjustedPos);
                length = (this.head + 63) >> 6;
                System.arraycopy(this.queue, 0, newQueue, adjustedPos, length);
            }

            this.tail = 0;
            this.head = size;
        } else {
            int tailBits = (this.tail & 63);
            int tailIdx = this.tail >> 6;
            int by2 = (tailIdx + 1) & (this.queue.length - 1);
            long mask;
            long bit1;
            long bit2;

            int cursor = 0;
            while (cursor < size) {
                mask = ((1L << tailBits) - 1);
                bit1 = ((this.queue[tailIdx] & ~mask) >>> tailBits);
                bit2 = (this.queue[by2] << (64 - tailBits));
                newQueue[cursor >> 6] = (bit1 | bit2);

                cursor += 64;
                tailIdx = (tailIdx + 1) & (this.queue.length - 1);
                by2 = (by2 + 1) & (this.queue.length - 1);
            }

            this.tail = 0;
            this.head = size;
        }

        this.queue = newQueue;
    }

    public int size() {
        if (this.head > this.tail) {
            return (this.head - this.tail);
        } else if (this.head < this.tail) {
            return ((this.queue.length << 6) - (this.tail - this.head));
        } else {
            return 0;
        }
    }

    public void set(int n, boolean bit) {
        if (n >= this.size() || n < 0) {
            return;
        }

        int idx = (this.tail + n) & ((this.queue.length << 6) - 1);
        int arrIdx = idx >> 6;
        long mask = 1L << (idx & 63);
        this.queue[arrIdx] ^= ((bit ? 0xFF : 0x00) ^ this.queue[arrIdx]) & mask;
    }

    public boolean get(int n) {
        if (n >= this.size() || n < 0) {
            return false;
        }

        int idx = (this.tail + n) & ((this.queue.length << 6) - 1);
        int arrIdx = idx >> 6;
        long mask = 1L << (idx & 63);
        return (this.queue[arrIdx] & mask) != 0;
    }

    public boolean isEmpty() {
        return (this.head == this.tail);
    }

    public boolean peek() {
        if (this.head == this.tail) {
            return false;
        }

        int arrIdx = this.tail >> 6;
        long mask = 1L << ((this.tail) & 63);
        return (this.queue[arrIdx] & mask) != 0;
    }

    public boolean poll() {
        if (this.head == this.tail) {
            return false;
        }

        int arrIdx = this.tail >> 6;
        long mask = 1L << ((this.tail) & 63);
        this.tail = (this.tail + 1) & ((this.queue.length << 6) - 1);
        return (this.queue[arrIdx] & mask) != 0;
    }
}
