package com.project.lumina.relay.listener

import com.google.gson.JsonObject
import com.project.lumina.relay.util.*
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

                val skinData = jwtPayload(packet.clientJwt!!)
                    ?: throw IllegalStateException("Failed to parse skin data")

                packet.clientJwt = forgeSkinData(keyPair, skinData)

                println("Login success - Chain built with ${chain.size} certificates")
            } catch (e: Throwable) {
                val disconnectPacket = DisconnectPacket()
                try {
                    val field = disconnectPacket.javaClass.getDeclaredField("kickMessage")
                    field.isAccessible = true
                    field.set(disconnectPacket, "Login failed: ${e.message ?: e.toString()}")
                } catch (reflectionError: Exception) {
                    println("Failed to set disconnect message: $reflectionError")
                }

                luminaRelaySession.clientBound(disconnectPacket)
                println("Login failed: $e")
                e.printStackTrace()
                return false
            }

            loginPacket = packet
            connectServer()
            return true
        }
        return false
    }


    private fun extractExtraDataFromChain(packet: LoginPacket): JsonObject? {
        if (packet.authPayload is CertificateChainPayload) {
            val chain = (packet.authPayload as CertificateChainPayload).chain

            chain.forEach { jwt ->
                val payload = jwtPayload(jwt)
                if (payload?.has("extraData") == true) {
                    return payload.getAsJsonObject("extraData")
                }
            }
        }
        return null
    }
}