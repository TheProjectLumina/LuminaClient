/*
 * © Project Lumina 2025 — Licensed under GNU GPLv3
 * You are free to use, modify, and redistribute this code under the terms
 * of the GNU General Public License v3. See the LICENSE file for details.
 */

package com.project.lumina.client.overlay.mods

import android.graphics.Bitmap
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.project.lumina.client.ui.theme.MyFontFamily
import kotlinx.coroutines.delay
import androidx.compose.ui.tooling.preview.Preview
import com.project.lumina.client.overlay.manager.OverlayManager
import com.project.lumina.client.overlay.manager.OverlayWindow

class TargetHudOverlay : OverlayWindow() {
    private val _layoutParams by lazy {
        super.layoutParams.apply {
            flags = flags or
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
            width = WindowManager.LayoutParams.WRAP_CONTENT
            height = WindowManager.LayoutParams.WRAP_CONTENT
            gravity = Gravity.CENTER
            x = 80 
            y = 60 
        }
    }

    override val layoutParams: WindowManager.LayoutParams
        get() = _layoutParams

    companion object {
        private val overlayInstance by lazy { TargetHudOverlay() }
        private var isVisible = false
        private const val DISPLAY_DURATION = 3000L 

        
        private var targetUsername by mutableStateOf("")
        private var targetImage by mutableStateOf<Bitmap?>(null)
        private var targetDistance by mutableStateOf(0f)
        private var targetHealth by mutableStateOf(1f) 
        private var targetMaxHealth by mutableStateOf(20f)
        private var targetAbsorption by mutableStateOf(0f)
        private var targetMaxAbsorption by mutableStateOf(20f)
        private var targetHurtTime by mutableStateOf(0f)

        fun showTargetHud(
            username: String,
            image: Bitmap?,
            distance: Float,
            maxDistance: Float = 50f, 
            hurtTime: Float = 0f
        ) {
            targetUsername = username
            targetImage = image
            targetDistance = distance.coerceIn(0f, maxDistance)
            targetMaxHealth = maxDistance
            targetHealth = distance
            targetAbsorption = 0f
            targetMaxAbsorption = 20f
            targetHurtTime = hurtTime

            if (!isVisible) {
                isVisible = true
                try {
                    OverlayManager.showOverlayWindow(overlayInstance)
                } catch (e: Exception) {
                    
                }
            }
        }

        fun dismissTargetHud() {
            isVisible = false
            try {
                OverlayManager.dismissOverlayWindow(overlayInstance)
            } catch (e: Exception) {
                
            }
        }

        fun isTargetHudVisible(): Boolean = isVisible
    }

    @Composable
    override fun Content() {
        var visible by remember { mutableStateOf(false) }

        
        LaunchedEffect(isVisible) {
            if (isVisible) {
                visible = true
                delay(DISPLAY_DURATION)
                visible = false
                delay(300) 
                dismissTargetHud()
            }
        }

        AnimatedVisibility(
            visible = visible && isVisible,
            enter = fadeIn(
                animationSpec = tween(durationMillis = 200)
            ),
            exit = fadeOut(
                animationSpec = tween(durationMillis = 200)
            )
        ) {
            TargetHudContent(
                username = targetUsername,
                image = targetImage,
                distance = targetDistance,
                health = targetHealth,
                maxHealth = targetMaxHealth,
                absorption = targetAbsorption,
                maxAbsorption = targetMaxAbsorption,
                hurtTime = targetHurtTime,
                fontFamily = MyFontFamily
            )
        }
    }

    @Composable
    private fun TargetHudContent(
        username: String,
        image: Bitmap?,
        distance: Float,
        health: Float, 
        maxHealth: Float, 
        absorption: Float, 
        maxAbsorption: Float, 
        hurtTime: Float,
        fontFamily: FontFamily
    ) {
        
        val animatedDistance by animateFloatAsState(
            targetValue = distance,
            animationSpec = tween(durationMillis = 600, easing = EaseOutCubic),
            label = "distance_animation"
        )

        
        val hurtScale by animateFloatAsState(
            targetValue = if (hurtTime > 0) 0.85f else 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessHigh
            ),
            label = "hurt_scale"
        )

