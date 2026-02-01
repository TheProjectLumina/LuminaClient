package com.project.lumina.client.overlay.manager

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PixelFormat
import android.util.Log
import android.os.Build
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalOverscrollConfiguration
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.compositionContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.project.lumina.client.application.AppContext
import com.project.lumina.client.constructors.GameManager
import com.project.lumina.client.overlay.mods.DummyOverlay
import com.project.lumina.client.service.Services
import com.project.lumina.client.ui.theme.LuminaClientTheme

import kotlinx.coroutines.launch

@SuppressLint("StaticFieldLeak")
@Suppress("MemberVisibilityCanBePrivate")
object OverlayManager {

    private val overlayWindows = ArrayList<OverlayWindow>()

    var currentContext: Context? = null
        private set

    var isShowing = false
        private set

    init {


        with(overlayWindows) {

            if (!Services.RemisOnline) {
                add(OverlayButton())
                addAll(
                    GameManager
                        .elements
                        .filter { it.isShortcutDisplayed }
                        .map { it.overlayShortcutButton })
            }
            else add(DummyOverlay())
        }

    }

    fun showOverlayWindow(overlayWindow: OverlayWindow) {
        overlayWindows.add(overlayWindow)

        val context = currentContext
        if (isShowing && context != null) {
            showOverlayWindow(context, overlayWindow)
        }
    }

    fun dismissOverlayWindow(overlayWindow: OverlayWindow) {
        overlayWindows.remove(overlayWindow)

        val context = currentContext
        if (isShowing && context != null) {
            dismissOverlayWindow(context, overlayWindow)
        }
    }

    fun show(context: Context) {
        currentContext = context

        overlayWindows.forEach {
            showOverlayWindow(context, it)
        }

        isShowing = true
    }

    fun dismiss() {
        val context = currentContext
        if (context != null) {
            overlayWindows.forEach {
                dismissOverlayWindow(context, it)
            }
            isShowing = false
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    private fun showOverlayWindow(context: Context, overlayWindow: OverlayWindow) {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val layoutParams = overlayWindow.layoutParams
        val composeView = overlayWindow.composeView
        
        
        composeView.setOnApplyWindowInsetsListener { view, insets ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                view.windowInsetsController?.hide(android.view.WindowInsets.Type.systemBars())
                view.windowInsetsController?.systemBarsBehavior = 
                    android.view.WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            } else {
                @Suppress("DEPRECATION")
                view.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
            }
            insets
        }
        
        composeView.setContent {
            LuminaClientTheme {
                CompositionLocalProvider(LocalOverscrollConfiguration provides null) {
                    overlayWindow.Content()
                }
            }
        }
        
        val lifecycleOwner = overlayWindow.lifecycleOwner
        lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        composeView.setViewTreeLifecycleOwner(lifecycleOwner)
        composeView.setViewTreeViewModelStoreOwner(object : ViewModelStoreOwner {
            override val viewModelStore: ViewModelStore
                get() = overlayWindow.viewModelStore
        })
        composeView.setViewTreeSavedStateRegistryOwner(lifecycleOwner)
        composeView.compositionContext = overlayWindow.recomposer
        if (overlayWindow.firstRun) {
            overlayWindow.composeScope.launch {
                overlayWindow.recomposer.runRecomposeAndApplyChanges()
            }
            overlayWindow.firstRun = false
        }

        try {
            if (composeView.parent == null) {
                windowManager.addView(composeView, layoutParams)
            }
        } catch (e: Exception) {
            Log.e("OverlayManager", "Failed to add overlay window", e)

            if (e is WindowManager.BadTokenException) {
                Toast.makeText(
                    context,
                    "Failed to add overlay window: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun dismissOverlayWindow(context: Context, overlayWindow: OverlayWindow) {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val composeView = overlayWindow.composeView

        try {
            if (composeView.parent != null) {
                windowManager.removeView(composeView)
            }
        } catch (e: Exception) {
            Log.w("OverlayManager", "Failed to remove overlay window", e)
        }
    }

    fun showCustomOverlay(view: View) {
        val wm = AppContext.instance.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )
        wm.addView(view, params)
    }

    fun dismissCustomOverlay(view: View) {
        val wm = AppContext.instance.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        wm.removeView(view)
    }


}