package com.project.lumina.client.router.launch

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.painterResource
import com.project.lumina.client.R

private val AccentColor = Color.Black 
private val SecondaryAccent = Color.Gray 
private val BackgroundColor = Color.White 
private val TextColor = Color.Black 

@Composable
fun PreloaderAnimation() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LogoAnimation()
            Spacer(modifier = Modifier.height(32.dp))
            ProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            LoadingText()
        }
    }
}

@Composable
fun LogoAnimation() {
    val infiniteTransition = rememberInfiniteTransition(label = "logo_animations")

    val scale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = EaseInOutQuart),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing)
        ),
        label = "rotation"
    )

    Box(
        modifier = Modifier
            .size(80.dp)
            .clip(CircleShape)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                //rotationZ = rotation
            },
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(R.drawable.logo),
            contentDescription = "Logo",
            modifier = Modifier.size(90.dp)
        )
    }
}

@Composable
fun ProgressIndicator() {
    val infiniteTransition = rememberInfiniteTransition(label = "progress")
    val progressAnimation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3500, easing = EaseInOut) 
        ),
        label = "progress_value"
    )

    val shimmerAnimation by infiniteTransition.animateFloat(
        initialValue = -1000f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(3500, easing = LinearEasing) 
        ),
        label = "shimmer"
    )

    Box(
        modifier = Modifier
            .width(200.dp)
            .height(8.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(AccentColor.copy(alpha = 0.2f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(200.dp * progressAnimation)
                .clip(RoundedCornerShape(4.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            AccentColor,
                            SecondaryAccent,
                            AccentColor
                        ),
                        start = Offset(shimmerAnimation - 200, 0f),
                        end = Offset(shimmerAnimation, 0f)
                    )
                )
        )
    }
}

@Composable
fun LoadingText() {
    val infiniteTransition = rememberInfiniteTransition(label = "loading_text")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        ),
        label = "text_alpha"
    )

    Text(
        text = "Loading",
        color = TextColor.copy(alpha = alpha),
        fontSize = 16.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 1.sp
    )
}

private val EaseInOutQuart = CubicBezierEasing(0.76f, 0f, 0.24f, 1f)
private val EaseInOutQuad = CubicBezierEasing(0.45f, 0f, 0.55f, 1f)