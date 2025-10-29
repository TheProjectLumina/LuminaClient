package com.project.lumina.client.overlay.mods

import android.view.Gravity
import android.view.WindowManager
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.project.lumina.client.constructors.NetBound
import com.project.lumina.client.overlay.manager.OverlayManager
import com.project.lumina.client.overlay.manager.OverlayWindow
import com.project.lumina.client.ui.theme.SMeterAccent
import com.project.lumina.client.ui.theme.SMeterBase
import com.project.lumina.client.ui.theme.SMeterBg
import com.project.lumina.client.ui.theme.SMiniLineGrpah


import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.cloudburstmc.math.vector.Vector3f
import java.util.ArrayDeque
import kotlin.math.sqrt


data class LineData(val x: String, val y: Float)

@Composable
fun MiniLineGraph(
    modifier: Modifier = Modifier,
    data: List<LineData>,
    lineColor: Color = Color(0xFF4B7BFF)
) {
    val transition = rememberInfiniteTransition(label = "graphTransition")
    val shimmerOffset by transition.animateFloat(
        initialValue = -500f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer"
    )

    Canvas(modifier = modifier) {
        if (data.isEmpty()) return@Canvas

        val minY = data.minOfOrNull { it.y } ?: 0f
        val maxY = data.maxOfOrNull { it.y }?.coerceAtLeast(minY + 1f) ?: 1f
        val yRange = maxY - minY
        val stepX = size.width / (data.size - 1).coerceAtLeast(1)

        
        drawRect(
            brush = Brush.linearGradient(
                colors = listOf(
                    Color(0xFF4B7BFF).copy(alpha = 0f),
                    Color(0xFF4B7BFF).copy(alpha = 0.1f),
                    Color(0xFF4B7BFF).copy(alpha = 0f)
                ),
                start = Offset(shimmerOffset, 0f),
                end = Offset(shimmerOffset + 100f, size.height)
            )
        )

        
        val path = Path()
        val fillPath = Path()

        data.forEachIndexed { index, point ->
            val x = index * stepX
            val y = size.height - ((point.y - minY) / yRange) * size.height

            if (index == 0) {
                path.moveTo(x, y)
                fillPath.moveTo(x, size.height)
                fillPath.lineTo(x, y)
            } else {
                path.lineTo(x, y)
                fillPath.lineTo(x, y)
            }

            if (index == data.size - 1) {
                fillPath.lineTo(x, size.height)
                fillPath.close()
            }
        }

        
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    lineColor.copy(alpha = 0.15f),
                    lineColor.copy(alpha = 0.0f)
                )
            )
        )

        
        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(
                width = 2.dp.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
    }
}

