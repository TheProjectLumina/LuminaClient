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
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.sin
import kotlin.math.cos
import kotlin.math.pow
import com.project.lumina.client.overlay.manager.OverlayManager
import com.project.lumina.client.overlay.manager.OverlayWindow
import com.project.lumina.client.CPPBridge.NativeHsvToRgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.rememberTextMeasurer

private fun createColorFromHsv(hue: Float, saturation: Float, value: Float): Color {
    val normalizedHue = (hue % 360f) / 360f
    val rgb = NativeHsvToRgb.hsvToRgb(normalizedHue)

    val r = rgb[0]
    val g = rgb[1]
    val b = rgb[2]

    val adjustedR = ((r - 0.5f) * saturation + 0.5f) * value
    val adjustedG = ((g - 0.5f) * saturation + 0.5f) * value
    val adjustedB = ((b - 0.5f) * saturation + 0.5f) * value

    return Color(
        red = adjustedR.coerceIn(0f, 1f),
        green = adjustedG.coerceIn(0f, 1f),
        blue = adjustedB.coerceIn(0f, 1f),
        alpha = 1f
    )
}

class OverlayModuleList : OverlayWindow() {
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
            x = 10
            y = 5
        }
    }

    override val layoutParams: WindowManager.LayoutParams
        get() = _layoutParams

    companion object {
        private val moduleState = ModuleState()
        private val overlayInstance by lazy { OverlayModuleList() }
        private var shouldShowOverlay = false

        fun showText(moduleName: String) {
            if (shouldShowOverlay && moduleName != "ArrayList") {
                moduleState.addModule(moduleName)
                try {
                    OverlayManager.showOverlayWindow(overlayInstance)
                } catch (e: Exception) {

                }
            }
        }

        fun removeText(moduleName: String) {
            if (moduleName != "ArrayList") {
                moduleState.removeModule(moduleName)
                if (moduleState.modules.isEmpty()) {
                    try {
                        OverlayManager.dismissOverlayWindow(overlayInstance)
                    } catch (e: Exception) {

                    }
                }
            }
        }

        fun setOverlayEnabled(enabled: Boolean) {
            shouldShowOverlay = enabled
            if (!enabled) {
                try {
                    OverlayManager.dismissOverlayWindow(overlayInstance)
                } catch (e: Exception) {

                }
            }
        }

        fun isOverlayEnabled(): Boolean = shouldShowOverlay
    }

    @Composable
    override fun Content() {
        if (!isOverlayEnabled()) return

        val textMeasurer = rememberTextMeasurer()
        val density = LocalDensity.current

        val globalTransition = rememberInfiniteTransition(label = "globalRgbTransition")
        val globalPhase by globalTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(6000, easing = LinearEasing)
            ),
            label = "globalColorPhase"
        )

        Column(
            modifier = Modifier
                .fillMaxHeight()
                .wrapContentWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            val sortedModules = moduleState.modules.sortedByDescending { module ->
                val textStyle = TextStyle(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                val textLayoutResult = textMeasurer.measure(
                    text = module.name,
                    style = textStyle
                )
                textLayoutResult.size.width
            }

            if (sortedModules.isNotEmpty()) {
                sortedModules.forEachIndexed { index, module ->
                    key(module.id) {
                        AnimatedModuleItem(
                            module = module,
                            index = index,
                            globalPhase = globalPhase
                        )
                    }
                }
            }
        }
    }

    @Composable
    fun AnimatedModuleItem(
        module: ModuleItem,
        index: Int,
        globalPhase: Float
    ) {
        var visible by remember { mutableStateOf(false) }
        val exitState by remember { derivedStateOf { moduleState.modulesToRemove.contains(module.name) } }

        LaunchedEffect(Unit) {
            delay((index * 30).toLong())
            visible = true
        }

        LaunchedEffect(exitState) {
            if (exitState) {
                delay(250)
                moduleState.removeModule(module.name)
            }
        }

        val slideAnimation by animateFloatAsState(
            targetValue = when {
                exitState -> 150f
                visible -> 0f
                else -> 150f
            },
            animationSpec = tween(
                durationMillis = 300,
                easing = EaseInOutCubic
            ),
            label = "slideAnimation"
        )

        val alphaAnimation by animateFloatAsState(
            targetValue = if (visible && !exitState) 1f else 0f,
            animationSpec = tween(200),
            label = "alphaAnimation"
        )

        val gradientColors = listOf(
            createColorFromHsv(globalPhase % 360, 0.8f, 1.0f),
            createColorFromHsv((globalPhase + 120) % 360, 0.3f, 1.0f),
            createColorFromHsv((globalPhase + 240) % 360, 0.9f, 0.9f)
        )

        val animatedGradient = Brush.horizontalGradient(
            colors = gradientColors,
            startX = 0f,
            endX = module.name.length * 8f
        )

        Box(
            modifier = Modifier
                .wrapContentWidth()
                .background(
                    color = Color.Black.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(2.dp)
                )
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(
                text = module.name,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .offset(x = slideAnimation.dp)
                    .alpha(alphaAnimation),
                style = TextStyle(
                    brush = animatedGradient,
                    shadow = Shadow(
                        color = gradientColors[0].copy(alpha = 0.4f),
                        offset = Offset(1f, 1f),
                        blurRadius = 2f
                    )
                )
            )
        }
    }
}

class ModuleState {
    private val _modules = mutableStateListOf<ModuleItem>()
    val modules: List<ModuleItem> get() = _modules.toList()
    private var nextId = 0
    private val _modulesToRemove = mutableStateListOf<String>()
    val modulesToRemove: List<String> get() = _modulesToRemove.toList()

    fun addModule(moduleName: String) {
        if (_modules.none { it.name == moduleName }) {
            _modules.add(ModuleItem(nextId++, moduleName))
            _modulesToRemove.remove(moduleName) 
        }
    }

    fun markForRemoval(moduleName: String) {
        if (!_modulesToRemove.contains(moduleName)) {
            _modulesToRemove.add(moduleName)
        }
    }

    fun removeModule(moduleName: String) {
        _modules.removeAll { it.name == moduleName }
        _modulesToRemove.remove(moduleName)
    }

    fun isModuleEnabled(moduleName: String): Boolean {
        return _modules.any { it.name == moduleName } && !_modulesToRemove.contains(moduleName)
    }

    fun toggleModule(moduleName: String) {
        if (isModuleEnabled(moduleName)) {
            markForRemoval(moduleName)
        } else {
            addModule(moduleName)
        }
    }
}

data class ModuleItem(val id: Int, val name: String)