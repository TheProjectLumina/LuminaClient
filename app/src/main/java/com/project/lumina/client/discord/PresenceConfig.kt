package com.project.lumina.client.discord

import android.content.Context
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName

data class PresenceConfig(
    @SerializedName("applicationId")
    val applicationId: String,
    @SerializedName("activity")
    val activity: ActivityConfig,
    @SerializedName("assets")
    val assets: AssetsConfig,
    @SerializedName("status")
    val status: String
) {
    companion object {
        private const val CONFIG_FILE = "discord_presence.json"

        fun load(context: Context): PresenceConfig {
            val json = context.assets.open(CONFIG_FILE).bufferedReader().use { it.readText() }
            return Gson().fromJson(json, PresenceConfig::class.java)
        }
    }
}

data class ActivityConfig(
    @SerializedName("name")
    val name: String,
    @SerializedName("type")
    val type: Int
)

data class AssetsConfig(
    @SerializedName("largeImage")
    val largeImage: String?,
    @SerializedName("smallImage")
    val smallImage: String?,
    @SerializedName("largeText")
    val largeText: String?,
    @SerializedName("smallText")
    val smallText: String?
)