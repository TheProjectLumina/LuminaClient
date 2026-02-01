package com.project.lumina.client.constructors

import android.util.Log
import androidx.annotation.StringRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.project.lumina.client.game.InterruptiblePacketHandler
import com.project.lumina.client.overlay.manager.OverlayManager
import com.project.lumina.client.overlay.mods.OverlayModuleList
import com.project.lumina.client.overlay.mods.OverlayNotification
import com.project.lumina.client.overlay.manager.OverlayShortcutButton
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.int
import kotlinx.serialization.json.put

abstract class Element(
    val name: String,
    val category: CheatCategory,
    val iconResId: Int = 0,
    defaultEnabled: Boolean = false,
    val private: Boolean = false,
    @StringRes open val displayNameResId: Int? = null
) : InterruptiblePacketHandler,
    Configurable {

    open lateinit var session: NetBound

    private var _isEnabled by mutableStateOf(defaultEnabled)

    var isEnabled: Boolean
        get() = _isEnabled
        set(value) {
            if (_isEnabled != value) {
                _isEnabled = value
                if (value) {
                    onEnabled()
                } else {
                    onDisabled()
                }
            }
        }

    val isSessionCreated: Boolean
        get() = ::session.isInitialized

    var isExpanded by mutableStateOf(false)

    var isShortcutDisplayed by mutableStateOf(false)

    var shortcutX = 0

    var shortcutY = 100

    val overlayShortcutButton by lazy { OverlayShortcutButton(this) }

    override val values: MutableList<Value<*>> = ArrayList()

    open fun onEnabled() {
        ArrayListManager.addModule(this)
        sendToggleMessage(true)
    }

    open fun onDisabled() {
        ArrayListManager.removeModule(this)
        sendToggleMessage(false)
    }

    open fun toJson() = buildJsonObject {
        put("state", isEnabled)
        put("values", buildJsonObject {
            values.forEach { value ->
                val key = if (value.name.isNotEmpty()) value.name else value.nameResId.toString()
                put(key, value.toJson())
            }
        })
        if (isShortcutDisplayed) {
            put("shortcut", buildJsonObject {
                put("x", shortcutX)
                put("y", shortcutY)
            })
        }
    }

    open fun fromJson(jsonElement: JsonElement) {
        if (jsonElement is JsonObject) {
            isEnabled = (jsonElement["state"] as? JsonPrimitive)?.boolean ?: isEnabled
            (jsonElement["values"] as? JsonObject)?.let {
                it.forEach { jsonObject ->
                    val value = getValue(jsonObject.key)
                        ?: if (jsonObject.key.toIntOrNull() != null) {
                            values.find { it.nameResId == jsonObject.key.toInt() }
                        } else null

                    value?.let { v ->
                        try {
                            v.fromJson(jsonObject.value)
                        } catch (e: Throwable) {
                            v.reset()
                        }
                    }
                }
            }
            (jsonElement["shortcut"] as? JsonObject)?.let {
                shortcutX = (it["x"] as? JsonPrimitive)?.int ?: shortcutX
                shortcutY = (it["y"] as? JsonPrimitive)?.int ?: shortcutY
                isShortcutDisplayed = true
            }
        }
    }


    private fun sendToggleMessage(enabled: Boolean) {
        if (!isSessionCreated) {
            return
        }

        val moduleName = name
        val drawable = "toc_24px"

        val moduleDisplayName = name

        try {
            if (enabled) {
                showModuleNotification()
                OverlayModuleList.showText(moduleName)
            } else {
                OverlayModuleList.removeText(moduleName)
            }
        } catch (e: Exception) {
            Log.w("AppCrashChan :3", "Failed to show module notification: ${e.message}")
        }
    }

    private fun showModuleNotification() {
        OverlayNotification.addNotification(name)
        try {
            OverlayManager.showOverlayWindow(OverlayNotification())
        } catch (e: Exception) {
            Log.w("AppCrashChan :3", "Failed to show overlay: ${e.message}")
        }
    }
}


