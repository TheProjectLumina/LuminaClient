package com.project.lumina.relay.util

import org.cloudburstmc.protocol.bedrock.codec.BedrockCodec

/**
 * Debug helper to verify codec configuration
 */
object CodecDebugHelper {

    fun printCodecInfo(codec: BedrockCodec, label: String = "Codec") {
        println("═══════════════════════════════════════")
        println("$label Information:")
        println("═══════════════════════════════════════")
        println("Protocol Version: ${codec.protocolVersion}")
        println("Minecraft Version: ${codec.minecraftVersion}")
        println("RakNet Protocol: ${codec.raknetProtocolVersion}")
        println("═══════════════════════════════════════")
    }

    fun verifyProtocolVersion(codec: BedrockCodec, expectedVersion: Int): Boolean {
        val actual = codec.protocolVersion
        val matches = actual == expectedVersion

        if (!matches) {
            println("Protocol Version Mismatch!")
            println("Expected: $expectedVersion")
            println("Actual:   $actual")
        } else {
            println("Protocol Version Correct: $actual")
        }

        return matches
    }

    fun compareCodecs(codec1: BedrockCodec, codec2: BedrockCodec, label1: String = "Codec 1", label2: String = "Codec 2") {
        println("═══════════════════════════════════════")
        println("Codec Comparison:")
        println("═══════════════════════════════════════")
        println("$label1 Protocol: ${codec1.protocolVersion}")
        println("$label2 Protocol: ${codec2.protocolVersion}")
        println("Match: ${codec1.protocolVersion == codec2.protocolVersion}")
        println("═══════════════════════════════════════")
    }
}