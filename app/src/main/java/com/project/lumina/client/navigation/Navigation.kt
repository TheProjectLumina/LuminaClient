package com.project.lumina.client.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.project.lumina.client.router.main.MainScreen

@Composable
fun Navigation() {
    val navController = rememberNavController()
    CompositionLocalProvider(LocalNavHostController provides navController) {
        NavHost(
            navController = navController,
            startDestination = Destinations.MainScreen.name
        ) {
            composable(Destinations.MainScreen.name) {
                MainScreen()
            }
        }
    }
}

val LocalNavHostController =
    staticCompositionLocalOf<NavHostController> { error("LocalNavHostController is not presented") }

enum class Destinations {
    MainScreen
}