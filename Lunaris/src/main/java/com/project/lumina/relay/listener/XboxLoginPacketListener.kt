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
                field = tokenCache?.checkCache(deviceInfo)
                    ?: fetchIdentityToken(accessToken(), deviceInfo).also {
                        tokenCache?.let { cache ->
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
                packet.authPayload = CertificateChainPayload(chain, AuthType.FULL)

                val clientJwtPayload = packet.clientJwt?.split('.')?.getOrNull(1)
                    ?: throw IllegalStateException("Invalid clientJwt format")

                val skinData = jwtPayload(packet.clientJwt!!)
                    ?: throw IllegalStateException("Failed to parse skin data")

                packet.clientJwt = forgeSkinData(keyPair, skinData)

            } catch (e: Exception) {
                val disconnectPacket = DisconnectPacket()
                try {
                    val field = disconnectPacket.javaClass.getDeclaredField("message")
                    field.isAccessible = true
                    field.set(disconnectPacket, "Login failed: ${e.message ?: e.toString()}")
                } catch (reflectionError: Exception) {
                }

                luminaRelaySession.clientBound(disconnectPacket)
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