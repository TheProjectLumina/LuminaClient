package com.project.lumina.relay.listener

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.project.lumina.relay.LuminaRelaySession
import com.project.lumina.relay.util.*
import org.cloudburstmc.protocol.bedrock.data.PacketCompressionAlgorithm
import org.cloudburstmc.protocol.bedrock.data.auth.AuthType
import org.cloudburstmc.protocol.bedrock.data.auth.CertificateChainPayload
import org.cloudburstmc.protocol.bedrock.packet.*
import org.cloudburstmc.protocol.bedrock.util.EncryptionUtils
import java.security.KeyPair
import java.util.Base64

open class EncryptedLoginPacketListener : LuminaRelayPacketListener {

    protected var keyPair: KeyPair = EncryptionUtils.createKeyPair()
    protected var loginPacket: LoginPacket? = null
    lateinit var luminaRelaySession: LuminaRelaySession

    override fun beforeClientBound(packet: BedrockPacket): Boolean {
        if (packet is LoginPacket) {
            try {

                val extraData = extractExtraData(packet)

                val newChain = forgeAuthData(keyPair, extraData)

                packet.authPayload = CertificateChainPayload(listOf(newChain), AuthType.SELF_SIGNED)

                val skinData = extractSkinData(packet)
                packet.clientJwt = forgeSkinData(keyPair, skinData)

                println("Forged authentication chain for encrypted relay")
            } catch (e: Exception) {
                println("Failed to forge authentication chain: $e")
                e.printStackTrace()
                luminaRelaySession.server.disconnect("Authentication failed")
                return false
            }

            loginPacket = packet
            connectServer()
            return true
        }
        return false
    }

    override fun beforeServerBound(packet: BedrockPacket): Boolean {
        when (packet) {
            is NetworkSettingsPacket -> {
                val threshold = packet.compressionThreshold
                if (threshold > 0) {
                    luminaRelaySession.client!!.setCompression(packet.compressionAlgorithm)
                    println("Compression threshold set to $threshold with ${packet.compressionAlgorithm}")
                } else {
                    luminaRelaySession.client!!.setCompression(PacketCompressionAlgorithm.NONE)
                    println("Compression disabled")
                }


                loginPacket?.let {
                    luminaRelaySession.serverBoundImmediately(it)
                    println("Login packet sent to server")
                } ?: run {
                    luminaRelaySession.server.disconnect("LoginPacket is null")
                }
                return true
            }

            is ServerToClientHandshakePacket -> {
                try {

                    val jwtSplit = packet.jwt.split(".")
                    val headerObject = JsonParser.parseString(
                        base64Decode(jwtSplit[0]).toString(Charsets.UTF_8)
                    ).asJsonObject
                    val payloadObject = JsonParser.parseString(
                        base64Decode(jwtSplit[1]).toString(Charsets.UTF_8)
                    ).asJsonObject

                    val serverKey = EncryptionUtils.parseKey(headerObject.get("x5u").asString)


                    val key = EncryptionUtils.getSecretKey(
                        keyPair.private,
                        serverKey,
                        base64Decode(payloadObject.get("salt").asString)
                    )


                    luminaRelaySession.client!!.enableEncryption(key)
                    println("Encryption enabled with server")


                    luminaRelaySession.serverBoundImmediately(ClientToServerHandshakePacket())
                } catch (e: Exception) {
                    println("Failed to complete encryption handshake: $e")
                    e.printStackTrace()
                    luminaRelaySession.server.disconnect("Encryption handshake failed")
                }
                return true
            }
        }
        return false
    }

    protected fun connectServer() {
        luminaRelaySession.luminaRelay.connectToServer {
            println("Connected to server")

            val packet = RequestNetworkSettingsPacket()
            packet.protocolVersion = luminaRelaySession.server.codec.protocolVersion
            luminaRelaySession.serverBoundImmediately(packet)
        }
    }


    private fun extractExtraData(packet: LoginPacket): JsonObject {
        val extraData = JsonObject()

        if (packet.authPayload is CertificateChainPayload) {
            val chain = (packet.authPayload as CertificateChainPayload).chain


            chain.forEach { jwt ->
                val payload = jwtPayload(jwt)
                if (payload?.has("extraData") == true) {
                    val data = payload.getAsJsonObject("extraData")

                    data.entrySet().forEach { (key, value) ->
                        extraData.add(key, value)
                    }
                    return extraData
                }
            }
        }


        extraData.addProperty("displayName", "LuminaUser")
        extraData.addProperty("identity", "00000000-0000-0000-0000-000000000000")

        return extraData
    }


    private fun extractSkinData(packet: LoginPacket): JsonObject {
        return jwtPayload(packet.clientJwt ?: "")
            ?: JsonObject().apply {
                addProperty("SkinId", "")
                addProperty("SkinData", "")
            }
    }
}