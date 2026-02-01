package com.project.lumina.client.discord

import android.content.Context
import android.util.Log
import com.google.gson.JsonObject
import com.project.lumina.client.discord.model.DiscordUser
import com.project.lumina.rpc.LunarisRPC
import com.project.lumina.rpc.callback.LunarisRPCCallback
import com.project.lumina.rpc.entities.Activity
import com.project.lumina.rpc.entities.Assets
import com.project.lumina.rpc.entities.Presence
import com.project.lumina.rpc.entities.Timestamps
import kotlinx.serialization.json.Json

interface RPCCallback {
    fun onConnected(user: DiscordUser?)
    fun onDisconnected()
    fun onError(message: String)
}

class RPCManager(
    private val context: Context,
    private val token: String,
    private val callback: RPCCallback?
) : LunarisRPCCallback {

    private var discordRPC: LunarisRPC? = null
    private var startTimestamp: Long = 0L
    private var isRunning: Boolean = false
    private var presenceConfig: PresenceConfig? = null

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    fun start() {
        if (isRunning) {
            Log.w(TAG, "RPC already running, ignoring start request")
            return
        }
        Log.i(TAG, "Starting RPC connection")
        startTimestamp = System.currentTimeMillis()
        loadPresenceConfig()
        discordRPC = LunarisRPC(token, this)
        discordRPC?.connect()
    }

    fun stop() {
        if (!isRunning) {
            Log.w(TAG, "RPC not running, ignoring stop request")
            return
        }
        Log.i(TAG, "Stopping RPC connection")
        discordRPC?.disconnect()
        isRunning = false
        callback?.onDisconnected()
    }

    fun isRunning(): Boolean = isRunning

    override fun onReady(userJson: JsonObject?) {
        Log.i(TAG, "RPC connection ready")
        isRunning = true
        sendPresence()
        val user = parseAndSaveUser(userJson)
        callback?.onConnected(user)
    }

    private fun parseAndSaveUser(userJson: JsonObject?): DiscordUser? {
        if (userJson == null) {
            Log.w(TAG, "No user data received from gateway")
            return null
        }
        return try {
            val userJsonString = userJson.toString()
            Log.d(TAG, "Parsing user data: $userJsonString")
            val user = json.decodeFromString<DiscordUser>(userJsonString)
            DiscordUserManager.saveUser(context, user)
            Log.i(TAG, "User data saved: ${user.username}")
            user
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse user data", e)
            null
        }
    }

    override fun onDisconnected() {
        Log.i(TAG, "RPC disconnected")
        isRunning = false
        callback?.onDisconnected()
    }

    override fun onError(error: Exception?) {
        val message = error?.message ?: "Unknown error"
        Log.e(TAG, "RPC error: $message", error)
        isRunning = false
        callback?.onError(message)
    }

    private fun loadPresenceConfig() {
        try {
            presenceConfig = PresenceConfig.load(context)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load presence config", e)
        }
    }

    private fun sendPresence(state: PresenceState? = null) {
        Log.i(TAG, "Sending presence update with state: $state")
        val config = presenceConfig ?: return

        val stateString = when (state) {
            is PresenceState.Connecting -> (state as PresenceState.Connecting).toStateString()
            is PresenceState.InSection -> state.toStateString()
            is PresenceState.JoiningServer -> (state as PresenceState.JoiningServer).toStateString()
            is PresenceState.PlayingInServer -> state.toStateString()
            null -> PresenceState.InSection(AppSection.HOME).toStateString()
        }

        val detailsString = when (state) {
            is PresenceState.PlayingInServer -> state.toDetailsString()
            else -> null
        }?.takeIf { it.isNotEmpty() }

        val assets = Assets.Builder()
            .setLargeImage(config.assets.largeImage)
            .setSmallImage(config.assets.smallImage)
            .setLargeText(config.assets.largeText)
            .setSmallText(config.assets.smallText)
            .build()

        val showJoinButton = PresenceStateManager.showJoinButton.value
        val isInGame = state is PresenceState.PlayingInServer

        val firstButtonLabel: String
        val firstButtonUrl: String

        if (showJoinButton && isInGame) {
            val serverIp = PresenceStateManager.getRawServerIp()
            val serverPort = PresenceStateManager.getServerPort()
            firstButtonLabel = "Join Game Server"
            firstButtonUrl = "minecraft://connect/?serverUrl=$serverIp&serverPort=$serverPort"
        } else {
            firstButtonLabel = "Download"
            firstButtonUrl = "https://projectlumina.online"
        }

        val activityBuilder = Activity.Builder()
            .setName(config.activity.name)
            .setState(stateString)
            .setType(config.activity.type)
            .setApplicationId(config.applicationId)
            .setTimestamps(Timestamps(startTimestamp, null))
            .setAssets(assets)
            .setButtons(
                firstButtonLabel,
                firstButtonUrl,
                "Join Discord",
                "https://discord.gg/q3KR8PkMp9"
            )

        if (detailsString != null) {
            activityBuilder.setDetails(detailsString)
        }

        val activity = activityBuilder.build()

        val presence = Presence(
            listOf(activity),
            false,
            0L,
            config.status
        )

        discordRPC?.updatePresence(presence)
    }

    fun updatePresenceState(state: PresenceState) {
        if (!isRunning) {
            Log.w(TAG, "RPC not running, cannot update presence state")
            return
        }
        Log.i(TAG, "Updating presence state: $state")
        sendPresence(state)
    }

    fun getStartTimestamp(): Long = startTimestamp

    fun release() {
        Log.i(TAG, "Releasing RPC resources")
        if (isRunning) {
            stop()
        }
        discordRPC = null
        startTimestamp = 0L
    }

    companion object {
        private const val TAG = "RPCManager"
    }
}