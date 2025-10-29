package com.project.lumina.relay.util

import coelho.msftauth.api.xbox.XboxDevice
import coelho.msftauth.api.xbox.XboxDeviceAuthRequest
import coelho.msftauth.api.xbox.XboxDeviceKey
import coelho.msftauth.api.xbox.XboxSISUAuthenticateRequest
import coelho.msftauth.api.xbox.XboxSISUAuthorizeRequest
import coelho.msftauth.api.xbox.XboxTitleAuthRequest
import coelho.msftauth.api.xbox.XboxToken
import coelho.msftauth.api.xbox.XboxUserAuthRequest
import coelho.msftauth.api.xbox.XboxXSTSAuthRequest
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import okhttp3.FormBody
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.Reader
import java.security.KeyPair
import java.security.PublicKey
import java.time.Instant
import java.util.Base64
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

val deviceKey = XboxDeviceKey()

val gson: Gson = GsonBuilder()
    .setPrettyPrinting()
    .create()

fun fetchChain(identityToken: String, keyPair: KeyPair): List<String> {
    val rawChain = JsonParser.parseReader(fetchRawChain(identityToken, keyPair.public)).asJsonObject
    val chains = rawChain.get("chain").asJsonArray

    
    val identityPubKey = JsonParser.parseString(base64Decode(chains.get(0).asString.split(".")[0]).toString(Charsets.UTF_8)).asJsonObject

    val jwt = signJWT(gson.toJson(JsonObject().apply {
        addProperty("certificateAuthority", true)
        addProperty("exp", (Instant.now().epochSecond + TimeUnit.HOURS.toSeconds(6)).toInt())
        addProperty("nbf", (Instant.now().epochSecond - TimeUnit.HOURS.toSeconds(6)).toInt())
        addProperty("identityPublicKey", identityPubKey.get("x5u").asString)
    }), keyPair)

    val list = mutableListOf(jwt)
    list.addAll(chains.map { it.asString })
    return list
}

fun fetchRawChain(identityToken: String, publicKey: PublicKey): Reader {
    
    val data = JsonObject().apply {
        addProperty("identityPublicKey", Base64.getEncoder().withoutPadding().encodeToString(publicKey.encoded))
    }
    val request = Request.Builder()
        .url("https://multiplayer.minecraft.net/authentication")
        .post(gson.toJson(data).toRequestBody("application/json".toMediaType()))
        .header("Client-Version", "1.21.60")
        .header("Authorization", identityToken)
        .build()
    val response = HttpUtils.client.newCall(request).execute()

    assert(response.code == 200) { "Http code ${response.code}" }

    return response.body!!.charStream()
}

fun fetchIdentityToken(accessToken: String, deviceInfo: XboxDeviceInfo): XboxIdentityToken {
    var userToken: XboxToken? = null
    val userRequestThread = thread {
        userToken = XboxUserAuthRequest(
            "http://auth.xboxlive.com", "JWT", "RPS",
            "user.auth.xboxlive.com", "t=$accessToken"
        ).request(HttpUtils.client)
    }
    val deviceToken = XboxDeviceAuthRequest(
        "http://auth.xboxlive.com", "JWT", deviceInfo.deviceType,
        "0.0.0.0", deviceKey
    ).request(HttpUtils.client)
    val titleToken = if (deviceInfo.allowDirectTitleTokenFetch) {
        XboxTitleAuthRequest(
            "http://auth.xboxlive.com", "JWT", "RPS",
            "user.auth.xboxlive.com", "t=$accessToken", deviceToken.token, deviceKey
        ).request(HttpUtils.client)
    } else {
        val device = XboxDevice(deviceKey, deviceToken)
        val sisuQuery = XboxSISUAuthenticateRequest.Query("phone")
        val sisuRequest = XboxSISUAuthenticateRequest(
            deviceInfo.appId, device, "service::user.auth.xboxlive.com::MBI_SSL",
            sisuQuery, deviceInfo.xalRedirect, "RETAIL"
        ).request(HttpUtils.client)
        val sisuToken = XboxSISUAuthorizeRequest(
            "t=$accessToken", deviceInfo.appId, device, "RETAIL",
            sisuRequest.sessionId, "user.auth.xboxlive.com", "http://xboxlive.com"
        ).request(HttpUtils.client)
        if (sisuToken.status != 200) {
            val did = deviceToken.displayClaims["xdi"]!!.asJsonObject.get("did").asString
            val sign = deviceKey.sign("/proxy?sessionid=${sisuRequest.sessionId}", null, "POST", null).replace("+", "%2B").replace("=", "%3D")
            val url = sisuToken.webPage.split("#")[0] +
                    "&did=0x$did&redirect=${deviceInfo.xalRedirect}" +
                    "&sid=${sisuRequest.sessionId}&sig=${sign}&state=${sisuQuery.state}"
            throw XboxGamerTagException(url)
        }
        sisuToken.titleToken
    }
    if (userRequestThread.isAlive)
        userRequestThread.join()
    if (userToken == null) error("failed to fetch xbox user token")
    val xstsToken = XboxXSTSAuthRequest(
        "https://multiplayer.minecraft.net/",
        "JWT",
        "RETAIL",
        listOf(userToken),
        titleToken,
        XboxDevice(deviceKey, deviceToken)
    ).request(HttpUtils.client)

    return XboxIdentityToken(xstsToken.toIdentityToken(), Instant.parse(xstsToken.notAfter).epochSecond)
}

/**
 * thown whilst no xbox gamer tag found on account
 */
@Suppress("CanBeParameter", "MemberVisibilityCanBePrivate")
class XboxGamerTagException(val sisuStartUrl: String)
    : IllegalStateException("Have you registered a Xbox GamerTag? You can register it here: $sisuStartUrl")

