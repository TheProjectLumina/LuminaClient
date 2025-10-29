package com.project.lumina.client.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalOverscrollConfiguration
import androidx.compose.runtime.CompositionLocalProvider
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.amplitude.android.Amplitude
import com.amplitude.android.Configuration
import com.amplitude.android.DefaultTrackingOptions
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.project.lumina.client.router.launch.AnimatedLauncherScreen
import com.project.lumina.client.ui.theme.LuminaClientTheme
import com.project.lumina.client.util.HashCat
import com.project.lumina.client.util.TrackUtil
import com.project.lumina.client.util.UpdateCheck
import kotlinx.coroutines.delay

class LaunchActivity : ComponentActivity() {

    @OptIn(ExperimentalFoundationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val amplitude = Amplitude(
            Configuration(
                apiKey = TrackUtil.TRACK_API,
                context = applicationContext,
                defaultTracking = DefaultTrackingOptions.ALL,
            )
        )
        amplitude.track("Launch Activity Init")

        val updateCheck = UpdateCheck()
        updateCheck.initiateHandshake(this)

        val verifier = HashCat.getInstance()
        val isValid = verifier.LintHashInit(this)
        if (isValid) {
            FirebaseCrashlytics.getInstance().log("App started")
        }

        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        setContent {
            LuminaClientTheme {
                CompositionLocalProvider(LocalOverscrollConfiguration provides null) {
                    AnimatedLauncherScreen()
                }
            }
        }


    }
}



suspend fun startActivityWithTransition(context: android.content.Context, destinationClass: Class<*>) {
    delay(800)
    val intent = Intent(context, destinationClass)
    context.startActivity(intent)
    (context as? ComponentActivity)?.overridePendingTransition(
        android.R.anim.fade_in,
        android.R.anim.fade_out
    )
    (context as? ComponentActivity)?.finish()
}

