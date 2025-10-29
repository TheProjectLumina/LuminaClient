package com.project.lumina.client.viewmodel

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import androidx.compose.ui.util.fastFilter
import androidx.core.content.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.lumina.client.application.AppContext
import com.project.lumina.client.model.CaptureModeModel
import com.project.lumina.client.router.main.MainScreenPages
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainScreenViewModel : ViewModel() {

    enum class PackageInfoState {
        Loading, Done
    }

    private val gameSettingsSharedPreferences by lazy {
        AppContext.instance.getSharedPreferences("game_settings", Context.MODE_PRIVATE)
    }

    private val _selectedPage = MutableStateFlow(MainScreenPages.HomePage)

    private val _captureModeModel = MutableStateFlow(initialCaptureModeModel())

    private val _packageInfos = MutableStateFlow<List<PackageInfo>>(emptyList())

    private val _packageInfoState = MutableStateFlow(PackageInfoState.Loading)

    private val _selectedGame = MutableStateFlow(initialSelectedGame())

    val selectedPage = _selectedPage.asStateFlow()

    val captureModeModel = _captureModeModel.asStateFlow()

    val packageInfos = _packageInfos.asStateFlow()

    val packageInfoState = _packageInfoState.asStateFlow()

    val selectedGame = _selectedGame.asStateFlow()

    fun selectPage(page: MainScreenPages) {
        _selectedPage.value = page
    }

    fun selectCaptureModeModel(captureModeModel: CaptureModeModel) {
        _captureModeModel.value = captureModeModel
        captureModeModel.to(gameSettingsSharedPreferences)
    }

    fun selectGame(packageName: String?) {
        _selectedGame.value = packageName
        gameSettingsSharedPreferences.edit {
            putString("selected_game", packageName)
        }
    }

    fun fetchPackageInfos() {
        viewModelScope.launch(Dispatchers.IO) {
            _packageInfoState.value = PackageInfoState.Loading
            try {
                val packageManager = AppContext.instance.packageManager
                val packageInfos = packageManager.getInstalledPackages(0)
                    .fastFilter {
                        it.applicationInfo!!.flags and ApplicationInfo.FLAG_SYSTEM == 0
                    }
                    .fastFilter {
                        it.applicationInfo != null
                    }
                _packageInfos.value = packageInfos
            } finally {
                _packageInfoState.value = PackageInfoState.Done
            }
        }
    }

    private fun initialCaptureModeModel(): CaptureModeModel {
        return CaptureModeModel.from(gameSettingsSharedPreferences)
    }

    private fun initialSelectedGame(): String? {
        return gameSettingsSharedPreferences.getString("selected_game", "com.mojang.minecraftpe")
    }

}