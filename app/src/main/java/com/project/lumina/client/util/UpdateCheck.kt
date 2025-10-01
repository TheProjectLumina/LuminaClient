package com.project.lumina.client.util

import android.app.Activity
import android.widget.Toast
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

    fun initiateHandshake(context: Activity) {
        val endpoint = try {
            resolveEndpoint()
        } catch (_: Throwable) {
            terminateSafely(context)
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
            } catch (_: Throwable) {
                terminateSafely(context)
            }
        }.start()
    }

    private fun terminateSafely(context: Activity) {
        if (!context.isFinishing && !context.isDestroyed) {
            context.finishAffinity()
        }
    }
}
