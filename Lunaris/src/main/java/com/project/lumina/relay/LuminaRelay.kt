package com.project.lumina.relay

import com.project.lumina.relay.LuminaRelaySession.ClientSession
import com.project.lumina.relay.address.LuminaAddress
import com.project.lumina.relay.address.inetSocketAddress
import io.netty.bootstrap.Bootstrap
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.Channel
import io.netty.channel.ChannelFuture
import io.netty.channel.EventLoopGroup
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioDatagramChannel
import org.cloudburstmc.netty.channel.raknet.RakChannelFactory
import org.cloudburstmc.netty.channel.raknet.config.RakChannelOption
import org.cloudburstmc.netty.handler.codec.raknet.server.RakServerRateLimiter
import org.cloudburstmc.protocol.bedrock.BedrockPeer
import org.cloudburstmc.protocol.bedrock.BedrockPong
import org.cloudburstmc.protocol.bedrock.PacketDirection
import org.cloudburstmc.protocol.bedrock.codec.BedrockCodec
import org.cloudburstmc.protocol.bedrock.codec.v786.Bedrock_v786
import org.cloudburstmc.protocol.bedrock.codec.v818.Bedrock_v818
import org.cloudburstmc.protocol.bedrock.netty.initializer.BedrockChannelInitializer
import kotlin.random.Random

class LuminaRelay(
    private val localAddress: LuminaAddress = LuminaAddress(getNativeDefaultIp(), getNativeDefaultPort()),
    private val advertisement: BedrockPong = createNativeAdvertisement()
) {

    companion object {
        val DefaultCodec: BedrockCodec = Bedrock_v818.CODEC

        
        init {
            System.loadLibrary("lunaris")
        }

        
        @JvmStatic external fun getNativeDefaultIp(): String
        @JvmStatic external fun getNativeDefaultPort(): Int
        @JvmStatic external fun getNativeRemoteIp(): String
        @JvmStatic external fun getNativeRemotePort(): Int
        @JvmStatic external fun createNativeAdvertisement(): BedrockPong
    }

    val isRunning: Boolean
        get() = channelFuture != null

    private var channelFuture: ChannelFuture? = null
    private var luminaRelaySession: LuminaRelaySession? = null
    private var remoteAddress: LuminaAddress? = null
    private val eventLoopGroup: EventLoopGroup = NioEventLoopGroup()

    fun capture(
        remoteAddress: LuminaAddress = LuminaAddress(getNativeRemoteIp(), getNativeRemotePort()),
        onSessionCreated: LuminaRelaySession.() -> Unit
    ): LuminaRelay {
        if (isRunning) {
            return this
        }

        this.remoteAddress = remoteAddress

        advertisement
            .ipv4Port(localAddress.port)
            .ipv6Port(localAddress.port)

        ServerBootstrap()
            .group(eventLoopGroup)
            .channelFactory(RakChannelFactory.server(NioDatagramChannel::class.java))
            .option(RakChannelOption.RAK_ADVERTISEMENT, advertisement.toByteBuf())
            .option(RakChannelOption.RAK_GUID, Random.nextLong())
            .childHandler(object : BedrockChannelInitializer<LuminaRelaySession.ServerSession>() {
                override fun createSession0(peer: BedrockPeer, subClientId: Int): LuminaRelaySession.ServerSession {
                    return LuminaRelaySession(peer, subClientId, this@LuminaRelay)
                        .also {
                            luminaRelaySession = it
                            it.onSessionCreated()
                        }
                        .server
                }

                override fun initSession(session: LuminaRelaySession.ServerSession) {}
                override fun preInitChannel(channel: Channel) {
                    channel.attr(PacketDirection.ATTRIBUTE).set(PacketDirection.CLIENT_BOUND)
                    super.preInitChannel(channel)
                }
            })
            .localAddress(localAddress.inetSocketAddress)
            .bind()
            .awaitUninterruptibly()
            .also {
                it.channel().pipeline().remove(RakServerRateLimiter.NAME)
                channelFuture = it
            }

        return this
    }

    internal fun connectToServer(onSessionCreated: ClientSession.() -> Unit) {
        val clientGUID = Random.nextLong()

        Bootstrap()
            .group(eventLoopGroup)
            .channelFactory(RakChannelFactory.client(NioDatagramChannel::class.java))
            .option(RakChannelOption.RAK_PROTOCOL_VERSION, luminaRelaySession!!.server.codec.raknetProtocolVersion)
            .option(RakChannelOption.RAK_GUID, clientGUID)
            .option(RakChannelOption.RAK_REMOTE_GUID, clientGUID)
            .option(RakChannelOption.RAK_CONNECT_TIMEOUT, 690000)
            .handler(object : BedrockChannelInitializer<ClientSession>() {
                override fun createSession0(peer: BedrockPeer, subClientId: Int): ClientSession {
                    return luminaRelaySession!!.ClientSession(peer, subClientId)
                }

                override fun initSession(clientSession: ClientSession) {
                    luminaRelaySession!!.client = clientSession
                    onSessionCreated(clientSession)
                }

                override fun preInitChannel(channel: Channel) {
                    channel.attr(PacketDirection.ATTRIBUTE).set(PacketDirection.SERVER_BOUND)
                    super.preInitChannel(channel)
                }
            })
            .remoteAddress(remoteAddress!!.inetSocketAddress)
            .connect()
            .awaitUninterruptibly()
    }

    fun disconnect() {
        if (!isRunning) {
            return
        }

        channelFuture?.channel()?.also {
            it.close().awaitUninterruptibly()
            it.parent().close().awaitUninterruptibly()
        }
        channelFuture = null
        luminaRelaySession = null
    }
}