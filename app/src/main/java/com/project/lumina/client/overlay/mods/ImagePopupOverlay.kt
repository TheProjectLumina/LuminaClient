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
 * or don’t bother pretending.
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

package com.project.lumina.client.overlay.mods


import android.view.Gravity
import android.view.WindowManager
import androidx.compose.animation.core.EaseInBack
import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.EaseOutBack
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.project.lumina.client.overlay.manager.OverlayManager
import com.project.lumina.client.overlay.manager.OverlayWindow

import kotlinx.coroutines.delay

class ImagePopupOverlay : OverlayWindow() {
    private val _layoutParams by lazy {
        super.layoutParams.apply {
            flags = flags or
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
            width = WindowManager.LayoutParams.MATCH_PARENT
            height = WindowManager.LayoutParams.MATCH_PARENT
            gravity = Gravity.CENTER
        }
    }

    override val layoutParams: WindowManager.LayoutParams
        get() = _layoutParams

    companion object {
        private val overlayInstance by lazy { ImagePopupOverlay() }
        private var shouldShowOverlay = true
        private var currentDrawableResId: Int = 0

        fun showOverlay(drawableResId: Int) {
            if (shouldShowOverlay) {
                currentDrawableResId = drawableResId
                try {
                    OverlayManager.showOverlayWindow(overlayInstance)
                } catch (e: Exception) {
                    
                }
            }
        }

        fun dismissOverlay() {
            try {
                OverlayManager.dismissOverlayWindow(overlayInstance)
            } catch (e: Exception) {
                
            }
        }

        fun setOverlayEnabled(enabled: Boolean) {
            shouldShowOverlay = enabled
            if (!enabled) {
                dismissOverlay()
            }
        }

        fun isOverlayEnabled(): Boolean = shouldShowOverlay
    }

    @Composable
    override fun Content() {
        if (!isOverlayEnabled() || currentDrawableResId == 0) return

        
        var overlayVisible by remember { mutableStateOf(false) }
        var imageVisible by remember { mutableStateOf(false) }
        var dismissing by remember { mutableStateOf(false) }

        
        val backgroundAlpha by animateFloatAsState(
            targetValue = if (overlayVisible) 0.7f else 0f,
            animationSpec = tween(500),
            label = "backgroundAlpha"
        )

        val blurRadius by animateFloatAsState(
            targetValue = if (overlayVisible) 10f else 0f,
            animationSpec = tween(500),
            label = "blurRadius"
        )

        
        val imageOffsetY by animateFloatAsState(
            targetValue = if (imageVisible) 0f else 200f,
            animationSpec = tween(700, easing = EaseOutBack),
            label = "imageOffsetY"
        )

        
        val infiniteTransition = rememberInfiniteTransition(label = "levitationTransition")
        val levitation by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(2000, easing = EaseInOutSine),
                repeatMode = RepeatMode.Reverse
            ),
            label = "levitation"
        )

        
        val exitScale by animateFloatAsState(
            targetValue = if (dismissing) 1.3f else 1f,
            animationSpec = tween(300, easing = EaseInBack),
            label = "exitScale"
        )

        val exitAlpha by animateFloatAsState(
            targetValue = if (dismissing) 0f else 1f,
            animationSpec = tween(300),
            label = "exitAlpha"
        )

        
        LaunchedEffect(Unit) {
            
            overlayVisible = true
            delay(200)

            
            imageVisible = true
            delay(5000)

            
            dismissing = true
            delay(300)

            
            dismissOverlay()
        }

        
        val levitationOffset = (levitation * 15 - 7.5f)

        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = backgroundAlpha))
                .blur(blurRadius.dp)
        ) {
            
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                
                Surface(
                    modifier = Modifier
                        .size(250.dp)
                        .graphicsLayer {
                            translationY = imageOffsetY + levitationOffset
                            scaleX = exitScale
                            scaleY = exitScale
                            alpha = exitAlpha
                        },
                    shadowElevation = 8.dp
                ) {
                    Image(
                        painter = painterResource(id = currentDrawableResId),
                        contentDescription = "Popup Image",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    )
                }
            }
        }
    }
}