        val hurtAlpha by animateFloatAsState(
            targetValue = if (hurtTime > 0) 0.7f else 1f,
            animationSpec = tween(durationMillis = 200),
            label = "hurt_alpha"
        )

        
        val baseHue = remember(username) {
            val charCount = username.length
            val charSum = username.sumOf { it.code }
            
            ((charCount * 137.5f + charSum * 31.7f) % 360f).coerceIn(0f, 360f)
        }
        val themeColors = remember(username) {
            Pair(
                Color.hsv(baseHue, 0.8f, 1.0f, 0.5f), 
                Color.hsv((baseHue + 25f) % 360f, 0.7f, 1.0f, 0.4f) 
            )
        }
        
        val statusColor = remember(username) {
            Color.hsv((baseHue + 50f) % 360f, 0.75f, 1.0f, 0.9f) 
        }
        
        val backgroundColor = Color(0xFF151515).copy(0.6f)

        
        Box(
            modifier = Modifier
                .width(190.dp)
                .height(70.dp)
                .background(
                    color = backgroundColor,
                    shape = RoundedCornerShape(15.dp)
                )

                .padding(horizontal = 10.dp, vertical = 8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .scale(hurtScale)
                        .alpha(hurtAlpha)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.05f))
                        .border(
                            2.dp,
                            Color.White.copy(alpha = 0.2f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (image != null) {
                        Image(
                            bitmap = image.asImageBitmap(),
                            contentDescription = "Player Avatar",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        
                        Text(
                            text = username.take(2).uppercase(),
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = fontFamily
                        )
                    }
                }

                
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    
                    Text(
                        text = username,
                        color = Color.White.copy(alpha = 1.0f), 
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = fontFamily,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    
                    Column(
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(5.dp))
                                .background(Color.Black.copy(alpha = 0.4f))
                              /*  .border(
                                    1.dp,
                                    Color.White.copy(alpha = 0.1f),
                                    RoundedCornerShape(5.dp)
                                )*/
                        ) {
                            
                            val distancePercentage = (1f - (animatedDistance / maxHealth)).coerceIn(0f, 1f)

                            
                            if (distancePercentage > 0f) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(distancePercentage)
                                        .fillMaxHeight()
                                        .clip(RoundedCornerShape(5.dp))
                                        .background(
                                            brush = Brush.horizontalGradient(
                                                colors = listOf(
                                                    themeColors.first,
                                                    themeColors.second
                                                )
                                            )
                                        )
                                )
                            }

                            
                           /** Box(
                                modifier = Modifier
                                    .fillMaxWidth(distancePercentage)
                                    .height(2.dp)
                                    .align(Alignment.BottomStart)
                                    .clip(RoundedCornerShape(bottomStart = 5.dp, bottomEnd = 5.dp))
                                    .background(
                                        brush = Brush.horizontalGradient(
                                            colors = listOf(
                                                themeColors.first.copy(alpha = 1.0f),
                                                themeColors.second.copy(alpha = 1.0f)
                                            )
                                        )
                                    )
                            )*/
                        }

                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            
                            Text(
                                text = "${String.format("%.1f", animatedDistance)}m",
                                color = themeColors.first.copy(alpha = 1.0f), 
                                fontSize = 12.sp,
                                fontFamily = fontFamily,
                                fontWeight = FontWeight.Bold
                            )

                            
                            Text(
                                text = when {
                                    animatedDistance <= 5f -> "DANGER"
                                    animatedDistance <= 15f -> "CLOSE"
                                    animatedDistance <= 30f -> "MEDIUM"
                                    else -> "FAR"
                                },
                                color = statusColor, 
                                fontSize = 10.sp,
                                fontFamily = fontFamily,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }

    
    @Preview(showBackground = true)
    @Composable
    private fun TargetHudContentPreview() {
        val defaultFontFamily = FontFamily.Default
        TargetHudContent(
            username = "PlayerName123",
            image = null,
            distance = 10.5f,
            health = 10.5f, 
            maxHealth = 50f, 
            absorption = 0f, 
            maxAbsorption = 20f, 
            hurtTime = 0f,
            fontFamily = defaultFontFamily
        )
    }

    @Preview(showBackground = false)
    @Composable
    private fun TargetHudContentClosePreview() {
        val defaultFontFamily = FontFamily.Default
        TargetHudContent(
            username = "Enemy",
            image = null,
            distance = 3.2f,
            health = 3.2f, 
            maxHealth = 50f, 
            absorption = 0f, 
            maxAbsorption = 20f, 
            hurtTime = 5f, 
            fontFamily = defaultFontFamily
        )
    }
}