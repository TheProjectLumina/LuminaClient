package com.project.lumina.client.discord

import android.content.Context
import com.project.lumina.client.constructors.AccountManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AppSection(val displayName: String) {
    HOME("Home"),
    SETTINGS("Settings"),
    REALMS("Realms"),
    DISCORD("Discord"),
    ABOUT("About")
}

sealed class PresenceState {
    object Connecting : PresenceState() {
        fun toStateString(): String = "Connecting..."
    }

    data class InSection(val section: AppSection) : PresenceState() {
        fun toStateString(): String = "In ${section.displayName} Section"
    }

    object JoiningServer : PresenceState() {
        fun toStateString(): String = "Joining a Server..."
    }

    data class PlayingInServer(
        val serverIp: String,
        val username: String,
        val showServerInfo: Boolean = true
    ) : PresenceState() {
        fun toStateString(): String = if (showServerInfo) "Playing in $serverIp" else "Playing on a server"
        fun toDetailsString(): String? = if (showServerInfo) "User: $username" else null
    }
}

object PresenceStateManager {
    private const val PREFS_NAME = "SettingsPrefs"
    private const val KEY_SHOW_SERVER_INFO = "showServerInfo"
    private const val KEY_SHOW_JOIN_BUTTON = "showJoinButton"

    private val _currentState = MutableStateFlow<PresenceState>(PresenceState.InSection(AppSection.HOME))
    val currentState: StateFlow<PresenceState> = _currentState.asStateFlow()

    private val _currentSection = MutableStateFlow(AppSection.HOME)
    val currentSection: StateFlow<AppSection> = _currentSection.asStateFlow()

    private val _isRelayActive = MutableStateFlow(false)
    val isRelayActive: StateFlow<Boolean> = _isRelayActive.asStateFlow()

    private val _isInGame = MutableStateFlow(false)
    val isInGame: StateFlow<Boolean> = _isInGame.asStateFlow()

    private val _showServerInfo = MutableStateFlow(true)
    val showServerInfo: StateFlow<Boolean> = _showServerInfo.asStateFlow()

    private val _showJoinButton = MutableStateFlow(false)
    val showJoinButton: StateFlow<Boolean> = _showJoinButton.asStateFlow()

    private var lastServerIp: String = ""
    private var rawServerIp: String = ""
    private var serverPort: Int = 19132

    fun init(context: Context) {
        _showServerInfo.value = getShowServerInfo(context)
        _showJoinButton.value = getShowJoinButton(context)
    }

    fun setShowServerInfo(context: Context, show: Boolean) {
        _showServerInfo.value = show
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_SHOW_SERVER_INFO, show)
            .apply()
        if (_isInGame.value && lastServerIp.isNotEmpty()) {
            val username = getCurrentUsername()
            _currentState.value = PresenceState.PlayingInServer(lastServerIp, username, show)
        }
    }

    fun getShowServerInfo(context: Context): Boolean {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_SHOW_SERVER_INFO, true)
    }

    fun setShowJoinButton(context: Context, show: Boolean) {
        _showJoinButton.value = show
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_SHOW_JOIN_BUTTON, show)
            .apply()
        if (_isInGame.value) {
            _currentState.value = PresenceState.PlayingInServer(lastServerIp, getCurrentUsername(), _showServerInfo.value)
        }
    }

    fun getShowJoinButton(context: Context): Boolean {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_SHOW_JOIN_BUTTON, false)
    }

    fun getRawServerIp(): String = rawServerIp

    fun getServerPort(): Int = serverPort

    fun setServerInfo(ip: String, port: Int) {
        rawServerIp = ip
        serverPort = port
    }

    fun setCurrentSection(section: AppSection) {
        _currentSection.value = section
        if (!_isRelayActive.value) {
            _currentState.value = PresenceState.InSection(section)
        }
    }

    fun onRelayStarted() {
        _isRelayActive.value = true
        _isInGame.value = false
        _currentState.value = PresenceState.JoiningServer
    }

    fun onGameJoined(serverIp: String) {
        _isInGame.value = true
        lastServerIp = formatServerIp(serverIp)
        val username = getCurrentUsername()
        _currentState.value = PresenceState.PlayingInServer(lastServerIp, username, _showServerInfo.value)
    }

    fun onServerDisconnected() {
        if (_isRelayActive.value) {
            _isInGame.value = false
            lastServerIp = ""
            _currentState.value = PresenceState.JoiningServer
        }
    }

    fun onRelayDisconnected() {
        _isRelayActive.value = false
        _isInGame.value = false
        lastServerIp = ""
        _currentState.value = PresenceState.InSection(_currentSection.value)
    }

    fun formatServerIp(rawIp: String): String {
        if (rawIp.isEmpty()) return rawIp
        val parts = rawIp.split(".")
        if (parts.size <= 2) return rawIp
        val isNumericIp = parts.all { part -> part.toIntOrNull() != null }
        if (isNumericIp) return rawIp
        return parts.drop(1).joinToString(".")
    }

    fun getCurrentUsername(): String {
        return AccountManager.currentAccount?.remark ?: "Guest"
    }

    fun reset() {
        _currentSection.value = AppSection.HOME
        _isRelayActive.value = false
        _isInGame.value = false
        lastServerIp = ""
        _currentState.value = PresenceState.InSection(AppSection.HOME)
    }
}