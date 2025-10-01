package com.project.lumina.client.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalOverscrollConfiguration
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.CompositionLocalProvider
import com.project.lumina.client.remlink.RemoteLink
import com.project.lumina.client.ui.theme.LuminaClientTheme
import android.content.Intent
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import com.amplitude.android.Amplitude
import com.amplitude.android.Configuration
import com.amplitude.android.DefaultTrackingOptions
import com.project.lumina.client.constructors.GameManager
import com.project.lumina.client.game.module.api.config.ConfigManagerElement

import com.project.lumina.client.util.HashCat
import com.project.lumina.client.util.TrackUtil
import com.project.lumina.client.util.UpdateCheck


class RemoteLinkActivity : ComponentActivity() {

    companion object {

        private var currentInstance: RemoteLinkActivity? = null

        fun launchConfigImport() {
            currentInstance?.let { activity ->
                try {
                    val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                        addCategory(Intent.CATEGORY_OPENABLE)
                        type = "*/*"
                    }

                    activity.importConfigLauncher.launch(intent)
                } catch (e: Exception) {
                    Log.e("RemoteLinkActivity", "Error launching import: ${e.message}", e)
                }
            }
        }
    }


    val importConfigLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.data?.let { uri ->

                (GameManager.elements.find { it.name == "config_manager" } as? ConfigManagerElement)?.let { configManager ->
                    configManager.importConfig(uri)
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val verifier = HashCat.getInstance()
        val isValid = verifier.LintHashInit(this)
        if (isValid) { }
        currentInstance = this

        
      val amplitude = Amplitude(
        Configuration(
        apiKey = TrackUtil.TRACK_API,
        context = applicationContext,
        defaultTracking = DefaultTrackingOptions.ALL,
        )
        )

        amplitude.track("Remote Link Opened")
        enableEdgeToEdge()
        val updateCheck = UpdateCheck()
        updateCheck.initiateHandshake(this)
        setContent {
            LuminaClientTheme {
                CompositionLocalProvider(LocalOverscrollConfiguration provides null) {
                    RemoteLink()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        if (currentInstance == this) {
            currentInstance = null
        }
    }
}