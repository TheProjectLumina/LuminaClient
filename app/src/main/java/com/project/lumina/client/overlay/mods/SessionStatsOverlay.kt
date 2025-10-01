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

package com.project.lumina.client.overlay.mods

import android.view.Gravity
import android.view.WindowManager
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import com.project.lumina.client.overlay.manager.OverlayManager
import com.project.lumina.client.overlay.manager.OverlayWindow
import kotlinx.coroutines.delay

class SessionStatsOverlay : OverlayWindow() {

    private val _statLines = mutableStateOf<List<String>>(emptyList())
    private val _isVisible = mutableStateOf(true)

    private val _layoutParams by lazy {
        super.layoutParams.apply {
            flags = flags or
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
            width = WindowManager.LayoutParams.WRAP_CONTENT
            height = WindowManager.LayoutParams.WRAP_CONTENT
            gravity = Gravity.TOP or Gravity.START
            x = 20
            y = 50
        }
    }

    override val layoutParams: WindowManager.LayoutParams
        get() = _layoutParams

    @Composable
    override fun Content() {
        SessionStatsCard(
            statLines = _statLines,
            isVisible = _isVisible,
            overlay = this
        )
    }

    fun updateStats(statLines: List<String>) {
        _statLines.value = statLines
    }

    fun addStat(statLine: String) {
        _statLines.value = _statLines.value + statLine
    }

    fun clearStats() {
        _statLines.value = emptyList()
    }

    fun dismiss() {
        _isVisible.value = false
    }

    companion object {
        private var currentOverlay: SessionStatsOverlay? = null

        fun showSessionStats(initialStats: List<String> = emptyList()): SessionStatsOverlay {
            currentOverlay?.let {
                OverlayManager.dismissOverlayWindow(it)
            }

            val overlay = SessionStatsOverlay().apply {
                updateStats(initialStats)
            }

            OverlayManager.showOverlayWindow(overlay)
            currentOverlay = overlay
            return overlay
        }
    }
}

@Composable
fun SessionStatsCard(
    statLines: MutableState<List<String>>,
    isVisible: MutableState<Boolean>,
    overlay: OverlayWindow
) {
    var visible by remember { isVisible }

    
    val infiniteTransition = rememberInfiniteTransition(label = "hueAnimation")
    val animatedHue by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "hue"
    )

    
    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.8f,
        animationSpec = tween(
            durationMillis = 300,
            easing = FastOutSlowInEasing
        ),
        label = "scale"
    )

    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(200),
        label = "alpha"
    )

    
    fun getThemedColor(index: Int): Color {
        val hue = (animatedHue + (index * 18f)) % 360f
        return Color.hsv(hue, 0.7f, 0.9f)
    }

    Box(
        modifier = Modifier
            .scale(scale)
            .alpha(alpha)
            .width(160.dp)
            .height(120.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                Color.Black.copy(alpha = 0.6f) 
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            
            Text(
                text = "Statistics",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(4.dp))

            
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .clip(RoundedCornerShape(1.dp))
            ) {
                val lineLength = 20
                val lengthPerLine = size.width / lineLength

                for (i in 0 until lineLength) {
                    val color = getThemedColor(i)
                    drawRect(
                        color = color,
                        topLeft = Offset(i * lengthPerLine, 0f),
                        size = Size(lengthPerLine, size.height)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            
            Column(modifier = Modifier.fillMaxWidth()) {
                statLines.value.forEach { statLine ->
                    val parts = statLine.split(":", limit = 2)

                    if (parts.size == 2) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 1.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = parts[0].trim(),
                                fontSize = 12.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Normal,
                                textAlign = TextAlign.Start
                            )
                            Text(
                                text = parts[1].trim(),
                                fontSize = 12.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Normal,
                                textAlign = TextAlign.End
                            )
                        }
                    } else {
                        Text(
                            text = statLine,
                            fontSize = 12.sp,
                            color = Color.White,
                            modifier = Modifier.padding(vertical = 1.dp),
                            fontWeight = FontWeight.Normal,
                            textAlign = TextAlign.Start
                        )
                    }
                }
            }
        }
    }

    if (!visible) {
        LaunchedEffect(visible) {
            delay(300)
            OverlayManager.dismissOverlayWindow(overlay)
        }
    }
}