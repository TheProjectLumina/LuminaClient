package com.project.lumina.client.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.app.NotificationCompat
import com.project.lumina.client.R
import com.project.lumina.client.constructors.AccountManager
import com.project.lumina.client.constructors.NetBound
import com.project.lumina.client.constructors.GameManager
import com.project.lumina.client.model.CaptureModeModel
import com.project.lumina.client.overlay.manager.OverlayManager

import com.project.lumina.relay.LuminaRelay
import com.project.lumina.relay.LuminaRelaySession
import com.project.lumina.relay.address.LuminaAddress
import com.project.lumina.relay.definition.Definitions
import com.project.lumina.relay.listener.AutoCodecPacketListener
import com.project.lumina.relay.listener.BiomeDefinitionListPacketListener
import com.project.lumina.relay.listener.EncryptedLoginPacketListener
import com.project.lumina.relay.listener.GamingPacketHandler
import com.project.lumina.relay.listener.XboxLoginPacketListener
import com.project.lumina.relay.util.XboxIdentityTokenCacheFileSystem
import com.project.lumina.relay.util.captureLuminaRelay
import android.app.ActivityManager
import com.project.lumina.client.remlink.TerminalViewModel
import com.project.lumina.client.discord.PresenceStateManager
import java.io.File
import kotlin.concurrent.thread

/**
 * Android Foreground Service to handle the CaptureMode functionality
 */
class Services : Service() {

