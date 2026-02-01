package com.project.lumina.client.router.launch

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.project.lumina.client.ui.theme.LBg1
import com.project.lumina.client.ui.theme.LBg2
import com.project.lumina.client.ui.theme.LBlobColor1
import com.project.lumina.client.ui.theme.LBlobColor2
import kotlin.math.PI
import kotlin.math.sin

@Composable
fun AnimatedBackground(isPreloading: Boolean) {

    val transition = rememberInfiniteTransition(label = "background")
    val primaryPhase = transition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(30000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "primaryPhase"
    )


    val secondaryPhase = transition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(40000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "secondaryPhase"
    )


    val blurRadius = animateFloatAsState(
        targetValue = if (isPreloading) 2.5f else 1.5f,
        label = "blur"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind {

                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            LBg1,
                            LBg2
                        )
                    )
                )


                drawSmoothWaves(
                    primaryPhase = primaryPhase.value,
                    secondaryPhase = secondaryPhase.value,
                    isPreloading = isPreloading
                )
            }
            .blur(blurRadius.value.dp)
    )
}

private fun DrawScope.drawSmoothWaves(primaryPhase: Float, secondaryPhase: Float, isPreloading: Boolean) {

    val primaryColor = LBlobColor1.copy(alpha = 0.25f)
    val secondaryColor = LBlobColor2.copy(alpha = 0.2f)


    val segmentCount = 200


    drawWave(
        phase = primaryPhase,
        color = primaryColor,
        yPosition = size.height * 0.6f,
        amplitude = size.height * 0.03f,
        frequency = 3f,
        strokeWidth = 2.5f,
        segmentCount = segmentCount
    )


    drawWave(
        phase = secondaryPhase,
        color = secondaryColor,
        yPosition = size.height * 0.4f,
        amplitude = size.height * 0.02f,
        frequency = 4f,
        strokeWidth = 2f,
        segmentCount = segmentCount
    )


    if (!isPreloading) {
        drawWave(
            phase = primaryPhase + PI.toFloat() * 0.5f,
            color = primaryColor.copy(alpha = 0.15f),
            yPosition = size.height * 0.75f,
            amplitude = size.height * 0.015f,
            frequency = 5f,
            strokeWidth = 1.5f,
            segmentCount = segmentCount
        )
    }
}

private fun DrawScope.drawWave(
    phase: Float,
    color: Color,
    yPosition: Float,
    amplitude: Float,
    frequency: Float,
    strokeWidth: Float,
    segmentCount: Int
) {
    val path = Path()


    val segmentWidth = size.width / segmentCount


    path.moveTo(0f, yPosition + amplitude * sin(phase))

    for (i in 0 until segmentCount) {
        val x1 = i * segmentWidth
        val x2 = (i + 1) * segmentWidth

        val y1 = yPosition + amplitude * sin(x1 * (frequency / size.width) * 2 * PI.toFloat() + phase)
        val y2 = yPosition + amplitude * sin(x2 * (frequency / size.width) * 2 * PI.toFloat() + phase)


        val cx = (x1 + x2) / 2
        val cy = yPosition + amplitude * sin(cx * (frequency / size.width) * 2 * PI.toFloat() + phase)

        path.quadraticTo(cx, cy, x2, y2)
    }


    drawPath(
        path = path,
        color = color,
        style = Stroke(
            width = strokeWidth,
            cap = StrokeCap.Round
        )
    )
}