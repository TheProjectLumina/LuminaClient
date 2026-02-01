package com.project.lumina.relay.listener

import com.project.lumina.relay.LuminaRelaySession
import org.cloudburstmc.protocol.bedrock.packet.BedrockPacket
import org.cloudburstmc.protocol.bedrock.packet.BiomeDefinitionListPacket

@Suppress("MemberVisibilityCanBePrivate")
class BiomeDefinitionListPacketListener(
    val luminaRelaySession: LuminaRelaySession
) : LuminaRelayPacketListener {

    companion object {
        private const val TAG = "BiomeDefinitionListPacketListener"
        private var awtErrorDetected = false
    }

    override fun beforeClientBound(packet: BedrockPacket): Boolean {
        if (packet is BiomeDefinitionListPacket) {
            return handleBiomeDefinitionListPacket(packet, isClientBound = true)
        }
        return false
    }

    override fun beforeServerBound(packet: BedrockPacket): Boolean {
        if (packet is BiomeDefinitionListPacket) {
            return handleBiomeDefinitionListPacket(packet, isClientBound = false)
        }
        return false
    }

    private fun handleBiomeDefinitionListPacket(packet: BiomeDefinitionListPacket, isClientBound: Boolean): Boolean {
        return try {
            if (wouldCauseAwtError(packet)) {
                println("[$TAG] Skipping BiomeDefinitionListPacket due to AWT compatibility issue")
                awtErrorDetected = true
                
                val fallbackPacket = BiomeDefinitionListPacket()
                fallbackPacket.definitions = null
                fallbackPacket.biomes = null
                
                if (isClientBound) {
                    luminaRelaySession.clientBound(fallbackPacket)
                } else {
                    luminaRelaySession.serverBound(fallbackPacket)
                }
                
                return true
            }
            
            false
        } catch (e: Exception) {
            println("[$TAG] Error handling BiomeDefinitionListPacket: ${e.message}")
            true
        }
    }

    private fun wouldCauseAwtError(packet: BiomeDefinitionListPacket): Boolean {
        if (awtErrorDetected) {
            return true
        }
        
        if (packet.biomes != null) {
            return true
        }
        
        try {
            Class.forName("java.awt.Color")
            return false
        } catch (e: ClassNotFoundException) {
            awtErrorDetected = true
            return true
        } catch (e: NoClassDefFoundError) {
            awtErrorDetected = true
            return true
        }
    }

    override fun onDisconnect(reason: String) {
        if (awtErrorDetected) {
            println("[$TAG] Session disconnected. AWT compatibility issues were detected during this session.")
        }
    }
}
