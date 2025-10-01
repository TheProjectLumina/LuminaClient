package com.project.lumina.relay.handler

import net.kyori.adventure.text.Component
import org.cloudburstmc.protocol.bedrock.packet.BedrockPacketHandler

class SessionCloseHandler(private val onSessionClose: (String) -> Unit) : BedrockPacketHandler {

    override fun onDisconnect(reason: CharSequence) {
        
        onSessionClose(reason.toString())
    }
}