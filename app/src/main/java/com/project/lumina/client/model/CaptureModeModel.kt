package com.project.lumina.client.model

import android.content.SharedPreferences
import androidx.core.content.edit

data class CaptureModeModel(
    val serverHostName: String,
    val serverPort: Int
) {

    companion object {

        fun from(sharedPreferences: SharedPreferences): CaptureModeModel {
            val serverHostName = sharedPreferences.getString(
                "capture_mode_model_server_host_name",
                "play.lbsg.net"
            )!!
            val serverPort = sharedPreferences.getInt(
                "capture_mode_model_server_port",
                19132
            )
            return CaptureModeModel(serverHostName, serverPort)
        }

    }

    fun to(sharedPreferences: SharedPreferences) {
        sharedPreferences.edit {
            putString(
                "capture_mode_model_server_host_name",
                serverHostName
            )
            putInt(
                "capture_mode_model_server_port",
                serverPort
            )
        }
    }

}