package com.project.lumina.client.game

import org.cloudburstmc.protocol.bedrock.packet.BedrockPacket

data class InterceptablePacket(val packet: BedrockPacket) {

    var isIntercepted = false

    fun intercept() {
        isIntercepted = true
    }

}
