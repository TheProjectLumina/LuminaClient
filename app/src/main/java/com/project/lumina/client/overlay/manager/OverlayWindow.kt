package com.project.lumina.client.overlay.manager

import android.app.Activity
import android.app.Service
import android.content.Context
import android.graphics.PixelFormat
import android.hardware.input.InputManager
import android.os.Build
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.view.WindowManager.LayoutParams
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Recomposer
import androidx.compose.ui.platform.AndroidUiDispatcher
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.ViewModelStore
import kotlinx.coroutines.CoroutineScope

@Suppress("MemberVisibilityCanBePrivate")
abstract class OverlayWindow {

    var minimapZoom: Float = 1.0f
    var minimapDotSize: Int = 5

    open val layoutParams by lazy {
        LayoutParams().apply {
            width = LayoutParams.WRAP_CONTENT
            height = LayoutParams.WRAP_CONTENT
            gravity = Gravity.START or Gravity.TOP
            x = 0
            y = 0
            type = LayoutParams.TYPE_APPLICATION_OVERLAY
            flags = LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH or
                    LayoutParams.FLAG_HARDWARE_ACCELERATED or
                    LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                    LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS or
                    LayoutParams.FLAG_NOT_FOCUSABLE
            format = PixelFormat.TRANSLUCENT
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                alpha =
                    (OverlayManager.currentContext!!.getSystemService(Service.INPUT_SERVICE) as? InputManager)?.maximumObscuringOpacityForTouch
                        ?: 1f
            }
        }
    }

    open val composeView by lazy {
        ComposeView(OverlayManager.currentContext!!).apply {
            systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        }
    }

    val windowManager: WindowManager
        get() = OverlayManager.currentContext!!.getSystemService(Context.WINDOW_SERVICE) as WindowManager

    val lifecycleOwner = OverlayLifecycleOwner()

    val viewModelStore = ViewModelStore()

    val composeScope: CoroutineScope

    val recomposer: Recomposer

    var firstRun = true

    /**
     * Get the activity context if available, or null if in overlay mode
     */
    fun getActivityContext(): Activity? {
        return OverlayManager.currentContext as? Activity
    }

    /**
     * Show a toast message safely from overlay context
     */
    fun showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
        try {
            Toast.makeText(OverlayManager.currentContext, message, duration).show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    init {
        lifecycleOwner.performRestore(null)

        val coroutineContext = AndroidUiDispatcher.CurrentThread
        composeScope = CoroutineScope(coroutineContext)
        recomposer = Recomposer(coroutineContext)
    }

    @Composable
    abstract fun Content()

}