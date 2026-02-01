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

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

import io.netty.channel.Channel;
import io.netty.channel.ChannelException;
import io.netty.channel.ChannelFactory;
import io.netty.channel.ReflectiveChannelFactory;
import io.netty.channel.socket.DatagramChannel;
import io.netty.util.internal.StringUtil;

public class RakChannelFactory<T extends Channel> implements ChannelFactory<T> {

    private final Class<T> channelClass;
    private final Function<DatagramChannel, T> constructor;
    private final ChannelFactory<? extends DatagramChannel> datagramChannelFactory;
    private final Consumer<DatagramChannel> parentConsumer;

    private RakChannelFactory(Class<T> channelClass, Function<DatagramChannel, T> constructor, ChannelFactory<? extends DatagramChannel> datagramChannelFactory,
                              Consumer<DatagramChannel> parentConsumer) {
        Objects.requireNonNull(channelClass, "channelClass");
        Objects.requireNonNull(datagramChannelFactory, "datagramChannelFactory");
        Objects.requireNonNull(constructor, "constructor");
        this.channelClass = channelClass;
        this.constructor = constructor;

        this.datagramChannelFactory = datagramChannelFactory;
        this.parentConsumer = parentConsumer;
    }

    public static RakChannelFactory<RakServerChannel> server(Class<? extends DatagramChannel> clazz) {
        return server(clazz, null);
    }

    public static RakChannelFactory<RakServerChannel> server(Class<? extends DatagramChannel> clazz, Consumer<DatagramChannel> parentConsumer) {
        return server(clazz, parentConsumer, null);
    }

    public static RakChannelFactory<RakServerChannel> server(Class<? extends DatagramChannel> clazz, Consumer<DatagramChannel> parentConsumer, Consumer<RakChannel> childConsumer) {
        return server(new ReflectiveChannelFactory<>(clazz), parentConsumer, childConsumer);
    }

    public static RakChannelFactory<RakServerChannel> server(ChannelFactory<? extends DatagramChannel> channelFactory) {
        return server(channelFactory, null);
    }

    public static RakChannelFactory<RakServerChannel> server(ChannelFactory<? extends DatagramChannel> channelFactory, Consumer<DatagramChannel> parentConsumer) {
        return server(channelFactory, parentConsumer, null);
    }

    public static RakChannelFactory<RakServerChannel> server(ChannelFactory<? extends DatagramChannel> channelFactory, Consumer<DatagramChannel> parentConsumer, Consumer<RakChannel> childConsumer) {
        return new RakChannelFactory<>(RakServerChannel.class, ch -> new RakServerChannel(ch, childConsumer), channelFactory, parentConsumer);
    }

    public static RakChannelFactory<RakClientChannel> client(Class<? extends DatagramChannel> clazz) {
        return client(clazz, null);
    }

    public static RakChannelFactory<RakClientChannel> client(Class<? extends DatagramChannel> channelFactory, Consumer<DatagramChannel> parentConsumer) {
        return client(new ReflectiveChannelFactory<>(channelFactory), parentConsumer);
    }

    public static RakChannelFactory<RakClientChannel> client(ChannelFactory<? extends DatagramChannel> channelFactory) {
        return client(channelFactory, null);
    }

    public static RakChannelFactory<RakClientChannel> client(ChannelFactory<? extends DatagramChannel> channelFactory, Consumer<DatagramChannel> parentConsumer) {
        return new RakChannelFactory<>(RakClientChannel.class, RakClientChannel::new, channelFactory, parentConsumer);
    }

    @Override
    public T newChannel() {
        try {
            DatagramChannel channel = datagramChannelFactory.newChannel();
            if (this.parentConsumer != null) {
                this.parentConsumer.accept(channel);
            }
            return constructor.apply(channel);
        } catch (Throwable t) {
            throw new ChannelException("Unable to create Channel from class " + this.channelClass, t);
        }
    }

    @Override
    public String toString() {
        return StringUtil.simpleClassName(RakChannelFactory.class) +
                '(' + StringUtil.simpleClassName(this.channelClass) + ".class)";
    }
}