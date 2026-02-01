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

package org.cloudburstmc.netty;

import org.cloudburstmc.netty.util.BitQueue;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.ThreadLocalRandom;

public class BitQueueTests {

    @Test
    public void testQueue() {
        Queue<Boolean> bits = new ArrayDeque<>();
        BitQueue queue = new BitQueue();

        for (int i = 0; i < 256; i++) {
            boolean value = ThreadLocalRandom.current().nextBoolean();
            queue.add(value);
            bits.add(value);
        }

        while (!queue.isEmpty() && !bits.isEmpty()) {
            boolean expected = bits.poll();
            boolean actual = queue.poll();
            Assertions.assertEquals(expected, actual, "Expected %s but got %s");
        }
        Assertions.assertTrue(queue.isEmpty() && bits.isEmpty(), "Queue is not empty");
    }
}