data class XboxIdentityToken(val token: String, val notAfter: Long) {

    val expired: Boolean
        get() = notAfter < Instant.now().epochSecond

}

@Suppress("MemberVisibilityCanBePrivate")
data class XboxDeviceInfo(val appId: String, val deviceType: String,
                          val allowDirectTitleTokenFetch: Boolean = false,
                          val xalRedirect: String = "") {

    /**
     * @param token refresh token or authorization code
     * @return Pair<AccessToken, RefreshToken>
     */
    fun refreshToken(token: String): Pair<String, String> {
        val form = FormBody.Builder()
        form.add("client_id", appId)
        form.add("redirect_uri", "https://login.live.com/oauth20_desktop.srf")
        
        if (token.split("\n")[0].substring(token.lastIndexOf('.')+1).length == 36) {
            form.add("grant_type", "authorization_code")
            form.add("code", token)
        } else {
            form.add("scope", "service::user.auth.xboxlive.com::MBI_SSL")
            form.add("grant_type", "refresh_token")
            form.add("refresh_token", token)
        }
        val request = Request.Builder()
            .url("https://login.live.com/oauth20_token.srf")
            .header("Content-Type", "application/x-www-form-urlencoded")
            .post(form.build())
            .build()
        val response = HttpUtils.client.newCall(request).execute()

        assert(response.code == 200) { "Http code ${response.code}" }

        val body = JsonParser.parseReader(response.body!!.charStream()).asJsonObject
        if (!body.has("access_token") || !body.has("refresh_token")) {
            if (body.has("error")) {
                throw RuntimeException("error occur whilst refreshing token: " + body.get("error").asString)
            } else {
                throw RuntimeException("error occur whilst refreshing token")
            }
        }
        return body.get("access_token").asString to body.get("refresh_token").asString
    }

    companion object {

        val DEVICE_ANDROID = XboxDeviceInfo("0000000048183522", "Android", false, xalRedirect = "ms-xal-0000000048183522://auth")
        val DEVICE_IOS = XboxDeviceInfo("000000004c17c01a", "iOS", false, xalRedirect = "ms-xal-000000004c17c01a://auth")
        val DEVICE_NINTENDO = XboxDeviceInfo("00000000441cc96b", "Nintendo", true)
        val devices = arrayOf(DEVICE_ANDROID, DEVICE_IOS, DEVICE_NINTENDO).associateBy { it.deviceType }
    }
}

interface IXboxIdentityTokenCache {

    /**
     * identifier for the account which used to cache
     */
    val identifier: String

    fun cache(device: XboxDeviceInfo, token: XboxIdentityToken)

    fun checkCache(device: XboxDeviceInfo): XboxIdentityToken?

}

@Suppress("MemberVisibilityCanBePrivate")
class XboxIdentityTokenCacheFileSystem(val cacheFile: File, override val identifier: String) : IXboxIdentityTokenCache {

    override fun cache(device: XboxDeviceInfo, token: XboxIdentityToken) {
        val json = if (!cacheFile.exists()) {
            null
        } else {
            try {
                JsonParser.parseReader(cacheFile.reader(Charsets.UTF_8)).asJsonObject
            } catch (t: Throwable) {
                println("Load config: $t")
                null
            }
        } ?: JsonObject()

        val identifierJson = if (json.has(identifier)) {
            val identifierElement = json.get(identifier)
            if (identifierElement.isJsonObject) {
                identifierElement.asJsonObject
            } else {
                JsonObject()
            }
        } else {
            JsonObject()
        }

        identifierJson.add(device.deviceType, JsonObject().apply {
            addProperty("token", token.token)
            addProperty("expires", token.notAfter)
        })

        json.add(identifier, identifierJson)

        removeExpired(json)
        cacheFile.writeText(gson.toJson(json), Charsets.UTF_8)
    }

    override fun checkCache(device: XboxDeviceInfo): XboxIdentityToken? {
        if (!cacheFile.exists()) {
            return null
        }

        val json: JsonObject = try {
            JsonParser.parseReader(cacheFile.reader(Charsets.UTF_8)).asJsonObject
        } catch (t: Throwable) {
            println("Load config: $t")
            null
        } ?: return null

        try {
            val identifierJson = if (json.has(identifier)) {
                json.get(identifier).asJsonObject
            } else {
                return null
            }

            val deviceJson = if (identifierJson.has(device.deviceType)) {
                identifierJson.get(device.deviceType).asJsonObject
            } else {
                return null
            }

            if (deviceJson.get("expires").asLong < Instant.now().epochSecond || !deviceJson.has("token")) {
                
                identifierJson.remove(device.deviceType)
                removeExpired(json)
                cacheFile.writeText(gson.toJson(json), Charsets.UTF_8)
                return null
            }

            return XboxIdentityToken(deviceJson.get("token").asString, deviceJson.get("expires").asLong)
        } catch (e: Throwable) {
            println("Check cache: $e")
            return null
        }
    }

    private fun removeExpired(json: JsonObject) {
        val toRemove = mutableListOf<String>()
        val epoch = Instant.now().epochSecond

        json.entrySet().forEach { (_, value) ->
            if (!value.isJsonObject) {
                return@forEach
            }
            val identifierJson = value.asJsonObject
            toRemove.clear()
            identifierJson.entrySet().forEach FE1@ { (key, value) ->
                if (!value.isJsonObject) {
                    return@FE1
                }
                val deviceJson = value.asJsonObject
                if (deviceJson.get("expires").asLong < epoch) {
                    toRemove.add(key)
                }
            }
            toRemove.forEach {
                identifierJson.remove(it)
            }
        }
    }
}
