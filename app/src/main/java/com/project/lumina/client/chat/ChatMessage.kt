package com.project.lumina.client.chat

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isSpamEnabled: Boolean = false,
    val spamInterval: Int = 1000
)