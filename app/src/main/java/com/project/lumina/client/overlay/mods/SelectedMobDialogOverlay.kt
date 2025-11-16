package com.project.lumina.client.overlay.mods

import android.view.Gravity
import android.view.WindowManager
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.project.lumina.client.constructors.EntityStorage
import com.project.lumina.client.overlay.manager.OverlayManager
import com.project.lumina.client.overlay.manager.OverlayWindow
import com.project.lumina.client.ui.theme.*
import com.project.lumina.client.R
import kotlinx.coroutines.delay
import kotlin.math.sqrt

private val modernFont = FontFamily(Font(R.font.fredoka_light))

class SelectedMobDialogOverlay(
    private val entityInfo: EntityStorage.EntityInfo,
    private val playerPosition: org.cloudburstmc.math.vector.Vector3f,
    private val onAttack: (EntityStorage.EntityInfo) -> Unit,
    private val onFollow: (EntityStorage.EntityInfo) -> Unit,
    private val onSpectate: (EntityStorage.EntityInfo) -> Unit,
    private val onDismiss: () -> Unit,
    private val isAttacking: Boolean = false,
    private val isSpectating: Boolean = false
) : OverlayWindow() {

    private val _layoutParams by lazy {
        super.layoutParams.apply {
            flags = flags or
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
            width = WindowManager.LayoutParams.WRAP_CONTENT
            height = WindowManager.LayoutParams.WRAP_CONTENT
            gravity = Gravity.CENTER
        }
    }

    override val layoutParams: WindowManager.LayoutParams
        get() = _layoutParams

    @Composable
    override fun Content() {
        var isVisible by remember { mutableStateOf(false) }
        var shouldDismiss by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            delay(50)
            isVisible = true
            delay(10000)
            shouldDismiss = true
            delay(300)
            OverlayManager.dismissOverlayWindow(this@SelectedMobDialogOverlay)
        }
        
        LaunchedEffect(shouldDismiss) {
            if (shouldDismiss) {
                delay(300)
                OverlayManager.dismissOverlayWindow(this@SelectedMobDialogOverlay)
            }
        }

        val scale by animateFloatAsState(
            targetValue = if (isVisible && !shouldDismiss) 1f else 0.8f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            ),
            label = "dialogScale"
        )

        val alpha by animateFloatAsState(
            targetValue = if (isVisible && !shouldDismiss) 1f else 0f,
            animationSpec = tween(300),
            label = "dialogAlpha"
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(onClick = {
                    shouldDismiss = true
                }),
            contentAlignment = Alignment.Center
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = KitsuSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 16.dp),
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .fillMaxHeight(0.8f)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        this.alpha = alpha
                    }
                    .clickable(onClick = {})
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Selected Mob Detected!",
                            style = TextStyle(
                                fontSize = 16.sp,
                                fontFamily = modernFont,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF9B59B6)
                            )
                        )
                        Icon(
                            painter = painterResource(id = R.drawable.cross_circle_24),
                            contentDescription = "Close",
                            tint = KitsuOnSurfaceVariant,
                            modifier = Modifier
                                .size(24.dp)
                                .clickable {
                                    shouldDismiss = true
                                }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    MobImageBox(entityInfo = entityInfo)

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = entityInfo.name,
                        style = TextStyle(
                            fontSize = 18.sp,
                            fontFamily = modernFont,
                            fontWeight = FontWeight.SemiBold,
                            color = KitsuPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    val dx = entityInfo.coords.x - playerPosition.x
                    val dy = entityInfo.coords.y - playerPosition.y
                    val dz = entityInfo.coords.z - playerPosition.z
                    val distance = sqrt(dx * dx + dy * dy + dz * dz)
                    val direction = getCardinalDirection(dx, dz)

                    InfoRow("Distance", String.format("%.1f blocks", distance))
                    InfoRow("Direction", direction)
                    InfoRow("Height", entityInfo.relativeHeight)
                    InfoRow("Coordinates", "(${entityInfo.coords.x.toInt()}, ${entityInfo.coords.y.toInt()}, ${entityInfo.coords.z.toInt()})")

                    Spacer(modifier = Modifier.height(20.dp))

                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        ActionButton(
                            text = if (isAttacking) "Stop Attacking" else "Attack",
                            icon = if (isAttacking) R.drawable.cross_circle_24 else R.drawable.sword_24,
                            color = if (isAttacking) Color(0xFFFF5252) else Color(0xFFE81123),
                            onClick = {
                                onAttack(entityInfo)
                                shouldDismiss = true
                            }
                        )

                        ActionButton(
                            text = "Follow",
                            icon = R.drawable.moon_stars_24,
                            color = KitsuPrimary,
                            onClick = {
                                onFollow(entityInfo)
                                shouldDismiss = true
                            }
                        )

                        ActionButton(
                            text = if (isSpectating) "Stop Spectating" else "Spectate",
                            icon = if (isSpectating) R.drawable.cross_circle_24 else R.drawable.eye_24,
                            color = if (isSpectating) Color(0xFFFF5252) else Color(0xFF9B59B6),
                            onClick = {
                                onSpectate(entityInfo)
                                shouldDismiss = true
                            }
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun MobImageBox(entityInfo: EntityStorage.EntityInfo) {
        val context = androidx.compose.ui.platform.LocalContext.current
        val resourceId = remember(entityInfo.imagePath) {
            if (entityInfo.imagePath != null) {
                try {
                    context.resources.getIdentifier(
                        entityInfo.imagePath.substringBeforeLast("."),
                        "drawable",
                        context.packageName
                    )
                } catch (e: Exception) {
                    0
                }
            } else {
                0
            }
        }

        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(KitsuSurfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            if (resourceId != 0) {
                Image(
                    painter = painterResource(id = resourceId),
                    contentDescription = entityInfo.name,
                    modifier = Modifier.size(64.dp)
                )
            } else {
                PlaceholderIcon()
            }
        }
    }

    @Composable
    private fun PlaceholderIcon() {
        Icon(
            painter = painterResource(id = R.drawable.moon_stars_24),
            contentDescription = "No Image",
            tint = KitsuOnSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.size(48.dp)
        )
    }

    @Composable
    private fun InfoRow(label: String, value: String) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = TextStyle(
                    fontSize = 13.sp,
                    fontFamily = modernFont,
                    color = KitsuOnSurfaceVariant
                )
            )
            Text(
                text = value,
                style = TextStyle(
                    fontSize = 13.sp,
                    fontFamily = modernFont,
                    fontWeight = FontWeight.SemiBold,
                    color = KitsuOnSurface
                )
            )
        }
    }

    @Composable
    private fun ActionButton(
        text: String,
        icon: Int,
        color: Color,
        onClick: () -> Unit
    ) {
        Button(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = color,
                contentColor = Color.White
            )
        ) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                style = TextStyle(
                    fontSize = 15.sp,
                    fontFamily = modernFont,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }

    private fun getCardinalDirection(dx: Float, dz: Float): String {
        val angle = Math.toDegrees(kotlin.math.atan2(dz.toDouble(), dx.toDouble()))
        val normalized = (angle + 360) % 360

        return when {
            normalized < 22.5 || normalized >= 337.5 -> "East"
            normalized < 67.5 -> "Southeast"
            normalized < 112.5 -> "South"
            normalized < 157.5 -> "Southwest"
            normalized < 202.5 -> "West"
            normalized < 247.5 -> "Northwest"
            normalized < 292.5 -> "North"
            else -> "Northeast"
        }
    }
}