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
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.nativeCanvas
import com.project.lumina.client.overlay.manager.OverlayManager
import com.project.lumina.client.overlay.manager.OverlayWindow
import com.project.lumina.client.ui.theme.*
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class Position(val x: Float, val y: Float)

class MiniMapOverlay : OverlayWindow() {
    private val _layoutParams by lazy {
        super.layoutParams.apply {
            flags = flags or
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
            width = WindowManager.LayoutParams.WRAP_CONTENT
            height = WindowManager.LayoutParams.WRAP_CONTENT
            gravity = Gravity.TOP or Gravity.END
            x = 50
            y = 50
        }
    }

    override val layoutParams: WindowManager.LayoutParams
        get() = _layoutParams

    private var centerPosition by mutableStateOf(Position(0f, 0f))
    private var playerRotation by mutableStateOf(0f)
    private var targets by mutableStateOf(listOf<Position>())
    private var minimapSize by mutableStateOf(100f)
    private var targetRotation by mutableStateOf(0f)
    private var rotationSmoothStep = 0.15f

    companion object {
        val overlayInstance by lazy { MiniMapOverlay() }
        private var shouldShowOverlay = false

        fun showOverlay() {
            if (shouldShowOverlay) {
                try {
                    OverlayManager.showOverlayWindow(overlayInstance)
                } catch (e: Exception) {}
            }
        }

        fun dismissOverlay() {
            try {
                OverlayManager.dismissOverlayWindow(overlayInstance)
            } catch (e: Exception) {}
        }

        fun setOverlayEnabled(enabled: Boolean) {
            shouldShowOverlay = enabled
            if (enabled) showOverlay() else dismissOverlay()
        }

        fun isOverlayEnabled(): Boolean = shouldShowOverlay

        fun setCenter(x: Float, y: Float) {
            overlayInstance.centerPosition = Position(x, y)
        }

        fun setPlayerRotation(rotation: Float) {
            overlayInstance.targetRotation = rotation
        }

        fun setTargets(targetList: List<Position>) {
            overlayInstance.targets = targetList
        }

        fun setMinimapSize(size: Float) {
            overlayInstance.minimapSize = size
        }
    }

    @Composable
    override fun Content() {
        if (!isOverlayEnabled()) return

        LaunchedEffect(targetRotation) {
            while (kotlin.math.abs(playerRotation - targetRotation) > 0.001f) {

                var delta = (targetRotation - playerRotation) % (2 * Math.PI).toFloat()
                if (delta > Math.PI) delta -= (2 * Math.PI).toFloat()
                if (delta < -Math.PI) delta += (2 * Math.PI).toFloat()

                playerRotation += delta * rotationSmoothStep
                kotlinx.coroutines.delay(16L)
            }
        }


        Minimap(centerPosition, playerRotation, targets, minimapSize)
    }

    @Composable
    private fun Minimap(center: Position, rotation: Float, targets: List<Position>, size: Float) {
        val dpSize = size.dp
        val rawRadius = size / 2
        val radius = rawRadius * minimapZoom
        val scale = 2f * minimapZoom

        Box(
            modifier = Modifier
                .size(dpSize)
                .background(Mbg, shape = RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(dpSize)) {
                val centerX = this.size.width / 2
                val centerY = this.size.height / 2


                val gridColor = MgridColor
                val gridSpacing = this.size.width / 10
                for (i in 1 until 10) {
                    val x = i * gridSpacing
                    drawLine(gridColor, Offset(x, 0f), Offset(x, this.size.height), strokeWidth = 1f)
                    drawLine(gridColor, Offset(0f, x), Offset(this.size.width, x), strokeWidth = 1f)
                }


                drawLine(MCrosshair, Offset(centerX, 0f), Offset(centerX, this.size.height), strokeWidth = 1.5f)
                drawLine(MCrosshair, Offset(0f, centerY), Offset(this.size.width, centerY), strokeWidth = 1.5f)


                val playerDotRadius = minimapDotSize * minimapZoom
                drawCircle(MPlayerMarker, radius = playerDotRadius, center = Offset(centerX, centerY))



                val northAngle = -rotation
                val northDistance = rawRadius * 0.95f
                val northX = centerX + northDistance * sin(northAngle)
                val northY = centerY - northDistance * cos(northAngle)


                val paint = android.graphics.Paint().apply {
                    color = android.graphics.Color.BLUE
                    textSize = size * 0.14f
                    textAlign = android.graphics.Paint.Align.CENTER
                    isFakeBoldText = true
                    isAntiAlias = true
                }


                drawContext.canvas.nativeCanvas.drawText("^", northX, northY - paint.textSize * 0.6f, paint)
                drawContext.canvas.nativeCanvas.drawText("N", northX, northY + paint.textSize * 0.4f, paint)




                targets.forEach { target ->
                    val relX = target.x - center.x
                    val relY = target.y - center.y
                    val distance = sqrt(relX * relX + relY * relY) * scale


                    val dotRadius = minimapDotSize * minimapZoom

                    val angle = atan2(relY, relX) - rotation
                    val clampedDistance = if (distance < radius * 0.9f) distance else radius * 0.85f
                    val entityX = centerX + clampedDistance * sin(angle)
                    val entityY = centerY - clampedDistance * cos(angle)

                    drawCircle(
                        color = if (distance < radius * 0.9f) MEntityClose else MEntityFar,
                        radius = dotRadius,
                        center = Offset(entityX, entityY)
                    )
                }
            }
        }
    }
}