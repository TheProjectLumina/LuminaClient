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

import org.cloudburstmc.netty.channel.raknet.RakChannelFactory;
import org.cloudburstmc.netty.channel.raknet.RakChildChannel;
import org.cloudburstmc.netty.channel.raknet.RakClientChannel;
import org.cloudburstmc.netty.channel.raknet.RakConstants;
import org.cloudburstmc.netty.channel.raknet.RakServerChannel;
import org.cloudburstmc.netty.channel.raknet.config.RakChannelOption;
import org.cloudburstmc.netty.channel.raknet.packet.RakMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.StringJoiner;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;

public class RakTests {

    private static final byte[] ADVERTISEMENT = new StringJoiner(";", "", ";")
            .add("MCPE")
            .add("RakNet unit test")
            .add(Integer.toString(542))
            .add("1.19.0")
            .add(Integer.toString(0))
            .add(Integer.toString(4))
            .add(Long.toUnsignedString(ThreadLocalRandom.current().nextLong()))
            .add("C")
            .add("Survival")
            .add("1")
            .add("19132")
            .add("19132")
            .toString().getBytes(StandardCharsets.UTF_8);

    private static final int RESEND_PACKET_ID = 0xFF;

    private static SimpleChannelInboundHandler<RakMessage> RESEND_HANDLER() {
        return new SimpleChannelInboundHandler<RakMessage>() {
            @Override
            protected void channelRead0(ChannelHandlerContext ctx, RakMessage message) throws Exception {
                int packetId = message.content().getUnsignedByte(message.content().readerIndex());
                if (packetId == RESEND_PACKET_ID) {
                    ctx.writeAndFlush(new RakMessage(message.content().retain()));
                } else {
                    ctx.fireChannelRead(message.retain());
                }
            }
        };
    };

    private static SimpleChannelInboundHandler<RakMessage> RESEND_RECEIVER(ByteBuf expectedMessage) {
        return new SimpleChannelInboundHandler<RakMessage>() {
            @Override
            protected void channelRead0(ChannelHandlerContext ctx, RakMessage message) throws Exception {
                int packetId = message.content().getUnsignedByte(message.content().readerIndex());
                if (packetId != RESEND_PACKET_ID) {
                    ctx.fireChannelRead(message.retain());
                    return;
                }

                ByteBuf buffer = message.content().skipBytes(1);
                if (ByteBufUtil.equals(buffer, expectedMessage)) {
                    System.out.println("Received message is valid");
                } else {
                    throw new IllegalStateException("Malformed message received\nExpected: " + ByteBufUtil.hexDump(expectedMessage) + "\nReceived: " + ByteBufUtil.hexDump(buffer));
                }
            }
        };
    };

    private static ServerBootstrap serverBootstrap() {
        return new ServerBootstrap()
                .channelFactory(RakChannelFactory.server(NioDatagramChannel.class))
                .group(new NioEventLoopGroup())
                .option(RakChannelOption.RAK_SUPPORTED_PROTOCOLS, new int[]{11})
                .option(RakChannelOption.RAK_MAX_CONNECTIONS, 1)
                .childOption(RakChannelOption.RAK_ORDERING_CHANNELS, 1)
                .option(RakChannelOption.RAK_GUID, ThreadLocalRandom.current().nextLong())
                .option(RakChannelOption.RAK_ADVERTISEMENT, Unpooled.wrappedBuffer(ADVERTISEMENT))
                .handler(new ChannelInitializer<RakServerChannel>() {
                    @Override
                    protected void initChannel(RakServerChannel ch) throws Exception {
                        System.out.println("Initialised server channel");
                    }
                })
                .childHandler(new ChannelInitializer<RakChildChannel>() {
                    @Override
                    protected void initChannel(RakChildChannel ch) throws Exception {
                        System.out.println("Server child channel initialized " + ch.remoteAddress());
                        ch.pipeline().addLast(RESEND_HANDLER());
                    }
                });
    }

