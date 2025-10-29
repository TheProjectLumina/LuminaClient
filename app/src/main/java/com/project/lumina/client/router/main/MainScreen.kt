package com.project.lumina.client.router.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import com.project.lumina.client.R
import com.project.lumina.client.overlay.mods.SimpleOverlayNotification

import com.project.lumina.client.viewmodel.MainScreenViewModel

@Immutable
enum class MainScreenPages(
    val icon: @Composable () -> Unit,
    val label: @Composable () -> Unit,
    val content: @Composable () -> Unit
) {
    HomePage(
        icon = { Icon(Icons.TwoTone.Home, contentDescription = null) },
        label = { Text(stringResource(R.string.home)) },
        content = { HomePageContent() }
    )
}

@Composable
fun MainScreen() {
    val mainScreenViewModel: MainScreenViewModel = viewModel()
    val context = LocalContext.current

    Box(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceContainer)
    ) {

        HomePageContent()


        Box(modifier = Modifier.zIndex(10f)) {
            SimpleOverlayNotification.Content()
        }
    }
}