    companion object {
        const val ACTION_CAPTURE_START = "com.project.lumina.relay.capture.start"
        const val ACTION_CAPTURE_STOP = "com.project.lumina.relay.capture.stop"
        private const val NOTIFICATION_CHANNEL_ID = "lumina_capture_channel"
        private const val NOTIFICATION_ID = 1001

        private val handler = Handler(Looper.getMainLooper())
        private var luminaRelay: LuminaRelay? = null
        private var thread: Thread? = null
        var isActive by mutableStateOf(false)
        var RemisOnline by mutableStateOf(false)
        var RemInGame by mutableStateOf(false)
        var isLaunchingMinecraft by mutableStateOf(false)
        var currentServerHostName: String = ""

        fun toggle(context: Context, captureModeModel: CaptureModeModel) {
            if (!isActive) {
                val intent = Intent(ACTION_CAPTURE_START)
                intent.setPackage(context.packageName)
                context.startForegroundService(intent)
                TerminalViewModel.addTerminalLog("Connection", "Services Starting...")
                return
            }

            val intent = Intent(ACTION_CAPTURE_STOP)
            intent.setPackage(context.packageName)
            context.startForegroundService(intent)
        }

        private fun on(context: Context, captureModeModel: CaptureModeModel) {
            if (thread != null) {
                return
            }

            val tokenCacheFile = File(context.cacheDir, "token_cache.json")

            isActive = true
            currentServerHostName = captureModeModel.serverHostName
            PresenceStateManager.setServerInfo(captureModeModel.serverHostName, captureModeModel.serverPort)
            PresenceStateManager.onRelayStarted()

            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val currentActivity = activityManager.appTasks
                .flatMap { it.taskInfo.topActivity?.className?.let { listOf(it) } ?: emptyList() }
                .firstOrNull()
            RemisOnline = currentActivity == "com.project.lumina.client.activity.RemoteLinkActivity"




            val isPortrait = context.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT
            handler.post {
                if (isPortrait) {
                    OverlayManager.dismiss()
                } else {
                    OverlayManager.show(context)
                }
            }

            thread = thread(name = "LuminaRelayThread") {
                runCatching {
                    GameManager.loadConfig()
                }.exceptionOrNull()?.let {
                    it.printStackTrace()
                    context.toast("Load configuration error: ${it.message}")
                }

                runCatching {
                    Definitions.loadBlockPalette()
                }.exceptionOrNull()?.let {
                    it.printStackTrace()
                    context.toast("Load block palette error: ${it.message}")
                }

                val sessionEncryptor = if (AccountManager.currentAccount == null) {
                    EncryptedLoginPacketListener()
                } else {
                    AccountManager.currentAccount?.let { account ->
                        Log.e("LuminaRelay", "Logged in as ${account.remark}")
                        TerminalViewModel.addTerminalLog("Connection", "Logged in as ${account.remark}")
                        TerminalViewModel.addTerminalLog("Help", "Type '!help' in chat to use modules.")
                        XboxLoginPacketListener({ account.refresh() }, account.platform).also {
                            it.tokenCache =
                                XboxIdentityTokenCacheFileSystem(tokenCacheFile, account.remark)
                        }
                    }
                }

                runCatching {
                    luminaRelay = captureLuminaRelay(
                        remoteAddress = LuminaAddress(
                            captureModeModel.serverHostName,
                            captureModeModel.serverPort
                        )
                    ) {
                        initModules(this)

                        listeners.add(AutoCodecPacketListener(this))
                        listeners.add(BiomeDefinitionListPacketListener(this))
                        sessionEncryptor?.let {
                            it.luminaRelaySession = this
                            listeners.add(it)
                        }
                        listeners.add(GamingPacketHandler(this))
                    }
                }.exceptionOrNull()?.let {
                    it.printStackTrace()
                    context.toast("Start LuminaRelay error: ${it.stackTraceToString()}")
                }
            }
        }

        private fun off() {
            thread(name = "LuminaRelayThread") {
                GameManager.saveConfig()
                isActive = false
                RemisOnline = false
                currentServerHostName = ""
                PresenceStateManager.onRelayDisconnected()
                luminaRelay?.disconnect()
                thread?.interrupt()
                thread = null

                isLaunchingMinecraft = false

                handler.post {
                    OverlayManager.dismiss()
                }
                TerminalViewModel.addTerminalLog("Connection", "Services Stopped.")
            }
        }

        private fun Context.toast(message: String) {
            handler.post {
                Toast.makeText(this, message, Toast.LENGTH_LONG).show()
            }
        }

        private fun initModules(luminaRelaySession: LuminaRelaySession) {
            try {
                val session = NetBound(luminaRelaySession)
                luminaRelaySession.listeners.add(session)

                for (module in GameManager.elements) {
                    try {
                        module.session = session
                    } catch (e: Exception) {
                        Log.e("Services", "Failed to initialize session for module ${module.name}: ${e.message}")
                    }
                }

                TerminalViewModel.addTerminalLog("Connection", "Initializing Modules...")
            } catch (e: Exception) {
                Log.e("Services", "Failed to initialize modules: ${e.message}")
                TerminalViewModel.addTerminalLog("Error", "Failed to initialize modules: ${e.message}")
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_CAPTURE_START -> {
                startForeground(NOTIFICATION_ID, createNotification("Lumina capture service is running"))
                val captureModeModel = CaptureModeModel.from(
                    getSharedPreferences("game_settings", Context.MODE_PRIVATE)
                )
                on(applicationContext, captureModeModel)
            }
            ACTION_CAPTURE_STOP -> {
                off()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }

        return START_NOT_STICKY
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (!isActive) return

        val isPortrait = newConfig.orientation == Configuration.ORIENTATION_PORTRAIT
        handler.post {
            if (isPortrait) {
                OverlayManager.dismiss()
            } else {
                OverlayManager.show(this)
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Lumina Capture Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Used while Lumina capture mode is active"
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
        TerminalViewModel.addTerminalLog("Connection", "Notification Initialized.")
    }

    private fun createNotification(text: String) = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
        .setContentTitle("Lumina Capture")
        .setContentText(text)
        .setSmallIcon(R.drawable.img)
        .setPriority(NotificationCompat.PRIORITY_LOW)
        .addAction(
            android.R.drawable.ic_menu_close_clear_cancel,
            "Stop",
            createPendingIntent(ACTION_CAPTURE_STOP)
        )
        .build()

    private fun createPendingIntent(action: String): PendingIntent {
        val intent = Intent(action)
        intent.setPackage(packageName)

        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        return PendingIntent.getService(
            this,
            0,
            intent,
            flags
        )
    }
}