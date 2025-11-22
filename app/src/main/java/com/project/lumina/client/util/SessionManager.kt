package com.project.lumina.client.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Base64
import androidx.browser.customtabs.CustomTabsIntent
import java.io.File
import java.net.URLEncoder
import java.security.MessageDigest
import kotlin.random.Random

class SessionManager(private val context: Context) {

    companion object {
        private const val SESSION_FILE = "session_data"
        private const val SESSION_DURATION_HOURS = 4
        private const val SESSION_DURATION_MS = SESSION_DURATION_HOURS * 60 * 60 * 1000L
        private const val LINKVERTISE_USER_ID = "1444843"
        private const val YOUR_DOMAIN = API.LVAUTH
        private const val SECRET_SALT = "pFzBVr9YzofdxjDrJO1xdW=qeEF2VVIq"
    }

    fun checkSession(activity: Activity): Boolean {
        if (hasValidSession()) {
            return true
        }

        startAuthFlow(activity)
        return false
    }

    fun validateAndSaveSession(key: String, req: String): Boolean {
        val expectedKey = generateKey(req)

        if (key == expectedKey) {
            saveSession()
            return true
        }

        return false
    }

    private fun generateKey(req: String): String {
        val input = "$req$SECRET_SALT"
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return Base64.encodeToString(bytes, Base64.NO_WRAP or Base64.URL_SAFE)
            .substring(0, 32) // Take first 32 characters
    }

    private fun generateRandomReq(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        return (1..16)
            .map { chars.random() }
            .joinToString("")
    }

    private fun generateLinkvertiseUrl(userId: String, targetLink: String): String {
        val randomNumber = Random.nextInt(0, 1000)
        val baseUrl = "https://link-to.net/$userId/$randomNumber/dynamic"
        val base64Encoded = Base64.encodeToString(
            targetLink.toByteArray(),
            Base64.NO_WRAP
        )

        return "$baseUrl?r=$base64Encoded"
    }

    private fun hasValidSession(): Boolean {
        val sessionFile = File(context.filesDir, SESSION_FILE)

        if (!sessionFile.exists()) {
            return false
        }

        return try {
            val encodedData = sessionFile.readText()
            val decodedBytes = Base64.decode(encodedData, Base64.DEFAULT)
            val timestamp = String(decodedBytes).toLong()

            val currentTime = System.currentTimeMillis()
            val elapsed = currentTime - timestamp

            elapsed < SESSION_DURATION_MS
        } catch (e: Exception) {
            false
        }
    }

    fun saveSession() {
        val sessionFile = File(context.filesDir, SESSION_FILE)
        val timestamp = System.currentTimeMillis().toString()

        val encodedData = Base64.encodeToString(
            timestamp.toByteArray(),
            Base64.NO_WRAP
        )

        sessionFile.writeText(encodedData)
    }

    private fun startAuthFlow(activity: Activity) {
        // Generate random request code
        val reqCode = generateRandomReq()

        // Store req code for later validation
        storeReqCode(reqCode)

        // Create your domain URL with req parameter
        val yourDomainUrl = "$YOUR_DOMAIN?req=$reqCode"

        // Generate Linkvertise URL pointing to your domain
        val linkvertiseUrl = generateLinkvertiseUrl(
            userId = LINKVERTISE_USER_ID,
            targetLink = yourDomainUrl
        )

        // Launch Custom Tab
        val customTabsIntent = CustomTabsIntent.Builder()
            .setShowTitle(true)
            .build()

        customTabsIntent.launchUrl(activity, Uri.parse(linkvertiseUrl))
        activity.finish()
    }

    private fun storeReqCode(reqCode: String) {
        val reqFile = File(context.filesDir, "req_code")
        reqFile.writeText(reqCode)
    }

    fun getStoredReqCode(): String? {
        val reqFile = File(context.filesDir, "req_code")
        return if (reqFile.exists()) {
            reqFile.readText()
        } else {
            null
        }
    }

    fun clearSession() {
        val sessionFile = File(context.filesDir, SESSION_FILE)
        if (sessionFile.exists()) {
            sessionFile.delete()
        }

        val reqFile = File(context.filesDir, "req_code")
        if (reqFile.exists()) {
            reqFile.delete()
        }
    }

    fun getRemainingSessionTime(): Long {
        val sessionFile = File(context.filesDir, SESSION_FILE)

        if (!sessionFile.exists()) {
            return 0L
        }

        return try {
            val encodedData = sessionFile.readText()
            val decodedBytes = Base64.decode(encodedData, Base64.DEFAULT)
            val timestamp = String(decodedBytes).toLong()

            val currentTime = System.currentTimeMillis()
            val elapsed = currentTime - timestamp
            val remaining = SESSION_DURATION_MS - elapsed

            if (remaining > 0) remaining else 0L
        } catch (e: Exception) {
            0L
        }
    }
}