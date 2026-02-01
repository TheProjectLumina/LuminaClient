package com.project.lumina.client.discord.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DiscordBadge(
    @SerialName("id")
    val id: String,
    @SerialName("description")
    val description: String,
    @SerialName("icon")
    val icon: String
)