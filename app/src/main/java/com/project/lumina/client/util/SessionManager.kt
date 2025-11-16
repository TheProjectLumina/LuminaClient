package com.project.lumina.client.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Base64
import com.project.lumina.client.activity.LVAuthWebViewActivity
import java.io.File
import java.net.URLEncoder
import kotlin.random.Random

class SessionManager(private val context: Context) {

    companion object {
        private const val SESSION_FILE = "session_data"
        private const val SESSION_DURATION_HOURS = 4
        private const val SESSION_DURATION_MS = SESSION_DURATION_HOURS * 60 * 60 * 1000L
        private const val LINKVERTISE_USER_ID = "1444843"
        private const val TARGET_AUTH_URI = "https://projectlumina.online/?game=auth"
    }

    fun checkSession(activity: Activity): Boolean {
        if (hasValidSession()) {
            return true
        }

        startAuthFlow(activity)
        return false
    }


    private fun generateLinkvertiseUrl(userId: String, targetLink: String): String {
        val randomNumber = Random.nextInt(0, 1000)
        val baseUrl = "https://link-to.net/$userId/$randomNumber/dynamic"

        val encodedLink = URLEncoder.encode(targetLink, "UTF-8")
            .replace("%3A", ":")
            .replace("%2F", "/")

        val base64Encoded = Base64.encodeToString(
            encodedLink.toByteArray(),
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
        val linkvertiseUrl = generateLinkvertiseUrl(
            userId = LINKVERTISE_USER_ID,
            targetLink = TARGET_AUTH_URI
        )

        val intent = Intent(activity, LVAuthWebViewActivity::class.java).apply {
            putExtra(LVAuthWebViewActivity.EXTRA_AUTH_URL, linkvertiseUrl)
        }

        activity.startActivity(intent)
        activity.finish()
    }


    fun clearSession() {
        val sessionFile = File(context.filesDir, SESSION_FILE)
        if (sessionFile.exists()) {
            sessionFile.delete()
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