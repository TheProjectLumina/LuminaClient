package com.project.lumina.relay.listener

import com.project.lumina.relay.util.IXboxIdentityTokenCache
import com.project.lumina.relay.util.XboxDeviceInfo
import com.project.lumina.relay.util.XboxIdentityToken
import com.project.lumina.relay.util.fetchChain
import com.project.lumina.relay.util.fetchIdentityToken
import com.project.lumina.relay.util.signJWT
import net.kyori.adventure.text.Component
import org.cloudburstmc.protocol.bedrock.data.auth.AuthType
import org.cloudburstmc.protocol.bedrock.data.auth.CertificateChainPayload
import org.cloudburstmc.protocol.bedrock.packet.BedrockPacket
import org.cloudburstmc.protocol.bedrock.packet.DisconnectPacket
import org.cloudburstmc.protocol.bedrock.packet.LoginPacket

@Suppress("MemberVisibilityCanBePrivate")
class XboxLoginPacketListener(
    val accessToken: () -> String,
    val deviceInfo: XboxDeviceInfo
) : EncryptedLoginPacketListener() {

    var tokenCache: IXboxIdentityTokenCache? = null

    private var identityToken = XboxIdentityToken("", 0)
        get() {
            if (field.notAfter < System.currentTimeMillis() / 1000) {
                field = tokenCache?.checkCache(deviceInfo)?.also {
                    println("Token cache hit")
                } ?: fetchIdentityToken(accessToken(), deviceInfo).also {
                    tokenCache?.let { cache ->
                        println("Saving token cache")
                        cache.cache(deviceInfo, it)
                    }
                }
            }
            return field
        }

    private val chain: List<String>
        get() = fetchChain(identityToken.token, keyPair)

    fun forceFetchChain() {
        chain
    }

    override fun beforeClientBound(packet: BedrockPacket): Boolean {
        if (packet is LoginPacket) {
            try {
                println("Protocol Version: ${packet.protocolVersion}")

                packet.authPayload = CertificateChainPayload(chain, AuthType.FULL)

                val clientJwtPayload = packet.clientJwt?.split('.')?.getOrNull(1)
                    ?: throw IllegalStateException("Invalid clientJwt format")
                packet.clientJwt = signJWT(clientJwtPayload, keyPair, base64Encoded = true)
            } catch (e: Throwable) {
                // Use reflection to set private field since no setter exists
                val disconnectPacket = DisconnectPacket()
                val field = disconnectPacket.javaClass.getDeclaredField("kickMessage")
                field.isAccessible = true
                field.set(disconnectPacket, "Login failed: ${e.message ?: e.toString()}")
                luminaRelaySession.clientBound(disconnectPacket)



                println("Login failed: $e")
                return false
            }

            println("Login success")
            loginPacket = packet
            connectServer()
            return true
        }
        return false
    }
}