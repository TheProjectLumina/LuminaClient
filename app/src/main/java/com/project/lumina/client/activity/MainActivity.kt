/*
 * © Project Lumina 2025 — Licensed under GNU GPLv3
 * You are free to use, modify, and redistribute this code under the terms
 * of the GNU General Public License v3. See the LICENSE file for details.
 *
 * ─────────────────────────────────────────────────────────────────────────────
 * This is open source — not open credit.
 *
 * If you're here to build, welcome. If you're here to repaint and reupload
 * with your tag slapped on it… you're not fooling anyone.
 *
 * Changing colors and class names doesn't make you a developer.
 * Copy-pasting isn't contribution.
 *
 * You have legal permission to fork. But ask yourself — are you improving,
 * or are you just recycling someone else's work to feed your ego?
 *
 * Open source isn't about low-effort clones or chasing clout.
 * It's about making things better. Sharper. Cleaner. Smarter.
 *
 * So go ahead, fork it — but bring something new to the table,
 * or don't bother pretending.
 *
 * This message is philosophical. It does not override your legal rights under GPLv3.
 * ─────────────────────────────────────────────────────────────────────────────
 *
 * GPLv3 Summary:
 * - You have the freedom to run, study, share, and modify this software.
 * - If you distribute modified versions, you must also share the source code.
 * - You must keep this license and copyright intact.
 * - You cannot apply further restrictions — the freedom stays with everyone.
 * - This license is irrevocable, and applies to all future redistributions.
 *
 * Full text: https://www.gnu.org/licenses/gpl-3.0.html
 */


package com.project.lumina.client.activity


import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalOverscrollConfiguration
import androidx.compose.runtime.CompositionLocalProvider
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import com.project.lumina.client.constructors.ArrayListManager
import com.project.lumina.client.constructors.GameManager
import com.project.lumina.client.game.module.api.config.ConfigManagerElement
import com.project.lumina.client.navigation.Navigation
import com.project.lumina.client.ui.theme.LuminaClientTheme
import com.project.lumina.client.util.HashCat
import io.netty.util.internal.logging.InternalLoggerFactory
import io.netty.util.internal.logging.JdkLoggerFactory
import com.project.lumina.client.constructors.AccountManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.amplitude.android.Amplitude
import com.amplitude.android.Configuration
import com.amplitude.android.DefaultTrackingOptions
import com.project.lumina.client.devtools.LogcatService
import com.project.lumina.client.util.SessionManager
import com.project.lumina.client.util.TrackUtil


class MainActivity : ComponentActivity() {
    companion object {
        private var currentInstance: MainActivity? = null
        
        fun launchConfigImport() {
            if (currentInstance == null) {
                Log.e("MainActivity", "Error: MainActivity instance is null when trying to import config")
                return
            }
            
            currentInstance?.let { activity ->
                try {
                    Log.d("MainActivity", "Launching config import from activity: ${activity.javaClass.simpleName}")
                    val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                        addCategory(Intent.CATEGORY_OPENABLE)
                        type = "*/*"
                    }
                    
                    activity.importConfigLauncher.launch(intent)
                    Log.d("MainActivity", "Import launcher launched successfully")
                } catch (e: Exception) {
                    Log.e("MainActivity", "Error launching import: ${e.message}", e)
                }
            }
        }
    }
    
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (allGranted) {
            Log.d("MainActivity", "Storage permissions granted")
        } else {
            Log.w("MainActivity", "Storage permissions not granted")
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
    
    private fun requestStoragePermissions() {
        val permissionsToRequest = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        
        val permissionsNotGranted = permissionsToRequest.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }.toTypedArray()
        
        if (permissionsNotGranted.isNotEmpty()) {
            requestPermissionLauncher.launch(permissionsNotGranted)
        }
        
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                try {
                    val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                    val uri = Uri.fromParts("package", packageName, null)
                    intent.data = uri
                    startActivity(intent)
                } catch (e: Exception) {
                    Log.e("MainActivity", "Failed to request MANAGE_EXTERNAL_STORAGE permission", e)
                }
            }
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @SuppressLint("BatteryLife")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ArrayListManager.initializeSounds(this)



        currentInstance = this
        //Muffin.Start(this)
//

        val amplitude = Amplitude(
            Configuration(
                apiKey = TrackUtil.TRACK_API,
                context = applicationContext,
                defaultTracking = DefaultTrackingOptions.ALL,
            )
        )
        amplitude.track("Lumina Mobile")
        val sessionManager = SessionManager(applicationContext)
        sessionManager.checkSession(this)
        InternalLoggerFactory.setDefaultFactory(JdkLoggerFactory.INSTANCE)
        Log.i("MainApplication", "Forced Netty to use JUL logger instead of Log4j2.")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
            val packageName = packageName
            if (!powerManager.isIgnoringBatteryOptimizations(packageName)) {
                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                    data = Uri.parse("package:$packageName")
                }
                startActivity(intent)
            }
        }
      
        enableEdgeToEdge()
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        val verifier = HashCat.getInstance()
        val isValid = verifier.LintHashInit(this)
        if (isValid) {


            WindowCompat.setDecorFitsSystemWindows(window, false)
            WindowInsetsControllerCompat(window, window.decorView).let { controller ->
                controller.hide(WindowInsetsCompat.Type.systemBars())
                controller.systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        }
        
        requestStoragePermissions()

        // Initialize LogcatService if enabled in DevTools
        LogcatService.checkAndStartIfEnabled()

        setContent {
            LuminaClientTheme {
                CompositionLocalProvider(LocalOverscrollConfiguration provides null) {
                    
                Navigation()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        ArrayListManager.releaseSounds()
        LogcatService.stopLogcatService()
        if (currentInstance == this) {
            currentInstance = null
        }
    }




}