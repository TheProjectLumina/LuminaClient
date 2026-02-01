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

public enum RakDisconnectReason {
    CLOSED_BY_REMOTE_PEER,
    SHUTTING_DOWN,
    DISCONNECTED,
    TIMED_OUT,
    CONNECTION_REQUEST_FAILED,
    ALREADY_CONNECTED,
    NO_FREE_INCOMING_CONNECTIONS,
    INCOMPATIBLE_PROTOCOL_VERSION,
    IP_RECENTLY_CONNECTED,
    BAD_PACKET,
    QUEUE_TOO_LONG
}
