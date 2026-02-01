package com.project.lumina.client.constructors

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.edit
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.project.lumina.client.application.AppContext
import com.project.lumina.client.model.Account
import com.project.lumina.relay.util.XboxDeviceInfo
import java.io.File
import java.lang.reflect.Type

object AccountManager {

    private const val ACCOUNT_SHARED_PREFERENCES = "account"

    private const val KEY_CURRENT_MICROSOFT_REFRESH_TOKEN = "MICROSOFT_REFRESH_TOKEN"

    val accounts = mutableStateListOf<Account>()

    private var currentRefreshToken: String?
        get() {
            val sharedPreferences =
                AppContext.instance.getSharedPreferences(
                    ACCOUNT_SHARED_PREFERENCES,
                    Context.MODE_PRIVATE
                )
            return sharedPreferences.getString(KEY_CURRENT_MICROSOFT_REFRESH_TOKEN, null)
                ?.ifEmpty { null }
        }
        set(value) {
            AppContext.instance.getSharedPreferences(
                ACCOUNT_SHARED_PREFERENCES,
                Context.MODE_PRIVATE
            ).edit {
                putString(KEY_CURRENT_MICROSOFT_REFRESH_TOKEN, value ?: "")
            }
        }

    var currentAccount: Account? by mutableStateOf(null)
        private set

    fun selectAccount(account: Account?) {
        currentAccount = account
        if (account == null) {
            currentRefreshToken = null
        } else if (accounts.contains(account)) {
            currentRefreshToken = account.refreshToken
        }
    }

    private val storeFile = File(AppContext.instance.filesDir, "credentials.json")

    private val gson = GsonBuilder()
        .registerTypeAdapter(XboxDeviceInfo::class.java, DeviceInfoAdapter())
        .create()

    init {
        load()
        selectAccount(currentRefreshToken?.let { t -> accounts.find { it.refreshToken == t } })
    }

    private fun load() {
        accounts.clear()
        if (!storeFile.exists()) {
            currentRefreshToken = null
            return
        }
        accounts.addAll(gson.fromJson(storeFile.reader(Charsets.UTF_8), Array<Account>::class.java))
        cleanupCurrentRefreshToken()
    }

    fun save() {
        storeFile.writeText(gson.toJson(accounts.toTypedArray(), Array<Account>::class.java))
    }

    private fun cleanupCurrentRefreshToken() {
        val current = currentRefreshToken
        accounts.forEach {
            if (it.refreshToken == current) {
                return
            }
        }
        currentRefreshToken = null
    }

    private class DeviceInfoAdapter : JsonSerializer<XboxDeviceInfo>,
        JsonDeserializer<XboxDeviceInfo> {

        override fun serialize(
            src: XboxDeviceInfo,
            typeOf: Type?,
            ctx: JsonSerializationContext?
        ): JsonElement {
            return JsonPrimitive(src.deviceType)
        }

        override fun deserialize(
            json: JsonElement,
            typeOf: Type?,
            ctx: JsonDeserializationContext?
        ): XboxDeviceInfo {
            return XboxDeviceInfo.devices[json.asString] ?: XboxDeviceInfo.DEVICE_ANDROID
        }
    }

}