    private static Bootstrap clientBootstrap(int mtu) {
        return new Bootstrap()
                .channelFactory(RakChannelFactory.client(NioDatagramChannel.class))
                .group(new NioEventLoopGroup())
                .option(RakChannelOption.RAK_PROTOCOL_VERSION, 11)
                .option(RakChannelOption.RAK_MTU, mtu)
                .option(RakChannelOption.RAK_ORDERING_CHANNELS, 1);
    }

    private static IntStream validMtu() {
        return IntStream.range(RakConstants.MINIMUM_MTU_SIZE, RakConstants.MAXIMUM_MTU_SIZE)
                .filter(i -> i % 12 == 0);
    }

    public void setupServer() {
        serverBootstrap()
                .bind(new InetSocketAddress("127.0.0.1", 19132))
                .awaitUninterruptibly();
    }

    public void setupCookieServer() {
        serverBootstrap()
                .option(RakChannelOption.RAK_SEND_COOKIE, true)
                .bind(new InetSocketAddress("127.0.0.1", 19132))
                .awaitUninterruptibly();
    }

    @Test
    public void testClientConnect() {
        setupServer();
        int mtu = RakConstants.MAXIMUM_MTU_SIZE;
        System.out.println("Testing client with MTU " + mtu);

        clientBootstrap(mtu)
                .handler(new ChannelInitializer<RakClientChannel>() {
                    @Override
                    protected void initChannel(RakClientChannel ch) throws Exception {
                        System.out.println("Client channel initialized");
                    }
                })
                .connect(new InetSocketAddress("127.0.0.1", 19132))
                .awaitUninterruptibly()
                .channel();
    }

    @Test
    public void testClientConnectWithCookie() {
        setupCookieServer();
        int mtu = RakConstants.MAXIMUM_MTU_SIZE;
        System.out.println("Testing client with MTU " + mtu + " and cookie enabled");

        clientBootstrap(mtu)
                .handler(new ChannelInitializer<RakClientChannel>() {
                    @Override
                    protected void initChannel(RakClientChannel ch) throws Exception {
                        System.out.println("Client channel initialized");
                    }
                })
                .connect(new InetSocketAddress("127.0.0.1", 19132))
                .awaitUninterruptibly()
                .channel();
    }

    @Test
    public void testCompatibleClientConnect() {
        int mtu = RakConstants.MAXIMUM_MTU_SIZE;
        System.out.println("Testing client with MTU " + mtu);

        Channel channel = clientBootstrap(mtu)
                .option(RakChannelOption.RAK_COMPATIBILITY_MODE, true)
                .option(RakChannelOption.RAK_GUID, ThreadLocalRandom.current().nextLong())
                .handler(new ChannelInitializer<RakClientChannel>() {
                    @Override
                    protected void initChannel(RakClientChannel ch) throws Exception {
                        System.out.println("Client channel initialized");
                    }
                })
                .connect(new InetSocketAddress("127.0.0.1", 19132))
                .awaitUninterruptibly()
                .channel();
    }


    @ParameterizedTest
    @MethodSource("validMtu")
    public void testClientResend(int mtu) {
        setupServer();
        System.out.println("Testing client with MTU " + mtu);

        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[mtu * 16];
        random.nextBytes(bytes);
        ByteBuf buffer = Unpooled.wrappedBuffer(bytes);

        ChannelHandler sender = new ChannelInboundHandlerAdapter() {
            @Override
            public void channelActive(ChannelHandlerContext ctx) throws Exception {
                ByteBuf buf = buffer.alloc().buffer(buffer.readableBytes() + 1);
                buf.writeByte(RESEND_PACKET_ID);
                buf.writeBytes(buffer.slice());

                ctx.channel().writeAndFlush(new RakMessage(buf));
            }
        };

        ChannelInitializer<RakClientChannel> initializer = new ChannelInitializer<RakClientChannel>() {
            @Override
            protected void initChannel(RakClientChannel ch) throws Exception {
                ch.pipeline().addLast(RESEND_RECEIVER(buffer));
                ch.pipeline().addLast(sender);
            }
        };

        clientBootstrap(mtu)
                .handler(initializer)
                .connect(new InetSocketAddress("127.0.0.1", 19132))
                .awaitUninterruptibly()
                .channel();

        Object o = new Object();
        synchronized (o) {
            try {
                o.wait(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
