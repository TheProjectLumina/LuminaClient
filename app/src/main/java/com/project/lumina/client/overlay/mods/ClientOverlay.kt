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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.project.lumina.client.R
import com.project.lumina.client.overlay.manager.OverlayManager
import com.project.lumina.client.overlay.manager.OverlayWindow


class ClientOverlay : OverlayWindow() {
    private val _layoutParams by lazy {
        super.layoutParams.apply {
            flags = flags or
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
            width = WindowManager.LayoutParams.MATCH_PARENT
            height = WindowManager.LayoutParams.WRAP_CONTENT
            gravity = Gravity.TOP or Gravity.START
            x = 0
            y = 0
        }
    }

    override val layoutParams: WindowManager.LayoutParams
        get() = _layoutParams

    companion object {
        private val overlayInstance by lazy { ClientOverlay() }
        private var shouldShowOverlay = true


        /*private fun hsvToRgb(h: Float, s: Float, v: Float): Color {
            val hDegrees = h * 360f
            val c = v * s
            val x = c * (1 - abs((hDegrees / 60f) % 2 - 1))
            val m = v - c

            val (r, g, b) = when {
                hDegrees < 60 -> Triple(c, x, 0f)
                hDegrees < 120 -> Triple(x, c, 0f)
                hDegrees < 180 -> Triple(0f, c, x)
                hDegrees < 240 -> Triple(0f, x, c)
                hDegrees < 300 -> Triple(x, 0f, c)
                else -> Triple(c, 0f, x)
            }

            return Color(
                red = (r + m).coerceIn(0f, 1f),
                green = (g + m).coerceIn(0f, 1f),
                blue = (b + m).coerceIn(0f, 1f)
            )
        }*/

        fun showOverlay() {
            if (shouldShowOverlay) {
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
        if (!isOverlayEnabled()) return

        val firaSansFamily = FontFamily(Font(R.font.packet))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 0.dp),
            contentAlignment = Alignment.TopStart
        ) {

            for (i in 1..5) {
                Text(
                    text = "Project Lumina",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = firaSansFamily,
                    color = Color.White.copy(alpha = 0.15f),
                    modifier = Modifier
                        .padding(start = 8.dp, top = 13.dp, bottom = 8.dp)
                        .offset(x = (i * 0.5f).dp, y = (i * 0.5f).dp)
                )
            }


            Text(
                text = "Project Lumina",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = firaSansFamily,
                color = Color.White,
                modifier = Modifier
                    .padding(start = 8.dp, top = 13.dp, bottom = 8.dp)
            )
        }
    }



}