class SpeedometerOverlay : OverlayWindow() {
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
            x = 5
            y = 50
        }
    }

    override val layoutParams: WindowManager.LayoutParams
        get() = _layoutParams

    companion object {
        private var overlayInstance: SpeedometerOverlay? = null
        private var shouldShowOverlay = false
        private var lastPosition: Vector3f? = null
        private var lastUpdateTime: Long = 0L
        private val scope = CoroutineScope(Dispatchers.Main)
        private var currentSpeed by mutableStateOf(0.0f)
        private val speedHistory = ArrayDeque<Double>(5)
        private val graphData = ArrayDeque<LineData>(15)
        private var averageSpeed by mutableStateOf(0.0f)
        private const val SMOOTHING_ENABLED = true
        private const val MAX_SPEED = 50f
        private const val DEFAULT_SPEED = 1f

        private fun getDefaultGraphData(): List<LineData> {
            return List(15) { index ->
                LineData(
                    x = (index + 1).toString(),
                    y = DEFAULT_SPEED
                )
            }
        }

        fun showOverlay() {
            if (shouldShowOverlay) {
                try {
                    overlayInstance = overlayInstance ?: SpeedometerOverlay()
                    OverlayManager.showOverlayWindow(overlayInstance!!)
                } catch (e: Exception) {}
            }
        }

        fun dismissOverlay() {
            try {
                overlayInstance?.let { OverlayManager.dismissOverlayWindow(it) }
            } catch (e: Exception) {}
        }

        fun setOverlayEnabled(enabled: Boolean, NetBound: NetBound? = null) {
            shouldShowOverlay = enabled
            if (enabled && NetBound != null) showOverlay() else dismissOverlay()
        }

        fun updatePosition(position: Vector3f) {
            val currentTime = System.currentTimeMillis()
            lastPosition?.let { lastPos ->
                if (lastUpdateTime > 0L) {
                    val deltaTime = (currentTime - lastUpdateTime) / 1000f
                    if (deltaTime < 0.05f) return
                    val dx = position.x - lastPos.x
                    val dz = position.z - lastPos.z
                    val distance = sqrt(dx * dx + dz * dz)
                    if (distance.isNaN() || distance.isInfinite()) return
                    val instantSpeed = distance / deltaTime
                    if (instantSpeed.isNaN() || instantSpeed.isInfinite()) return

                    val smoothedSpeed = if (SMOOTHING_ENABLED) {
                        speedHistory.addLast(instantSpeed.toDouble())
                        if (speedHistory.size > 5) {
                            speedHistory.removeFirst()
                        }
                        val sortedSpeeds = speedHistory.sorted()
                        if (sortedSpeeds.size >= 3) {
                            sortedSpeeds.subList(1, sortedSpeeds.size - 1).average()
                        } else {
                            sortedSpeeds.average()
                        }
                    } else {
                        instantSpeed.toDouble()
                    }
                    if (smoothedSpeed.isNaN() || smoothedSpeed.isInfinite()) return
                    currentSpeed = smoothedSpeed.toFloat()

                    val cappedSpeed = currentSpeed.coerceAtMost(MAX_SPEED)

                    val dataPoint = LineData(
                        x = (graphData.size + 1).toString(),
                        y = cappedSpeed
                    )
                    graphData.addLast(dataPoint)
                    if (graphData.size > 15) {
                        graphData.removeFirst()
                    }

                    averageSpeed = if (graphData.isNotEmpty()) {
                        graphData.map { it.y }.average().toFloat().takeIf { !it.isNaN() } ?: 0.0f
                    } else {
                        0.0f
                    }
                }
            }
            lastPosition = position
            lastUpdateTime = currentTime
        }
    }

    @Composable
    override fun Content() {
        if (!shouldShowOverlay) return

        var visible by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            visible = true
        }

        val scale by animateFloatAsState(
            targetValue = if (visible) 1f else 0.8f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            ),
            label = "scale"
        )

        val alpha by animateFloatAsState(
            targetValue = if (visible) 1f else 0f,
            animationSpec = tween(300),
            label = "alpha"
        )

        Box(
            modifier = Modifier
                .scale(scale)
                .alpha(alpha)
        ) {
            CompactSpeedometerDisplay(currentSpeed, graphData.toList(), averageSpeed)
        }
    }

    @Composable
    private fun CompactSpeedometerDisplay(speed: Float, data: List<LineData>, avgSpeed: Float) {
        val baseColor = SMeterBase
        val accentColor = SMeterAccent
        val backgroundColor = SMeterBg

        
        val animatedSpeed by animateFloatAsState(
            targetValue = speed,
            animationSpec = spring(dampingRatio = 0.6f, stiffness = 120f),
            label = "speedAnimation"
        )

        
        val pulse = rememberInfiniteTransition(label = "pulseTransition")
        val pulseScale by pulse.animateFloat(
            initialValue = 1f,
            targetValue = 1.1f,
            animationSpec = infiniteRepeatable(
                animation = tween(500, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulseScale"
        )

        Row(
            modifier = Modifier
                .shadow(4.dp, RoundedCornerShape(12.dp), ambientColor = baseColor.copy(alpha = 0.3f))
                .clip(RoundedCornerShape(12.dp))
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            backgroundColor,
                            backgroundColor.copy(alpha = 0.95f)
                        )
                    )
                )
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                baseColor.copy(alpha = 0.2f),
                                backgroundColor.copy(alpha = 0.1f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                
                Box(
                    modifier = Modifier
                        .size(24.dp * pulseScale)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    accentColor.copy(alpha = 0.8f),
                                    accentColor.copy(alpha = 0f)
                                )
                            )
                        )
                )

                
                Text(
                    text = "%.1f".format(animatedSpeed),
                    style = TextStyle(
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            
            Spacer(modifier = Modifier.width(8.dp))

            
            Column(
                modifier = Modifier.width(90.dp),
                horizontalAlignment = Alignment.Start
            ) {
                
                val graphDataToShow = if (data.isNotEmpty() && data.all { !it.y.isNaN() && !it.y.isInfinite() }) {
                    data
                } else {
                    getDefaultGraphData()
                }

                MiniLineGraph(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(20.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(SMiniLineGrpah),
                    data = graphDataToShow,
                    lineColor = accentColor
                )

                
                Spacer(modifier = Modifier.height(2.dp))

                
                Text(
                    text = "Avg: ${String.format("%.1f", avgSpeed)} BPS",
                    style = TextStyle(
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        }
    }
}