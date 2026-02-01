package com.project.lumina.client.discord.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DiscordUser(
    @SerialName("id")
    val id: String,
    @SerialName("username")
    val username: String,
    @SerialName("discriminator")
    val discriminator: String? = null,
    @SerialName("avatar")
    val avatar: String? = null,
    @SerialName("banner")
    val banner: String? = null,
    @SerialName("global_name")
    val globalName: String? = null,
    @SerialName("bio")
    val bio: String? = null,
    @SerialName("nitro")
    val nitro: Boolean = false,
    @SerialName("badges")
    val badges: List<DiscordBadge>? = null
) {
    fun getAvatarUrl(): String {
        if (avatar == null) {
            val defaultIndex = (id.toLongOrNull() ?: 0L) % 5
            return "https://cdn.discordapp.com/embed/avatars/$defaultIndex.png"
        }
        val extension = if (avatar.startsWith("a_")) "gif" else "png"
        return "https://cdn.discordapp.com/avatars/$id/$avatar.$extension"
    }

    fun getBannerUrl(): String? {
        if (banner == null) return null
        val extension = if (banner.startsWith("a_")) "gif" else "png"
        return "https://cdn.discordapp.com/banners/$id/$banner.$extension?size=480"
    }

    fun getDisplayName(): String {
        return globalName ?: username
    }

    fun getFullUsername(): String {
        return when {
            discriminator == null || discriminator == "0" -> username
            else -> "$username#$discriminator"
        }
    }
}