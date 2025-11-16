package com.project.lumina.relay.listener

import com.google.gson.JsonParser
import com.project.lumina.relay.LuminaRelaySession
import com.project.lumina.relay.util.base64Decode
import com.project.lumina.relay.util.gson
import com.project.lumina.relay.util.jwtPayload
import com.project.lumina.relay.util.signJWT
import net.kyori.adventure.text.Component
import org.cloudburstmc.protocol.bedrock.data.PacketCompressionAlgorithm
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
            var newChain: String? = null
            if (packet.authPayload is CertificateChainPayload) {
                val authPayload = packet.authPayload as CertificateChainPayload
                authPayload.chain.forEach { chain ->
                    val chainBody = jwtPayload(chain) ?: return@forEach
                    if (chainBody.has("extraData")) {
                        chainBody.addProperty(
                            "identityPublicKey",
                            Base64.getEncoder().withoutPadding().encodeToString(keyPair.public.encoded)
                        )
                        newChain = signJWT(gson.toJson(chainBody), keyPair)
                    }
                }
                if (newChain != null) {
                    packet.authPayload = CertificateChainPayload(listOf(newChain!!), authPayload.getAuthType())
                }
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
                    println("Compression threshold set to $threshold")
                } else {
                    luminaRelaySession.client!!.setCompression(PacketCompressionAlgorithm.NONE)
                    println("Compression threshold set to 0")
                }

                loginPacket?.let { luminaRelaySession.serverBoundImmediately(it) }
                    ?: luminaRelaySession.server.disconnect("LoginPacket is null") // Fixed here
                return true
            }
            is ServerToClientHandshakePacket -> {
                val jwtSplit = packet.jwt.split(".")
                val headerObject = JsonParser.parseString(base64Decode(jwtSplit[0]).toString(Charsets.UTF_8)).asJsonObject
                val payloadObject = JsonParser.parseString(base64Decode(jwtSplit[1]).toString(Charsets.UTF_8)).asJsonObject
                val serverKey = EncryptionUtils.parseKey(headerObject.get("x5u").asString)
                val key = EncryptionUtils.getSecretKey(
                    keyPair.private, serverKey,
                    base64Decode(payloadObject.get("salt").asString)
                )
                luminaRelaySession.client!!.enableEncryption(key)
                println("Encryption enabled")
                luminaRelaySession.serverBoundImmediately(ClientToServerHandshakePacket())
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
}