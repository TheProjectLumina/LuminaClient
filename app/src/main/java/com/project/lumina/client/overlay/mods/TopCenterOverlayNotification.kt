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
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.project.lumina.client.overlay.manager.OverlayManager
import com.project.lumina.client.overlay.manager.OverlayWindow
import com.project.lumina.client.ui.theme.TCOBackground
import com.project.lumina.client.ui.theme.TCOGradient1
import com.project.lumina.client.ui.theme.TCOGradient2
import com.project.lumina.client.ui.theme.TextColorForModules
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class TopCenterOverlayNotification : OverlayWindow() {
    private val _layoutParams by lazy {
        super.layoutParams.apply {
            flags = flags or
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
            width = WindowManager.LayoutParams.WRAP_CONTENT
            height = WindowManager.LayoutParams.WRAP_CONTENT
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            y = 50
        }
    }

    override val layoutParams: WindowManager.LayoutParams
        get() = _layoutParams

    companion object {
        private val notificationState = TopNotificationState()

        fun addNotification(
            title: String,
            subtitle: String,
            iconRes: Int? = null,
            progressDuration: Long = 2500
        ) {
            notificationState.addNotification(title, subtitle, iconRes, progressDuration)
        }
    }

    @Composable
    override fun Content() {
        val notification = notificationState.currentNotification

        LaunchedEffect(notification) {
            if (notification == null) {
                delay(400)
                OverlayManager.dismissOverlayWindow(this@TopCenterOverlayNotification)
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 16.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            notification?.let {
                TopNotificationCard(it, notificationState)
            }
        }
    }
}

@Composable
fun TopNotificationCard(
    notification: TopNotificationItem,
    notificationState: TopNotificationState
) {
    val scope = rememberCoroutineScope()
    var visible by remember { mutableStateOf(true) }
    val progressAnimatable = remember { Animatable(1f) }

    
    val gradientBrush = remember {
        Brush.linearGradient(
            colors = listOf(TCOGradient1, TCOGradient2),
            start = Offset.Zero,
            end = Offset(100f, 100f)
        )
    }

    AnimatedVisibility(
        visible = visible,
        enter = scaleIn(
            initialScale = 0.7f,
            animationSpec = tween(
                durationMillis = 300,
                easing = FastOutSlowInEasing
            )
        ) + fadeIn(tween(200)),
        exit = scaleOut(
            targetScale = 0.7f,
            animationSpec = tween(
                durationMillis = 300,
                easing = FastOutSlowInEasing
            )
        ) + fadeOut(tween(200))
    ) {
        Box(
            modifier = Modifier
                .width(280.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(TCOBackground)
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(gradientBrush),
                    contentAlignment = Alignment.Center
                ) {
                    if (notification.iconRes != null) {
                        Image(
                            painter = painterResource(id = notification.iconRes),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Text(
                            text = notification.title.firstOrNull()?.uppercase() ?: "N",
                            color = TextColorForModules,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = notification.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextColorForModules,
                        maxLines = 1
                    )
                    Text(
                        text = notification.subtitle,
                        fontSize = 12.sp,
                        color = TextColorForModules.copy(alpha = 0.7f),
                        maxLines = 1,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Color.Gray.copy(alpha = 0.2f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(progressAnimatable.value)
                                .height(4.dp)
                                .background(gradientBrush)
                        )
                    }
                }
            }
        }
    }

    LaunchedEffect(notification.id) {
        scope.launch {
            progressAnimatable.animateTo(
                0f,
                animationSpec = tween(
                    durationMillis = notification.progressDuration.toInt(),
                    easing = LinearEasing
                )
            )
        }
        delay(notification.progressDuration)
        visible = false
        delay(400) 
        notificationState.clearNotification()
    }
}

class TopNotificationState {
    private val _currentNotification = mutableStateOf<TopNotificationItem?>(null)
    val currentNotification: TopNotificationItem? get() = _currentNotification.value

    private var nextId = 0

    fun addNotification(
        title: String,
        subtitle: String,
        iconRes: Int? = null,
        progressDuration: Long = 2500
    ) {
        _currentNotification.value = TopNotificationItem(
            id = nextId++,
            title = title,
            subtitle = subtitle,
            iconRes = iconRes,
            progressDuration = progressDuration
        )
    }

    fun clearNotification() {
        _currentNotification.value = null
    }
}

data class TopNotificationItem(
    val id: Int,
    val title: String,
    val subtitle: String,
    val iconRes: Int? = null,
    val progressDuration: Long = 1000
)