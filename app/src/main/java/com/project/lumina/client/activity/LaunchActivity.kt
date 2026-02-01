package com.project.lumina.client.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
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
import com.project.lumina.client.util.SessionManager
import com.project.lumina.client.util.TrackUtil
import com.project.lumina.client.util.UpdateCheck
import kotlinx.coroutines.delay

class LaunchActivity : ComponentActivity() {

    private lateinit var sessionManager: SessionManager
    private var isAuthHandled = false

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

        sessionManager = SessionManager(applicationContext)

        handleDeepLink(intent)

        if (!isAuthHandled) {
            val hasValidSession = sessionManager.checkSession(this)

            if (hasValidSession) {
                initializeApp(amplitude)
            }

        } else {
            initializeApp(amplitude)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleDeepLink(intent)
    }

    private fun handleDeepLink(intent: Intent) {
        val data = intent.data

        if (data != null && data.scheme == "lumina" && data.host == "auth") {
            val key = data.getQueryParameter("key")
            val req = data.getQueryParameter("req")

            if (key != null && req != null) {

                if (sessionManager.validateAndSaveSession(key, req)) {
                    Toast.makeText(
                        this,
                        "Authentication successful!",
                        Toast.LENGTH_SHORT
                    ).show()
                    isAuthHandled = true


                    val amplitude = Amplitude(
                        Configuration(
                            apiKey = TrackUtil.TRACK_API,
                            context = applicationContext,
                            defaultTracking = DefaultTrackingOptions.ALL,
                        )
                    )
                    amplitude.track("Auth Success")

                } else {
                    Toast.makeText(
                        this,
                        "Authentication failed. Please try again.",
                        Toast.LENGTH_SHORT
                    ).show()

                    sessionManager.checkSession(this)
                    return
                }
            }
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    private fun initializeApp(amplitude: Amplitude) {
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

        val remainingTime = sessionManager.getRemainingSessionTime()
        val hours = remainingTime / (60 * 60 * 1000)
        val minutes = (remainingTime % (60 * 60 * 1000)) / (60 * 1000)

        Toast.makeText(
            this,
            "Session valid for ${hours}h ${minutes}m",
            Toast.LENGTH_LONG
        ).show()
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