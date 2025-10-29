package com.project.lumina.client.overlay.mods

import android.content.res.Configuration
import android.view.WindowManager
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.project.lumina.client.overlay.manager.OverlayManager
import com.project.lumina.client.overlay.manager.OverlayWindow
import com.project.lumina.client.ui.theme.*
import kotlin.math.min

class KeystrokesOverlay : OverlayWindow() {

    private val _layoutParams by lazy {
        super.layoutParams.apply {
            flags = flags or
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
            width = WindowManager.LayoutParams.WRAP_CONTENT
            height = WindowManager.LayoutParams.WRAP_CONTENT
            x = 100
            y = 100
        }
    }

    override val layoutParams: WindowManager.LayoutParams
        get() = _layoutParams

    private var keyStates by mutableStateOf(
        mapOf(
            "W" to false,
            "A" to false,
            "S" to false,
            "D" to false,
            "Space" to false
        )
    )

    companion object {
        val overlayInstance by lazy { KeystrokesOverlay() }
        private var shouldShowOverlay = false

        fun showOverlay() {
            if (shouldShowOverlay) {
                try {
                    OverlayManager.showOverlayWindow(overlayInstance)
                } catch (e: Exception) {
                    println("Error showing KeystrokesOverlay: ${e.message}")
                }
            }
        }

        fun dismissOverlay() {
            try {
                OverlayManager.dismissOverlayWindow(overlayInstance)
            } catch (e: Exception) {
                println("Error dismissing KeystrokesOverlay: ${e.message}")
            }
        }

        fun setOverlayEnabled(enabled: Boolean) {
            shouldShowOverlay = enabled
            if (enabled) showOverlay() else dismissOverlay()
        }

        fun isOverlayEnabled(): Boolean = shouldShowOverlay

        fun setKeyState(key: String, isPressed: Boolean) {
            overlayInstance.keyStates = overlayInstance.keyStates.toMutableMap().apply {
                if (containsKey(key)) {
                    this[key] = isPressed
                }
            }
        }
    }

    @Composable
    override fun Content() {
        if (!isOverlayEnabled()) return

        val context = LocalContext.current
        val configuration = LocalConfiguration.current
        val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

        LaunchedEffect(isLandscape) {
            val width = context.resources.displayMetrics.widthPixels
            val height = context.resources.displayMetrics.heightPixels
            _layoutParams.x = min(width - 130, _layoutParams.x)
            _layoutParams.y = min(height - 130, _layoutParams.y)
            windowManager.updateViewLayout(composeView, _layoutParams)
        }

        KeystrokesContent(keyStates = keyStates) { dx, dy ->
            _layoutParams.x += dx.toInt()
            _layoutParams.y += dy.toInt()
            windowManager.updateViewLayout(composeView, _layoutParams)
        }
    }

    @Composable
    private fun KeystrokesContent(
        keyStates: Map<String, Boolean>,
        onDrag: (Float, Float) -> Unit
    ) {
        Box(
            modifier = Modifier
                .wrapContentSize()
                .pointerInput(Unit) {
                    detectDragGestures { _, drag ->
                        onDrag(drag.x, drag.y)
                    }
                }
        ) {
            Column(
                modifier = Modifier.wrapContentSize(),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                KeyButton(
                    label = "W",
                    isPressed = keyStates["W"] ?: false,
                    modifier = Modifier.size(40.dp)
                )

                Row(
                    modifier = Modifier.wrapContentSize(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally)
                ) {
                    KeyButton(
                        label = "A",
                        isPressed = keyStates["A"] ?: false,
                        modifier = Modifier.size(40.dp)
                    )
                    KeyButton(
                        label = "S",
                        isPressed = keyStates["S"] ?: false,
                        modifier = Modifier.size(40.dp)
                    )
                    KeyButton(
                        label = "D",
                        isPressed = keyStates["D"] ?: false,
                        modifier = Modifier.size(40.dp)
                    )
                }

                KeyButton(
                    label = " ",
                    isPressed = keyStates["Space"] ?: false,
                    modifier = Modifier.size(130.dp, 40.dp)
                )
            }
        }
    }

    @Composable
    private fun KeyButton(
        label: String,
        isPressed: Boolean,
        modifier: Modifier = Modifier
    ) {
        val animValue by animateFloatAsState(
            targetValue = if (isPressed) 1f else 0f,
            animationSpec = tween(durationMillis = 80),
            label = "KeyAnimation_$label"
        )
        val scale by animateFloatAsState(
            targetValue = lerp(0.96f, 0.91f, animValue),
            animationSpec = tween(durationMillis = 80),
            label = "ScaleAnimation_$label"
        )

        


        Box(
            modifier = modifier
                .scale(scale)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    if (isPressed) pressedColor.copy(alpha = 0.8f) else baseColor.copy(alpha = 0.85f)
                )
                .border(
                    width = 1.dp,
                    color = if (isPressed) pressedColor else borderColor.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                color = if (isPressed) Color.White else textColor,
                fontSize = 16.sp,
                fontWeight = if (isPressed) FontWeight.Bold else FontWeight.Medium,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }

    private fun lerp(start: Float, stop: Float, fraction: Float): Float =
        start + fraction * (stop - start)

    @Preview(showBackground = false)
    @Composable
    private fun KeystrokesOverlayPreview() {
        KeystrokesContent(
            keyStates = mapOf(
                "W" to true,
                "A" to false,
                "S" to false,
                "D" to false,
                "Space" to false
            ),
            onDrag = { _, _ -> }
        )
    }
}