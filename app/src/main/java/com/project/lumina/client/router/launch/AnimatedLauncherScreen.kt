package com.project.lumina.client.router.launch

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.window.layout.WindowMetricsCalculator
import androidx.compose.ui.platform.LocalContext
import android.content.Context
import android.content.Intent
import androidx.activity.ComponentActivity
import com.project.lumina.client.activity.MainActivity
import com.project.lumina.client.activity.MinecraftCheckActivity
import com.project.lumina.client.activity.RemoteLinkActivity
import com.project.lumina.client.util.FirstTimeUserManager
import kotlinx.coroutines.delay

@Composable
fun AnimatedLauncherScreen() {
    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("SettingsPrefs", Context.MODE_PRIVATE)
    val disableOpeningAnimation = sharedPreferences.getBoolean("disableOpeningAnimation", false)

    var isPreloading by remember { mutableStateOf(!disableOpeningAnimation) }
    var showContent by remember { mutableStateOf(disableOpeningAnimation) }
    var showFirstTimeDialog by remember { mutableStateOf(false) }

    val windowMetrics = remember {
        WindowMetricsCalculator.getOrCreate().computeCurrentWindowMetrics(context)
    }
    val screenWidth = with(LocalDensity.current) { windowMetrics.bounds.width().toDp() }
    val screenHeight = with(LocalDensity.current) { windowMetrics.bounds.height().toDp() }
    val isLandscape = screenWidth > screenHeight

    LaunchedEffect(disableOpeningAnimation) {
        if (!disableOpeningAnimation) {
            delay(3000)
            isPreloading = false
            delay(500)
            showContent = true

            if (FirstTimeUserManager.shouldShowDialog(context)) {
                delay(800)
                showFirstTimeDialog = true
            } else if (FirstTimeUserManager.hasAutoLaunchMode(context)) {
                delay(1000)
                val autoMode = FirstTimeUserManager.getAutoLaunchMode(context)
                when (autoMode) {
                    "mobile" -> {
                        val intent = Intent(context, MinecraftCheckActivity::class.java)
                        context.startActivity(intent)
                        (context as? ComponentActivity)?.overridePendingTransition(
                            android.R.anim.fade_in,
                            android.R.anim.fade_out
                        )
                        (context as? ComponentActivity)?.finish()
                    }
                    "remote" -> {
                        val intent = Intent(context, RemoteLinkActivity::class.java)
                        context.startActivity(intent)
                        (context as? ComponentActivity)?.overridePendingTransition(
                            android.R.anim.fade_in,
                            android.R.anim.fade_out
                        )
                        (context as? ComponentActivity)?.finish()
                    }
                }
            }
        } else {
            if (FirstTimeUserManager.shouldShowDialog(context)) {
                delay(300)
                showFirstTimeDialog = true
            } else if (FirstTimeUserManager.hasAutoLaunchMode(context)) {
                delay(500)
                val autoMode = FirstTimeUserManager.getAutoLaunchMode(context)
                when (autoMode) {
                    "mobile" -> {
                        val intent = Intent(context, MinecraftCheckActivity::class.java)
                        context.startActivity(intent)
                        (context as? ComponentActivity)?.finish()
                    }
                    "remote" -> {
                        val intent = Intent(context, RemoteLinkActivity::class.java)
                        context.startActivity(intent)
                        (context as? ComponentActivity)?.finish()
                    }
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedBackground(isPreloading)
        AnimatedVisibility(
            visible = isPreloading,
            enter = fadeIn(),
            exit = fadeOut(animationSpec = tween(500)),
            modifier = Modifier.fillMaxSize()
        ) {
            PreloaderAnimation()
        }
        AnimatedVisibility(
            visible = showContent,
            enter = fadeIn(animationSpec = tween(800)),
            modifier = Modifier.fillMaxSize()
        ) {
            if (isLandscape) {
                LandscapeLauncherContent()
            } else {
                PortraitLauncherContent()
            }
        }
        Text(
            text = "© Project Lumina 2026",
            color = Color.White.copy(alpha = 0.5f),
            fontSize = 10.sp,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(8.dp)
                .alpha(if (isPreloading) 0.3f else 0.7f)
        )

        FirstTimeUserDialog(
            isVisible = showFirstTimeDialog,
            onModeSelected = { mode ->
                FirstTimeUserManager.setAutoLaunchMode(context, mode)
                showFirstTimeDialog = false

                when (mode) {
                    "mobile" -> {
                        val intent = Intent(context, MinecraftCheckActivity::class.java)
                        context.startActivity(intent)
                        (context as? ComponentActivity)?.overridePendingTransition(
                            android.R.anim.fade_in,
                            android.R.anim.fade_out
                        )
                        (context as? ComponentActivity)?.finish()
                    }
                    "remote" -> {
                        val intent = Intent(context, RemoteLinkActivity::class.java)
                        context.startActivity(intent)
                        (context as? ComponentActivity)?.overridePendingTransition(
                            android.R.anim.fade_in,
                            android.R.anim.fade_out
                        )
                        (context as? ComponentActivity)?.finish()
                    }
                }
            },
            onDismiss = {
                FirstTimeUserManager.setDialogShown(context)
                showFirstTimeDialog = false
            }
        )
    }
}