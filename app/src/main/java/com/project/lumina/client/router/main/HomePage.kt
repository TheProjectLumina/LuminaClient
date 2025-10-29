package com.project.lumina.client.router.main

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.EaseOutQuart
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import com.project.lumina.client.R
import com.project.lumina.client.overlay.mods.NotificationType
import com.project.lumina.client.overlay.mods.SimpleOverlayNotification
import com.project.lumina.client.service.Services
import com.project.lumina.client.util.LocalSnackbarHostState
import com.project.lumina.client.util.SnackbarHostStateScope
import com.project.lumina.client.viewmodel.MainScreenViewModel
import kotlinx.coroutines.delay
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class, ExperimentalAnimationApi::class)
@Composable
fun HomePageContent() {
    SnackbarHostStateScope {
        val context = LocalContext.current
        val coroutineScope = rememberCoroutineScope()
        val snackbarHostState = LocalSnackbarHostState.current
        val mainScreenViewModel: MainScreenViewModel = viewModel()
        val pages = listOf(R.string.home, R.string.about, R.string.realms, R.string.settings)
        var currentPage by rememberSaveable { mutableStateOf(R.string.home) }




        var uiAnimationState by remember { mutableStateOf(0) }
        var uiVisible by remember { mutableStateOf(false) }

        
        var showProgressDialog by remember { mutableStateOf(false) }
        var downloadProgress by remember { mutableStateOf(0f) }
        var currentPackName by remember { mutableStateOf("") }
        
        val sharedPreferences = context.getSharedPreferences("SettingsPrefs", Context.MODE_PRIVATE)
        var InjectNekoPack by remember {
            mutableStateOf(sharedPreferences.getBoolean("injectNekoPackEnabled", false))
        }
        var isConnected by remember { mutableStateOf(false) }

        var progress by remember { mutableFloatStateOf(0f) }

        
        val blurRadius by animateFloatAsState(
            targetValue = if (uiAnimationState > 0) 0f else 10f,
            animationSpec = tween(1000, easing = FastOutSlowInEasing),
            label = "blurAnimation"
        )
        
        val logoScale by animateFloatAsState(
            targetValue = if (uiAnimationState > 1) 1f else 0.6f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            ),
            label = "logoScale"
        )
        
        val contentAlpha by animateFloatAsState(
            targetValue = if (uiAnimationState > 2) 1f else 0f,
            animationSpec = tween(700, easing = FastOutSlowInEasing),
            label = "contentAlpha"
        )
        
        
        val parallaxOffset by animateFloatAsState(
            targetValue = if (uiAnimationState > 0) 0f else -50f,
            animationSpec = tween(1200, easing = EaseOutQuart),
            label = "parallaxOffset"
        )



        
        LaunchedEffect(Unit) {
            
            uiAnimationState = 0
            uiVisible = false
            
            
            delay(100)
            uiAnimationState = 1
            
            
            delay(300)
            uiAnimationState = 2
            
            
            delay(200)
            uiAnimationState = 3
            uiVisible = true
            
            
            delay(200)
            uiAnimationState = 4
        }

        val showNotification: (String, NotificationType) -> Unit = { message, type ->
            SimpleOverlayNotification.show(
                message = message,
                type = type,
                durationMs = 3000
            )
        }

        val onPostPermissionResult: (Boolean) -> Unit = block@{ isGranted: Boolean ->
            if (!isGranted) {
                showNotification(
                    context.getString(R.string.notification_permission_denied),
                    NotificationType.ERROR
                )
                return@block
            }

            if (mainScreenViewModel.selectedGame.value === null) {
                showNotification(
                    context.getString(R.string.select_game_first),
                    NotificationType.WARNING
                )
                return@block
            }

            val captureModeModel = mainScreenViewModel.captureModeModel.value
            Services.toggle(context, captureModeModel)
        }

        val postNotificationPermissionLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted -> onPostPermissionResult(isGranted) }
        val overlayPermissionLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (!Settings.canDrawOverlays(context)) {
                showNotification(
                    context.getString(R.string.overlay_permission_denied),
                    NotificationType.ERROR
                )
                return@rememberLauncherForActivityResult
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                postNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                return@rememberLauncherForActivityResult
            }
            onPostPermissionResult(true)
        }
        var isActiveBefore by rememberSaveable { mutableStateOf(Services.isActive) }

        LaunchedEffect(Services.isActive) {
            if (Services.isActive == isActiveBefore) {
                return@LaunchedEffect
            }

            isActiveBefore = Services.isActive
            isConnected = Services.isActive
            
            
            SimpleOverlayNotification.show(
                message = if (isConnected) 
                    context.getString(R.string.backend_connected) 
                else 
                    context.getString(R.string.backend_disconnected),
                type = if (isConnected) NotificationType.SUCCESS else NotificationType.ERROR,
                durationMs = 3000
            )
        }

        Scaffold(
            topBar = {
                AnimatedVisibility(
                    visible = uiVisible,
                    enter = fadeIn(animationSpec = tween(300)) +
                            slideInVertically(
                                initialOffsetY = { -it },
                                animationSpec = tween(300)
                            ),
                    modifier = Modifier.graphicsLayer {
                        translationY = parallaxOffset * 0.3f
                    }
                ) {
                    Column {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(64.dp)
                                .background(MaterialTheme.colorScheme.surface)
                        ) {
                            
                            Text(
                                "Lumina",
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.headlineMedium,
                                modifier = Modifier
                                    .align(Alignment.CenterStart)
                                    .padding(start = 20.dp)
                                    .graphicsLayer {
                                        
                                        val pulseFactor = if (uiAnimationState >= 4) {
                                            1f + (sin(System.currentTimeMillis() / 2000f) * 0.03f)
                                        } else 1f
                                        scaleX = pulseFactor * logoScale
                                        scaleY = pulseFactor * logoScale
                                    }
                            )

                            Row(
                                modifier = Modifier
                                    .align(Alignment.CenterEnd)
                                    .padding(end = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                pages.forEachIndexed { index, page ->
                                    AnimatedVisibility(
                                        visible = uiAnimationState >= 3,
                                        enter = fadeIn(animationSpec = tween(300, delayMillis = 100 * index)) +
                                                slideInHorizontally(
                                                    initialOffsetX = { it / 4 },
                                                    animationSpec = tween(300)
                                                ),
                                    ) {
                                        val selected = currentPage == page
                                        Card(
                                            modifier = Modifier
                                                .clickable { currentPage = page }
                                                .padding(horizontal = 4.dp, vertical = 4.dp)
                                                .graphicsLayer {
                                                    
                                                    if (selected) {
                                                        translationY = -2f
                                                        shadowElevation = 0f
                                                    }
                                                },
                                            colors = CardDefaults.cardColors(
                                                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0f),
                                            ),
                                            shape = MaterialTheme.shapes.small,
                                            border = null
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text(
                                                    text = stringResource(page),
                                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                                    color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                                                    style = MaterialTheme.typography.bodyMedium
                                                )
                                                
                                                if (selected) {
                                                    Box(
                                                        modifier = Modifier
                                                            .width(16.dp)
                                                            .height(2.dp)
                                                            .background(
                                                                color = MaterialTheme.colorScheme.primary,
                                                                shape = MaterialTheme.shapes.small
                                                            )
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
                                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f),
                                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
                                        ),
                                        startX = 0f + (System.currentTimeMillis() % 3000) / 3000f * 1000f,
                                        endX = 1000f + (System.currentTimeMillis() % 3000) / 3000f * 1000f
                                    )
                                )
                        )
                    }
                }
            },
            bottomBar = {
                SnackbarHost(
                    snackbarHostState,
                    modifier = Modifier
                        .animateContentSize()
                )
            },
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ) { padding ->
            Box(
                Modifier
                    .padding(padding)
                    .fillMaxSize()
            ) {
                
                AnimatedVisibility(
                    visible = uiAnimationState >= 3,
                    enter = fadeIn(animationSpec = tween(300)) +
                            slideInVertically(
                                initialOffsetY = { it / 2 },
                                animationSpec = tween(300)
                            ),
                    modifier = Modifier.graphicsLayer {
                        translationY = parallaxOffset * 0.5f
                        alpha = contentAlpha
                    }
                ) {
                    
                    AnimatedContent(
                        targetState = currentPage,
                        transitionSpec = {
                            
                            val initialIndex = when(initialState) {
                                R.string.home -> 0
                                R.string.about -> 1
                                R.string.realms -> 2
                                R.string.settings -> 3
                                else -> 0
                            }

                            val targetIndex = when(targetState) {
                                R.string.home -> 0
                                R.string.about -> 1
                                R.string.realms -> 2
                                R.string.settings -> 3
                                else -> 0
                            }

                            
                            val direction = if (targetIndex > initialIndex) 1 else -1

                            
                            if (direction > 0) {
                                
                                (slideInHorizontally(
                                    animationSpec = tween(300),
                                    initialOffsetX = { fullWidth -> fullWidth }
                                ) + fadeIn(
                                    animationSpec = tween(300)
                                )) togetherWith
                                    (slideOutHorizontally(
                                        animationSpec = tween(300),
                                        targetOffsetX = { fullWidth -> -fullWidth }
                                    ) + fadeOut(
                                        animationSpec = tween(300)
                                    ))
                            } else {
                                
                                (slideInHorizontally(
                                    animationSpec = tween(300),
                                    initialOffsetX = { fullWidth -> -fullWidth }
                                ) + fadeIn(
                                    animationSpec = tween(300)
                                )) togetherWith
                                    (slideOutHorizontally(
                                        animationSpec = tween(300),
                                        targetOffsetX = { fullWidth -> fullWidth }
                                    ) + fadeOut(
                                        animationSpec = tween(300)
                                    ))
                            }
                        },
                        label = "pageTransition"
                    ) { page ->
                        
                        when (page) {
                            R.string.home -> HomeScreen(onStartToggle = {
                                if (!Settings.canDrawOverlays(context)) {
                                    Toast.makeText(context, context.getString(R.string.request_overlay_permission), Toast.LENGTH_SHORT).show()
                                    overlayPermissionLauncher.launch(
                                        Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, "package:${context.packageName}".toUri())
                                    )
                                    return@HomeScreen
                                }

                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    postNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    return@HomeScreen
                                }

                                if (Services.isActive) {
                                    
                                    onPostPermissionResult(true)
                                    return@HomeScreen
                                }




                                onPostPermissionResult(true)
                            })
                            R.string.about -> AboutScreen()
                            R.string.realms -> RealmsScreen()
                            R.string.settings -> SettingsScreen()
                            else -> {}
                        }
                    }
                }



                if (showProgressDialog) {
                    Dialog(onDismissRequest = { /* Prevent dismissal during download */ }) {
                        Card(
                            modifier = Modifier
                                .padding(16.dp)
                                .wrapContentSize()
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Downloading: $currentPackName",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                CircularProgressIndicator(
                                    progress = { downloadProgress },
                                    modifier = Modifier.size(48.dp),
                                    trackColor = ProgressIndicatorDefaults.circularTrackColor,
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "${(downloadProgress * 100).toInt()}%",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = if (downloadProgress < 1f) "Downloading..." else "Launching Minecraft...",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/*
 * © Project Lumina 2025 — Licensed under GNU GPLv3
 * You are free to use, modify, and redistribute this code under the terms
 * of the GNU General Public License v3. See the LICENSE file for details.
 */