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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Error
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

enum class NotificationType {
    SUCCESS, WARNING, ERROR, INFO
}


object SimpleOverlayNotification {
    private val notificationState = SimpleNotificationState()

    fun show(
        message: String,
        type: NotificationType = NotificationType.INFO,
        durationMs: Long = 3000
    ) {
        notificationState.setNotification(SimpleNotificationItem(message, type, durationMs))
    }
    
    @Composable
    fun Content() {
        val notification = notificationState.currentNotification

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.TopCenter
        ) {
            notification?.let {
                SimpleNotificationCard(it, notificationState)
            }
        }
    }
}

data class SimpleNotificationItem(
    val message: String,
    val type: NotificationType = NotificationType.INFO,
    val duration: Long = 3000
)

class SimpleNotificationState {
    private val _currentNotification = mutableStateOf<SimpleNotificationItem?>(null)
    val currentNotification: SimpleNotificationItem? get() = _currentNotification.value

    fun setNotification(notification: SimpleNotificationItem) {
        _currentNotification.value = notification
    }

    fun clearNotification() {
        _currentNotification.value = null
    }
}

@Composable
fun SimpleNotificationCard(
    notification: SimpleNotificationItem,
    state: SimpleNotificationState
) {
    var visible by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf(1f) }
    
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(
            durationMillis = notification.duration.toInt(),
            easing = LinearEasing
        ),
        label = "NotificationProgress"
    )

    val icon: ImageVector = when (notification.type) {
        NotificationType.SUCCESS -> Icons.Outlined.CheckCircle
        NotificationType.WARNING -> Icons.Outlined.Warning
        NotificationType.ERROR -> Icons.Outlined.Error
        NotificationType.INFO -> Icons.Outlined.Info
    }

    val backgroundColor = when (notification.type) {
        NotificationType.SUCCESS -> MaterialTheme.colorScheme.primaryContainer
        NotificationType.WARNING -> MaterialTheme.colorScheme.tertiaryContainer
        NotificationType.ERROR -> MaterialTheme.colorScheme.errorContainer
        NotificationType.INFO -> MaterialTheme.colorScheme.secondaryContainer
    }

    val contentColor = when (notification.type) {
        NotificationType.SUCCESS -> MaterialTheme.colorScheme.onPrimaryContainer
        NotificationType.WARNING -> MaterialTheme.colorScheme.onTertiaryContainer
        NotificationType.ERROR -> MaterialTheme.colorScheme.onErrorContainer
        NotificationType.INFO -> MaterialTheme.colorScheme.onSecondaryContainer
    }

    val accentColor = when (notification.type) {
        NotificationType.SUCCESS -> MaterialTheme.colorScheme.primary
        NotificationType.WARNING -> MaterialTheme.colorScheme.tertiary
        NotificationType.ERROR -> MaterialTheme.colorScheme.error
        NotificationType.INFO -> MaterialTheme.colorScheme.secondary
    }

    LaunchedEffect(notification) {
        progress = 0f
        visible = true
        delay(notification.duration)
        visible = false
        delay(300) 
        state.clearNotification()
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(400)) + slideInVertically(
            initialOffsetY = { -it },
            animationSpec = tween(500)
        ),
        exit = fadeOut(tween(300)) + slideOutVertically(
            targetOffsetY = { -it },
            animationSpec = tween(400)
        )
    ) {
        Surface(
            modifier = Modifier
                .padding(top = 16.dp, start = 16.dp, end = 16.dp)
                .width(320.dp),
            shape = RoundedCornerShape(12.dp),
            color = backgroundColor,
            shadowElevation = 4.dp
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(24.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Text(
                        text = notification.message,
                        color = contentColor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f)
                    )
                }
                
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp),
                    color = accentColor,
                    trackColor = accentColor.copy(alpha = 0.2f),
                    strokeCap = StrokeCap.Round
                )
            }
        }
    }
} 