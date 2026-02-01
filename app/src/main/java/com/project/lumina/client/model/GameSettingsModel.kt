package com.project.lumina.client.model

import android.content.SharedPreferences

data class GameSettingsModel(
    val captureModeModel: CaptureModeModel,
    val selectedGame: String,
) {

    companion object {

        fun from(sharedPreferences: SharedPreferences): GameSettingsModel {
            val captureModeModel = CaptureModeModel.from(sharedPreferences)
            val selectedGame = fetchSelectedGame(sharedPreferences)
            return GameSettingsModel(captureModeModel, selectedGame)
        }

        private fun fetchSelectedGame(sharedPreferences: SharedPreferences): String {
            return sharedPreferences.getString("selected_game", "")!!
        }

    }

}