package com.project.lumina.client.discord

import android.content.Context
import android.util.Log
import com.project.lumina.client.discord.model.DiscordUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

object RPCService {
    private const val TAG = "RPCService"
    private const val PREFS_NAME = "SettingsPrefs"
    private const val KEY_RPC_ENABLED = "discordRpcEnabled"

    private var rpcManager: RPCManager? = null
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var stateCollectionJob: kotlinx.coroutines.Job? = null

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _isConnecting = MutableStateFlow(false)
    val isConnecting: StateFlow<Boolean> = _isConnecting.asStateFlow()

    private val _currentUser = MutableStateFlow<DiscordUser?>(null)
    val currentUser: StateFlow<DiscordUser?> = _currentUser.asStateFlow()

    fun autoStartForClientMode(context: Context, callback: RPCCallback? = null) {
        if (!TokenManager.hasToken(context)) {
            Log.w(TAG, "No Discord token available for auto-start")
            return
        }

        if (!getSavedState(context)) {
            Log.i(TAG, "RPC was disabled last time, skipping auto-start")
            return
        }

        if (_isConnecting.value || _isConnected.value) {
            Log.i(TAG, "RPC already connecting or connected, skipping auto-start")
            return
        }

        Log.i(TAG, "Auto-starting RPC for Client Mode (was enabled last session)")
        PresenceStateManager.reset()

        start(context, object : RPCCallback {
            override fun onConnected(user: DiscordUser?) {
                Log.i(TAG, "Auto-start RPC connected successfully")
                PresenceStateManager.setCurrentSection(PresenceStateManager.currentSection.value)
                subscribeToStateChanges()
                callback?.onConnected(user)
            }

            override fun onDisconnected() {
                Log.i(TAG, "Auto-start RPC disconnected")
                stopStateCollection()
                callback?.onDisconnected()
            }

            override fun onError(message: String) {
                Log.e(TAG, "Auto-start RPC error: $message")
                stopStateCollection()
                callback?.onError(message)
            }
        })
    }

    fun start(context: Context, callback: RPCCallback? = null) {
        if (_isConnecting.value || _isConnected.value) {
            Log.w(TAG, "RPC already connecting or connected")
            return
        }

        val token = TokenManager.getToken(context)
        if (token == null) {
            Log.w(TAG, "No Discord token available")
            callback?.onError("No Discord token available")
            return
        }

        _isConnecting.value = true

        rpcManager = RPCManager(context, token, object : RPCCallback {
            override fun onConnected(user: DiscordUser?) {
                _isConnecting.value = false
                _isConnected.value = true
                _currentUser.value = user
                saveState(context, true)
                PresenceStateManager.setCurrentSection(PresenceStateManager.currentSection.value)
                subscribeToStateChanges()
                callback?.onConnected(user)
            }

            override fun onDisconnected() {
                _isConnecting.value = false
                _isConnected.value = false
                saveState(context, false)
                stopStateCollection()
                callback?.onDisconnected()
            }

            override fun onError(message: String) {
                _isConnecting.value = false
                _isConnected.value = false
                saveState(context, false)
                stopStateCollection()
                callback?.onError(message)
            }
        })
        rpcManager?.start()
    }

    fun stop(context: Context) {
        Log.i(TAG, "Stopping RPC service")
        stopStateCollection()
        rpcManager?.stop()
        rpcManager?.release()
        rpcManager = null
        _isConnected.value = false
        _isConnecting.value = false
        saveState(context, false)
    }

    fun release(context: Context) {
        Log.i(TAG, "Releasing RPC service resources")
        stop(context)
    }

    fun isRunning(): Boolean = _isConnected.value

    fun updatePresence(state: PresenceState) {
        if (!_isConnected.value) {
            Log.w(TAG, "RPC not connected, cannot update presence")
            return
        }
        rpcManager?.updatePresenceState(state)
    }

    private fun subscribeToStateChanges() {
        stopStateCollection()
        stateCollectionJob = serviceScope.launch {
            PresenceStateManager.currentState.collect { state ->
                if (_isConnected.value) {
                    Log.d(TAG, "State changed, updating presence: $state")
                    updatePresence(state)
                }
            }
        }
    }

    private fun stopStateCollection() {
        stateCollectionJob?.cancel()
        stateCollectionJob = null
    }

    private fun saveState(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_RPC_ENABLED, enabled)
            .apply()
    }

    fun getSavedState(context: Context): Boolean {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_RPC_ENABLED, false)
    }
}