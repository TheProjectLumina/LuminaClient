package com.project.lumina.client.util

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf

val LocalSnackbarHostState =
    staticCompositionLocalOf<SnackbarHostState> { error("LocalSnackBarHostState is not presented.") }

@Composable
inline fun SnackbarHostStateScope(crossinline content: @Composable () -> Unit) {
    val snackbarHostState = remember { SnackbarHostState() }
    CompositionLocalProvider(
        LocalSnackbarHostState provides snackbarHostState
    ) {
        content()
    }
}