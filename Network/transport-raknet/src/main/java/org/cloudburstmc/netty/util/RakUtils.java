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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Queue;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.DefaultChannelPipeline;
import io.netty.channel.socket.DatagramPacket;

public class RakUtils {

    private static final Constructor<DefaultChannelPipeline> DEFAULT_CHANNEL_PIPELINE_CONSTRUCTOR;
    private static final Method PIPELINE_DESTROY_METHOD;

    static {
        try {
            Constructor<DefaultChannelPipeline> constructor = DefaultChannelPipeline.class.getDeclaredConstructor(Channel.class);
            constructor.setAccessible(true);
            DEFAULT_CHANNEL_PIPELINE_CONSTRUCTOR = constructor;
        } catch (NoSuchMethodException e) {
            throw new AssertionError("Unable to find DefaultChannelPipeline(Channel) constructor", e);
        }

        try {
            Method method = DefaultChannelPipeline.class.getDeclaredMethod("destroy");
            method.setAccessible(true);
            PIPELINE_DESTROY_METHOD = method;
        } catch (NoSuchMethodException e) {
            throw new AssertionError("Unable to find DefaultChannelPipeline.destroy() method", e);
        }
    }

    public static DefaultChannelPipeline newChannelPipeline(Channel channel) {
        try {
            return DEFAULT_CHANNEL_PIPELINE_CONSTRUCTOR.newInstance(channel);
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new IllegalStateException("Unable to instantiate DefaultChannelPipeline", e);
        }
    }

    public static void destroyChannelPipeline(ChannelPipeline pipeline) {
        try {
            PIPELINE_DESTROY_METHOD.invoke(pipeline);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException("Unable to destroy DefaultChannelPipeline", e);
        }
    }

    private static final int AF_INET6 = 23;

    public static InetSocketAddress readAddress(ByteBuf buffer) {
        short type = buffer.readByte();
        InetAddress address;
        int port;
        try {
            if (type == 4) {
                byte[] addressBytes = new byte[4];
                buffer.readBytes(addressBytes);
                flip(addressBytes);
                address = Inet4Address.getByAddress(addressBytes);
                port = buffer.readUnsignedShort();
            } else if (type == 6) {
                buffer.readShortLE(); // Family, AF_INET6
                port = buffer.readUnsignedShort();
                buffer.readInt(); // Flow information
                byte[] addressBytes = new byte[16];
                buffer.readBytes(addressBytes);
                int scopeId = buffer.readInt();
                address = Inet6Address.getByAddress(null, addressBytes, scopeId);
            } else {
                throw new UnsupportedOperationException("Unknown Internet Protocol version. Expected 4 or 6, got " + type);
            }
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException(e);
        }
        return new InetSocketAddress(address, port);
    }

    public static boolean skipAddress(ByteBuf buffer) {
        short type = buffer.readByte();
        try {
            if (type == 4) {
                // Skip 4 + 2 bytes
                buffer.skipBytes(6);
            } else if (type == 6) {
                // Skip 2 + 2 + 4 + 16 + 4 bytes
                buffer.skipBytes(28);
            } else {
                // Vanilla client skips over if the type is not 4 or 6
                return false;
            }
        } catch (IndexOutOfBoundsException e) {
            return false;
        }
        return true;
    }

    public static void writeAddress(ByteBuf buffer, InetSocketAddress address) {
        byte[] addressBytes = address.getAddress().getAddress();
        if (address.getAddress() instanceof Inet4Address) {
            buffer.writeByte(4);
            flip(addressBytes);
            buffer.writeBytes(addressBytes);
            buffer.writeShort(address.getPort());
        } else if (address.getAddress() instanceof Inet6Address) {
            buffer.writeByte(6);
            buffer.writeShortLE(AF_INET6);
            buffer.writeShort(address.getPort());
            buffer.writeInt(0);
            buffer.writeBytes(addressBytes);
            buffer.writeInt(((Inet6Address) address.getAddress()).getScopeId());
        } else {
            throw new UnsupportedOperationException("Unknown InetAddress instance");
        }
    }

    private static void flip(byte[] bytes) {
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) (~bytes[i] & 0xFF);
        }
    }

    public static int writeAckEntries(ByteBuf buffer, Queue<IntRange> ackQueue, int mtu) {
        int startIndex = buffer.writerIndex();
        buffer.writeZero(2);
        mtu -= 2; // Skip entries size (short)

        int count = 0;
        IntRange ackRange;
        while ((ackRange = ackQueue.peek()) != null) {
            boolean singleton = ackRange.start == ackRange.end;
            int size = singleton ? 4 : 7;
            if (mtu < size) {
                break;
            }

            count++;
            mtu -= size;

            buffer.writeBoolean(singleton);
            buffer.writeMediumLE(ackRange.start);
            if (!singleton) {
                buffer.writeMediumLE(ackRange.end);
            }
            ackQueue.remove();
        }

        int finalIndex = buffer.writerIndex();
        buffer.writerIndex(startIndex);
        buffer.writeShort(count);
        buffer.writerIndex(finalIndex);
        return count;
    }

    public static int clamp(int value, int low, int high) {
        return value < low ? low : value > high ? high : value;
    }

    public static int powerOfTwoCeiling(int value) {
        value--;
        value |= value >> 1;
        value |= value >> 2;
        value |= value >> 4;
        value |= value >> 8;
        value |= value >> 16;
        value++;
        return value;
    }

    public static DatagramPacket datagramReply(ByteBuf buf, DatagramPacket request) {
        return new DatagramPacket(buf, request.sender(), request.recipient());
    }
}
