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
package io.lumina.luminaux


import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

import io.lumina.luminaux.components.FlickeringStartButton
import io.lumina.luminaux.components.FloatingBottomNavBar
import io.lumina.luminaux.components.GlassmorphicCard
import io.lumina.luminaux.components.VerticalNavButtons
import io.lumina.luminaux.components.VideoBackground
import io.lumina.luminaux.ui.theme.LuminaUXTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        hideSystemBars()

        setContent {
            LuminaUXTheme {
                GameUI(
                   // onUserInteraction = { showSystemBars() },
                    hideSystemBars = { hideSystemBars() }
                )
            }
        }
    }



    private fun hideSystemBars() {
        WindowCompat.setDecorFitsSystemWindows(window, false)

        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                )

        val controller = ViewCompat.getWindowInsetsController(window.decorView)
        controller?.let {
            it.hide(WindowInsetsCompat.Type.systemBars())
            it.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }


    private fun showSystemBars() {
        WindowCompat.setDecorFitsSystemWindows(window, true)
        val controller = ViewCompat.getWindowInsetsController(window.decorView)
        controller?.show(WindowInsetsCompat.Type.systemBars())
    }


    @Composable
    fun GameUI(
        onUserInteraction: () -> Unit = {},
        hideSystemBars: () -> Unit = {}
    ) {
        var uiVisible by remember { mutableStateOf(false) }
        val scope = rememberCoroutineScope()

        var interactionTime by remember { mutableStateOf(0L) } // Track last interaction time

        // Auto-hide system nav bar after 3 seconds of inactivity
        LaunchedEffect(interactionTime) {
            delay(3000) // 3 seconds delay
            hideSystemBars()
        }

        // Show UI on start
        LaunchedEffect(Unit) {
            delay(200)
            uiVisible = true
            interactionTime = System.currentTimeMillis() // Initialize interaction time
            hideSystemBars() // Hide nav bar initially
        }

        // Handle user interaction to show system nav bar
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures {
                        onUserInteraction() // Show system nav bar
                        interactionTime = System.currentTimeMillis() // Reset timer
                    }
                }
        ) {

            VideoBackground()

            // Animated Horizontal Cards Row
            AnimatedVisibility(
                visible = uiVisible,
                enter = fadeIn(animationSpec = tween(600, easing = FastOutSlowInEasing)) +
                        expandHorizontally(animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy)),
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(vertical = 48.dp, horizontal = 58.dp)
            ) {
               /* Row(
                    modifier = Modifier
                        .fillMaxHeight(0.90f)
                        .fillMaxWidth(0.85f)
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {*/
                    GlassmorphicCard(
                        title = "uwu",
                        modifier = Modifier
                            .fillMaxHeight(0.90f)
                            .width(200.dp)
                            .animateEnterExit(
                                enter = fadeIn(animationSpec = tween(500, delayMillis = 100)) +
                                        slideInVertically(
                                            initialOffsetY = { it / 5 },
                                            animationSpec = spring(
                                                dampingRatio = Spring.DampingRatioLowBouncy,
                                                stiffness = Spring.StiffnessMediumLow
                                            )
                                        )
                            )
                    )

            }
            AnimatedVisibility(
                visible = uiVisible,
                enter = fadeIn(animationSpec = tween(600, easing = FastOutSlowInEasing)) +
                        expandHorizontally(animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy)),
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(vertical = 48.dp, horizontal = 58.dp)
            ) {
                /* Row(
                     modifier = Modifier
                         .fillMaxHeight(0.90f)
                         .fillMaxWidth(0.85f)
                         .padding(vertical = 8.dp),
                     horizontalArrangement = Arrangement.spacedBy(12.dp)
                 ) {*/
                GlassmorphicCard(
                    title = "uwu",
                    modifier = Modifier
                        .fillMaxHeight(0.90f)
                        .width(200.dp)
                        .animateEnterExit(
                            enter = fadeIn(animationSpec = tween(500, delayMillis = 100)) +
                                    slideInVertically(
                                        initialOffsetY = { it / 5 },
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioLowBouncy,
                                            stiffness = Spring.StiffnessMediumLow
                                        )
                                    )
                        )
                )

            }

            // Bottom Area
            AnimatedVisibility(
                visible = uiVisible,
                enter = fadeIn(animationSpec = tween(600, delayMillis = 300)) +
                        slideInVertically(
                            initialOffsetY = { it / 4 },
                            animationSpec = tween(500, easing = FastOutSlowInEasing)
                        ),
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 20.dp, start = 16.dp, end = 16.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "© Project Lumina 2025 | v4.0.3",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 9.sp,
                        modifier = Modifier.align(Alignment.BottomStart)
                    )

                    FlickeringStartButton(
                        modifier = Modifier.align(Alignment.BottomCenter), onClick = {}
                    )

                    Text(
                        text = "Experience the Future",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 9.sp,
                        modifier = Modifier.align(Alignment.BottomEnd)
                    )
                }
            }

            AnimatedVisibility(
                visible = uiVisible,
                enter = fadeIn(animationSpec = tween(450, delayMillis = 450)) +
                        slideInHorizontally(
                            initialOffsetX = { it / 6 },
                            animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy)
                        ),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 10.dp, bottom = 30.dp)
            ) {
                VerticalNavButtons()
            }
        }
    }
}