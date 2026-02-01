package com.project.lumina.client.discord

import android.content.Context
import android.content.SharedPreferences
import com.project.lumina.client.discord.model.DiscordUser
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object DiscordUserManager {
    private const val PREFS_NAME = "lumina_discord_user_prefs"
    private const val KEY_USER_DATA = "discord_user_data"

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun saveUser(context: Context, user: DiscordUser) {
        val userJson = json.encodeToString(user)
        getPreferences(context).edit()
            .putString(KEY_USER_DATA, userJson)
            .apply()
    }

    fun getUser(context: Context): DiscordUser? {
        val userJson = getPreferences(context).getString(KEY_USER_DATA, null)
            ?: return null
        return try {
            json.decodeFromString<DiscordUser>(userJson)
        } catch (e: Exception) {
            null
        }
    }

    fun clearUser(context: Context) {
        getPreferences(context).edit()
            .remove(KEY_USER_DATA)
            .apply()
    }

    fun hasUser(context: Context): Boolean {
        return getUser(context) != null
    }
}