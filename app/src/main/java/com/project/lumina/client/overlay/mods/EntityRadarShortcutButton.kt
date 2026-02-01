package com.project.lumina.client.overlay.mods

import android.content.Context
import android.content.res.Configuration
import android.view.WindowManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.project.lumina.client.R
import com.project.lumina.client.application.AppContext
import com.project.lumina.client.overlay.manager.OverlayWindow

class EntityRadarShortcutButton(
    private val onShortcutClick: () -> Unit
) : OverlayWindow() {

    companion object {
        private const val PREFS_NAME = "entity_radar_shortcut"
        private const val KEY_X = "x"
        private const val KEY_Y = "y"
    }

    private val _layoutParams by lazy {
        val prefs = AppContext.instance.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        super.layoutParams.apply {
            layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            windowAnimations = android.R.style.Animation_Toast
            x = prefs.getInt(KEY_X, 0)
            y = prefs.getInt(KEY_Y, 100)
        }
    }

    override val layoutParams: WindowManager.LayoutParams
        get() = _layoutParams

    @Composable
    override fun Content() {
        val context = LocalContext.current
        val width = context.resources.displayMetrics.widthPixels
        val height = context.resources.displayMetrics.heightPixels
        val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
        val buttonSizePx = with(LocalDensity.current) { 56.dp.roundToPx() }
        val density = LocalDensity.current

        LaunchedEffect(isLandscape) {
            val buttonSizePx = with(density) { 56.dp.roundToPx() }
            _layoutParams.x = _layoutParams.x.coerceIn(0, width - buttonSizePx)
            _layoutParams.y = _layoutParams.y.coerceIn(0, height - buttonSizePx)
            windowManager.updateViewLayout(composeView, _layoutParams)
            updateShortcut()
        }

        Box(
            modifier = Modifier
                .padding(5.dp)
                .size(56.dp)
        ) {
            ElevatedCard(
                onClick = { onShortcutClick() },
                shape = CircleShape,
                colors = CardDefaults.elevatedCardColors(containerColor = Color.Black.copy(alpha = 0.8f)),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectDragGestures { _, dragAmount ->
                            _layoutParams.x = (_layoutParams.x + dragAmount.x.toInt()).coerceIn(0, width - buttonSizePx)
                            _layoutParams.y = (_layoutParams.y + dragAmount.y.toInt()).coerceIn(0, height - buttonSizePx)
                            windowManager.updateViewLayout(composeView, _layoutParams)
                            updateShortcut()
                        }
                    }
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Image(
                        painter = painterResource(id = R.drawable.moon_stars_24),
                        contentDescription = "Entity Radar",
                        colorFilter = ColorFilter.tint(Color.White),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }

    private fun updateShortcut() {
        val prefs = AppContext.instance.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putInt(KEY_X, _layoutParams.x)
            .putInt(KEY_Y, _layoutParams.y)
            .apply()
    }
}