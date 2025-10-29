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
import android.graphics.PixelFormat
import androidx.compose.animation.core.*
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.project.lumina.client.overlay.manager.OverlayManager
import com.project.lumina.client.overlay.manager.OverlayWindow
import kotlinx.coroutines.delay

class OverlayNotification : OverlayWindow() {
    private val _layoutParams by lazy {
        WindowManager.LayoutParams().apply {
            flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE

            type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY

            width = WindowManager.LayoutParams.WRAP_CONTENT
            height = WindowManager.LayoutParams.WRAP_CONTENT
            gravity = Gravity.BOTTOM or Gravity.END
            x = 12
            y = 12
            format = PixelFormat.TRANSLUCENT
        }
    }

    override val layoutParams: WindowManager.LayoutParams
        get() = _layoutParams

    companion object {
        private val notificationState = NotificationState()
        private var isOverlayShowing = false

        fun addNotification(moduleName: String) {
            notificationState.addNotification(moduleName)
            if (!isOverlayShowing) {
                try {
                    OverlayManager.showOverlayWindow(OverlayNotification())
                    isOverlayShowing = true
                } catch (e: Exception) {
                    
                }
            }
        }

        fun onOverlayDismissed() {
            isOverlayShowing = false
        }
    }

    @Composable
    override fun Content() {
        val notifications = notificationState.notifications

        LaunchedEffect(notifications.size) {
            if (notifications.isEmpty()) {
                delay(200)
                OverlayManager.dismissOverlayWindow(this@OverlayNotification)
                onOverlayDismissed()
            }
        }

        Box(
            modifier = Modifier
                .wrapContentSize()
                .padding(0.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            
            Column(
                modifier = Modifier
                    .wrapContentSize()
                    .padding(end = 8.dp, bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = Alignment.End
            ) {
                
                notifications.take(notificationState.maxNotifications).forEach { notification ->
                    key(notification.id) { 
                        NotificationCard(
                            notification = notification,
                            notificationState = notificationState
                        )
                    }
                }
            }
        }
    }
}

data class NotificationItem(
    val id: Int,
    val moduleName: String,
    val duration: Float = 0.8f 
)

@Composable
fun NotificationCard(
    notification: NotificationItem,
    notificationState: NotificationState
) {
    
    var timeShown by remember(notification.id) { mutableFloatStateOf(0f) }
    var isVisible by remember(notification.id) { mutableStateOf(false) }
    var shouldExit by remember(notification.id) { mutableStateOf(false) }

    val frameTime = 16L 

    
    val themeColors = remember(notification.id) {
        val baseHue = (notification.id * 137.5f) % 360f
        Pair(
            Color.hsv(baseHue, 0.8f, 0.95f, 0.85f),
            Color.hsv((baseHue + 25f) % 360f, 0.7f, 0.9f, 0.85f)
        )
    }

    
    val progressPercent = (timeShown / notification.duration).coerceIn(0f, 1f)

    
    LaunchedEffect(notification.id) {
        
        isVisible = true

        
        val startTime = System.currentTimeMillis()
        while (timeShown < notification.duration && !shouldExit) {
            delay(frameTime)
            val elapsed = (System.currentTimeMillis() - startTime) / 1000f
            timeShown = elapsed
        }

        
        shouldExit = true
        delay(150)
        notificationState.removeNotification(notification.id)
    }

    
    AnimatedVisibility(
        visible = isVisible && !shouldExit,
        enter = fadeIn(
            animationSpec = tween(150, easing = FastOutSlowInEasing)
        ) + scaleIn(
            initialScale = 0.95f,
            animationSpec = tween(150, easing = FastOutSlowInEasing)
        ),
        exit = fadeOut(
            animationSpec = tween(150, easing = LinearOutSlowInEasing)
        ) + scaleOut(
            targetScale = 0.95f,
            animationSpec = tween(150, easing = FastOutLinearInEasing)
        )
    ) {
        CompactNotificationContent(
            notification = notification,
            progressPercent = progressPercent,
            themeColor = themeColors.first,
            gradientEndColor = themeColors.second
        )
    }
}

@Composable
private fun CompactNotificationContent(
    notification: NotificationItem,
    progressPercent: Float,
    themeColor: Color,
    gradientEndColor: Color,
    modifier: Modifier = Modifier
) {
    val compactWidth = 120.dp
    val compactHeight = 24.dp

    Box(
        modifier = modifier
            .width(compactWidth)
            .height(compactHeight)
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(12.dp),
                ambientColor = themeColor.copy(alpha = 0.25f),
                spotColor = themeColor.copy(alpha = 0.4f)
            )
    ) {
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF1A1A1A).copy(alpha = 0.95f))
        )

        
        Box(
            modifier = Modifier
                .fillMaxWidth(progressPercent)
                .fillMaxHeight()
                .clip(RoundedCornerShape(12.dp))
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            themeColor.copy(alpha = 0.3f),
                            gradientEndColor.copy(alpha = 0.2f)
                        )
                    )
                )
        )

        
        Box(
            modifier = Modifier
                .fillMaxWidth(progressPercent)
                .height(2.dp)
                .align(Alignment.BottomStart)
                .clip(RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp))
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(themeColor, gradientEndColor)
                    )
                )
        )

        
        Text(
            text = notification.moduleName,
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        )

        
        Box(
            modifier = Modifier
                .size(4.dp)
                .align(Alignment.CenterEnd)
                .padding(end = 6.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(themeColor.copy(alpha = 0.8f))
        )
    }
}

class NotificationState {
    private val _notifications = mutableStateListOf<NotificationItem>()
    val notifications: List<NotificationItem> get() = _notifications

    val maxNotifications = 3
    private var nextId = 0
    private val activeModules = mutableMapOf<String, Long>()
    private val minCooldownMs = 100L

    fun addNotification(moduleName: String) {
        val currentTime = System.currentTimeMillis()

        
        val lastAdded = activeModules[moduleName]
        if (lastAdded != null && (currentTime - lastAdded) < minCooldownMs) {
            return
        }

        activeModules[moduleName] = currentTime

        
        while (_notifications.size >= maxNotifications) {
            val oldest = _notifications.firstOrNull()
            oldest?.let { removeNotification(it.id) }
        }

        
        _notifications.add(NotificationItem(nextId++, moduleName))
    }

    fun removeNotification(id: Int) {
        _notifications.removeAll { it.id == id }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun CompactNotificationPreview() {
    val sampleNotification = NotificationItem(id = 1, moduleName = "Scaffold")

    Column(
        modifier = Modifier
            .background(Color.Black)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        CompactNotificationContent(
            notification = sampleNotification.copy(moduleName = "KillAura"),
            progressPercent = 0.3f,
            themeColor = Color(0xFF8B5CF6),
            gradientEndColor = Color(0xFFEC4899)
        )

        CompactNotificationContent(
            notification = sampleNotification.copy(moduleName = "Speed"),
            progressPercent = 0.7f,
            themeColor = Color(0xFF10B981),
            gradientEndColor = Color(0xFF3B82F6)
        )

        CompactNotificationContent(
            notification = sampleNotification.copy(moduleName = "Fly"),
            progressPercent = 0.9f,
            themeColor = Color(0xFFF59E0B),
            gradientEndColor = Color(0xFFEF4444)
        )
    }
}