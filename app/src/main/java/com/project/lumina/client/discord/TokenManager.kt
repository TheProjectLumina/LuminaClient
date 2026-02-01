package com.project.lumina.client.discord

import android.content.Context
import android.content.SharedPreferences

object TokenManager {
    private const val PREFS_NAME = "lumina_discord_prefs"
    private const val KEY_TOKEN = "discord_token"

    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun saveToken(context: Context, token: String) {
        getPreferences(context).edit()
            .putString(KEY_TOKEN, token)
            .apply()
    }

    fun getToken(context: Context): String? {
        return getPreferences(context).getString(KEY_TOKEN, null)
    }

    fun clearToken(context: Context) {
        getPreferences(context).edit()
            .remove(KEY_TOKEN)
            .apply()
    }

    fun hasToken(context: Context): Boolean {
        return getToken(context) != null
    }
}