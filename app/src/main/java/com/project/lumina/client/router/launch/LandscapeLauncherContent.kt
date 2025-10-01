package com.project.lumina.client.router.launch

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Link
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.window.layout.WindowMetricsCalculator
import com.project.lumina.client.activity.MainActivity
import com.project.lumina.client.activity.MinecraftCheckActivity
import com.project.lumina.client.activity.RemoteLinkActivity
import com.project.lumina.client.activity.startActivityWithTransition
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun LandscapeLauncherContent() {
    var showTitle by remember { mutableStateOf(false) }
    var titleMovedUp by remember { mutableStateOf(false) }
    var showCards by remember { mutableStateOf(false) }
    var loadingCard by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(Unit) {
        delay(300)
        showTitle = true
        delay(1000)
        titleMovedUp = true
        delay(600)
        showCards = true
    }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val titleOffsetY = animateFloatAsState(
        targetValue = if (titleMovedUp) 40f else 300f,
        animationSpec = tween(800, easing = EaseOutQuart),
        label = "titleOffset"
    )
    val windowMetrics = remember {
        WindowMetricsCalculator.getOrCreate().computeCurrentWindowMetrics(context)
    }
    val screenHeight = with(LocalDensity.current) { windowMetrics.bounds.height().toDp() }
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedVisibility(
                visible = showTitle,
                enter = fadeIn(animationSpec = tween(1000)) +
                        expandVertically(animationSpec = tween(800, easing = EaseOutQuart))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer {
                            translationY = titleOffsetY.value
                        }
                        .padding(top = if (titleMovedUp) 16.dp else screenHeight * 0.2f)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "PROJECT LUMINA",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White,
                            textAlign = TextAlign.Center,

                            )
                        if (titleMovedUp) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Select Mode",
                                fontSize = 16.sp,
                                color = Color.White.copy(alpha = 0.7f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(if (titleMovedUp) 32.dp else 0.dp))
            AnimatedVisibility(
                visible = showCards,
                enter = fadeIn(tween(500)) +
                        scaleIn(initialScale = 0.8f, animationSpec = tween(500, easing = EaseOutBack))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    LauncherCard(
                        title = "Client Mode",
                        description = "Lumina For Mobile",
                        icon = Icons.Filled.Dashboard,
                        isLoading = loadingCard == "main",
                        onClick = {
                            coroutineScope.launch {
                                loadingCard = "main"
                                startActivityWithTransition(context, MinecraftCheckActivity::class.java)
                            }
                        }
                    )
                    LauncherCard(
                        title = "Remote Link",
                        description = "Connect to external systems",
                        icon = Icons.Filled.Link,
                        isLoading = loadingCard == "remote",
                        onClick = {
                            coroutineScope.launch {
                                loadingCard = "remote"
                                startActivityWithTransition(context, RemoteLinkActivity::class.java)
                            }
                        }
                    )
                }
            }
        }
    }
}

private val EaseOutQuart = CubicBezierEasing(0.25f, 1f, 0.5f, 1f)
private val EaseOutBack = CubicBezierEasing(0.34f, 1.56f, 0.64f, 1f)
private val EaseInOutQuad = CubicBezierEasing(0.45f, 0.05f, 0.55f, 0.95f)