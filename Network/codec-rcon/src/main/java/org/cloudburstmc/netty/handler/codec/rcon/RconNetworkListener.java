package org.cloudburstmc.netty.handler.codec.rcon;

import com.nukkitx.network.NetworkListener;
import com.nukkitx.network.handler.ExceptionHandler;
import com.nukkitx.network.util.Bootstraps;
import com.nukkitx.network.util.EventLoops;
import com.nukkitx.network.util.NetworkThreadFactory;

import org.cloudburstmc.netty.handler.codec.rcon.handler.RconHandler;

import java.net.InetSocketAddress;
import java.nio.ByteOrder;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import lombok.Getter;

public class RconNetworkListener extends ChannelInitializer<SocketChannel> implements NetworkListener {
    private final RconEventListener eventListener;
    private final InetSocketAddress address;
    private final ServerBootstrap bootstrap;
    @Getter
    private final ExecutorService commandExecutionService = Executors.newSingleThreadExecutor(
            NetworkThreadFactory.builder().daemon(true).format("RCON Command Executor").build());
    private final byte[] password;
    private SocketChannel channel;

    public RconNetworkListener(RconEventListener eventListener, byte[] password, String address, int port) {
        this.eventListener = eventListener;
        this.password = password;
        this.address = new InetSocketAddress(address, port);

        bootstrap = new ServerBootstrap().option(ChannelOption.ALLOCATOR, ByteBufAllocator.DEFAULT).handler(this);

        Bootstraps.setupServerBootstrap(bootstrap);
        this.bootstrap.group(EventLoops.commonGroup());
    }

    @Override
    public boolean bind() {
        return bootstrap.bind(address).awaitUninterruptibly().isSuccess();
    }

    @Override
    public void close() {
        commandExecutionService.shutdown();
        try {
            commandExecutionService.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            // Ignore
        }
        bootstrap.config().group().shutdownGracefully();
        if (channel != null) {
            channel.close().syncUninterruptibly();
        }
    }

    @Override
    public InetSocketAddress getAddress() {
        return null;
    }

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        this.channel = socketChannel;

        channel.pipeline().addLast("lengthDecoder", new LengthFieldBasedFrameDecoder(ByteOrder.LITTLE_ENDIAN, 4096, 0, 4, 0, 4, true));
        channel.pipeline().addLast("rconDecoder", new RconCodec());
        channel.pipeline().addLast("rconHandler", new RconHandler(eventListener, password));
        channel.pipeline().addLast("lengthPrepender", new LengthFieldPrepender(ByteOrder.LITTLE_ENDIAN, 4, 0, false));
        channel.pipeline().addLast("exceptionHandler", new ExceptionHandler());
    }
}
