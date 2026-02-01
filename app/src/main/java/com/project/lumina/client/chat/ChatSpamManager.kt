package com.project.lumina.client.chat

import android.util.Log
import com.project.lumina.client.constructors.GameManager
import kotlinx.coroutines.*
import net.kyori.adventure.text.Component
import org.cloudburstmc.protocol.bedrock.packet.TextPacket
import org.cloudburstmc.protocol.bedrock.packet.CommandRequestPacket
import org.cloudburstmc.protocol.bedrock.data.command.CommandOriginData
import org.cloudburstmc.protocol.bedrock.data.command.CommandOriginType
import java.util.UUID

object ChatSpamManager {
    private var isSpamming = false
    private var currentMessage: ChatMessage? = null
    private var spamJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    fun startSpam(message: ChatMessage) {
        if (isSpamming) {
            stopSpam()
        }

        currentMessage = message
        isSpamming = true

        spamJob = scope.launch {
            try {
                while (isSpamming && currentMessage != null) {
                    sendMessageToServer(message.message)
                    delay(message.spamInterval.toLong())
                }
            } catch (e: Exception) {
                Log.e("ChatSpamManager", "Error in spam loop", e)
                stopSpam()
            }
        }

        Log.d("ChatSpamManager", "Started spamming: ${message.message} (${message.spamInterval}ms)")
    }

    fun stopSpam() {
        isSpamming = false
        spamJob?.cancel()
        spamJob = null
        val msg = currentMessage?.message ?: "unknown"
        currentMessage = null
        Log.d("ChatSpamManager", "Stopped spamming: $msg")
    }

    fun isSpamming(): Boolean = isSpamming

    fun getCurrentMessage(): ChatMessage? = currentMessage

    suspend fun sendMessageToServer(message: String) {
        try {
            val session = GameManager.netBound
            if (session != null) {
                if (message.startsWith("/")) {
                    val commandPacket = CommandRequestPacket().apply {
                        command = message.substring(1)
                        commandOriginData = CommandOriginData(
                            CommandOriginType.PLAYER,
                            UUID.randomUUID(),
                            "",
                            0L
                        )
                        internal = false
                        version = session.protocolVersion
                    }
                    session.serverBound(commandPacket)
                    Log.d("ChatSpamManager", "Sent command: ${message}")
                } else {
                    val textPacket = TextPacket().apply {
                        type = TextPacket.Type.CHAT
                        this.message = message
                        sourceName = ""
                        xuid = ""
                        platformChatId = ""
                        filteredMessage = ""
                    }
                    session.serverBound(textPacket)
                    Log.d("ChatSpamManager", "Sent chat message: ${message}")
                }
            }
        } catch (e: Exception) {
            Log.e("ChatSpamManager", "Error sending message", e)
        }
    }
}