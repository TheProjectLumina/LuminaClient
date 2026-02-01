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

import java.net.InetSocketAddress;

import io.netty.buffer.ByteBuf;

public class RakPing {

    private final long pingTime;
    private final InetSocketAddress sender;

    public RakPing(long pingTime, InetSocketAddress sender) {
        this.pingTime = pingTime;
        this.sender = sender;
    }

    public long getPingTime() {
        return this.pingTime;
    }


    public InetSocketAddress getSender() {
        return this.sender;
    }

    public RakPong reply(long guid, ByteBuf pongData) {
        return new RakPong(this.pingTime, guid, pongData, this.sender);
    }
}
