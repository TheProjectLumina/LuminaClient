package com.project.lumina.client.util

import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.project.lumina.client.activity.CrashHandlerActivity
import okhttp3.OkHttpClient
import okhttp3.Request

class UpdateCheck {
    private val connector = OkHttpClient()
    private val fallbackNotice = retrieveFallback()

    companion object {
        init {
            try {
                System.loadLibrary("pixie")
            } catch (_: UnsatisfiedLinkError) {}
        }
    }

    private external fun resolveEndpoint(): String
    private external fun retrieveFallback(): String
    private external fun verifySignature(payload: String): Boolean

    fun initiateHandshake(context: Activity, allowOffline: Boolean = false) {
        if (context is CrashHandlerActivity) {
            return
        }

        val endpoint = try {
            resolveEndpoint()
        } catch (_: Throwable) {
            handleConnectionError(context)
            return
        }

        Thread {
            try {
                val response = connector.newCall(Request.Builder().url(endpoint).build()).execute()
                val payload = response.body?.string()
                if (payload == null || !verifySignature(payload)) {
                    if (!context.isFinishing && !context.isDestroyed) {
                        context.runOnUiThread {
                            Toast.makeText(context, fallbackNotice, Toast.LENGTH_LONG).show()
                        }
                    }
                    terminateSafely(context)
                }
            } catch (e: Throwable) {
                handleConnectionError(context)
            }
        }.start()
    }

    private fun handleConnectionError(context: Activity) {
        if (context is CrashHandlerActivity) {
            return
        }

        if (!context.isFinishing && !context.isDestroyed) {
            context.runOnUiThread {
                Toast.makeText(
                    context,
                    "No internet connection detected.",
                    Toast.LENGTH_LONG
                ).show()
            }

            Handler(Looper.getMainLooper()).postDelayed({
                terminateSafely(context)
            }, 500)
        }
    }

    private fun terminateSafely(context: Activity) {
        if (!context.isFinishing && !context.isDestroyed) {
            context.finishAffinity()
        }
    }
}
