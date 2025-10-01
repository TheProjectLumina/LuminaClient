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

package com.project.lumina.client.router.main

import android.Manifest
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.project.lumina.client.R
import com.project.lumina.client.service.Services
import com.project.lumina.client.ui.component.ServerSelector
import com.project.lumina.client.viewmodel.MainScreenViewModel
import io.lumina.luminaux.components.*
import kotlinx.coroutines.delay
import kotlin.text.toIntOrNull
import com.project.lumina.client.overlay.mods.NotificationType

@Composable
private fun getAppVersion(context: android.content.Context): String {
    return try {
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        packageInfo.versionName ?: "Unknown"
    } catch (e: Exception) {
        "Unknown"
    }
}

@OptIn(ExperimentalAnimationApi::class)
private fun enterAnimation(delay: Int, duration: Int = 500) = fadeIn(
    animationSpec = tween(
        durationMillis = duration,
        delayMillis = delay,
        easing = FastOutSlowInEasing
    )
) + expandHorizontally(
    animationSpec = tween(
        durationMillis = duration,
        delayMillis = delay,
        easing = FastOutSlowInEasing
    )
)

@Composable
fun GameUI() {
    var uiVisible by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    var interactionTime by remember { mutableStateOf(0L) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val mainScreenViewModel: MainScreenViewModel = viewModel()
    var showConnectionNotification by remember { mutableStateOf(false) }
    var isConnected by remember { mutableStateOf(false) }
    var lastConnectionChangeTime by remember { mutableLongStateOf(0L) }
    val pages = listOf("Home", "About", "Settings")
    var currentPage by rememberSaveable { mutableStateOf("Home") }
    var selectedNavIndex by rememberSaveable { mutableStateOf(0) } 

    
    val backgroundBlurRadius by animateFloatAsState(
        targetValue = if (uiVisible) 2f else 0f,
        animationSpec = tween(800, easing = FastOutSlowInEasing),
        label = "backgroundBlur"
    )

    
    val infiniteTransition = rememberInfiniteTransition(label = "rippleEffect")
    val rippleScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rippleScale"
    )

    
    val floatOffsetLeft by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floatLeft"
    )

    val floatOffsetRight by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, delayMillis = 800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floatRight"
    )

    var showCustomNotification by remember { mutableStateOf(false) }
    var customNotificationMessage by remember { mutableStateOf("") }
    var customNotificationType by remember { mutableStateOf<NotificationType>(NotificationType.INFO) }
    var lastCustomNotificationTime by remember { mutableLongStateOf(0L) }
    val captureModeModel by mainScreenViewModel.captureModeModel.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    var serverHostName by remember { mutableStateOf(captureModeModel.serverHostName) }
    var serverPort by remember { mutableStateOf(captureModeModel.serverPort.toString()) }

    val onPostPermissionResult: (Boolean) -> Unit = block@{ isGranted: Boolean ->
        if (!isGranted) {
            Toast.makeText(context, "Please grant permissions", Toast.LENGTH_SHORT).show()
            return@block
        }

        if (mainScreenViewModel.selectedGame.value === null) {
            Toast.makeText(context, "Please choose a game", Toast.LENGTH_SHORT).show()
            return@block
        }

        if (!Services.isActive) {
            val captureModeModel = mainScreenViewModel.captureModeModel.value
            Services.toggle(context, captureModeModel)
            return@block
        }

        Services.toggle(context, mainScreenViewModel.captureModeModel.value)
    }

    val postNotificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted -> onPostPermissionResult(isGranted) }

    val overlayPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (!Settings.canDrawOverlays(context)) {
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
        showConnectionNotification = true
        lastConnectionChangeTime = System.currentTimeMillis()

        delay(5000)
        if (System.currentTimeMillis() - lastConnectionChangeTime >= 4500) {
            showConnectionNotification = false
        }
    }

    
    LaunchedEffect(interactionTime) {
        delay(3000) 
    }

    
    LaunchedEffect(Unit) {
        delay(200)
        uiVisible = true
        interactionTime = System.currentTimeMillis()
    }

    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures {
                    interactionTime = System.currentTimeMillis() 
                }
            }
    ) {

        Box(modifier = Modifier.fillMaxSize()) {
            VideoBackground(
            )

            
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(0.03f)
                    .scale(rippleScale)
                    .blur(50.dp)
            )
        }

        
        AnimatedVisibility(
            visible = uiVisible,
            enter = enterAnimation(delay = 250, duration = 700),
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(vertical = 48.dp, horizontal = 58.dp)
                .graphicsLayer { translationY = floatOffsetRight }
        ) {
            GlassmorphicCard2(
                modifier = Modifier
                    .fillMaxHeight(0.90f)
                    .width(220.dp)
            ) {
                ServerSelector()
            }
        }

        
        AnimatedVisibility(
            visible = uiVisible,
            enter = enterAnimation(delay = 100, duration = 700),
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(vertical = 48.dp, horizontal = 58.dp)
                .graphicsLayer { translationY = floatOffsetLeft }
        ) {
            GlassmorphicCard2(
                modifier = Modifier
                    .fillMaxHeight(0.90f)
                    .width(220.dp)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(16.dp)
                ) {
                    
                    AnimatedVisibility(
                        visible = uiVisible,
                        enter = fadeIn(animationSpec = tween(700, delayMillis = 400)) +
                                slideInVertically(
                                    initialOffsetY = { -40 },
                                    animationSpec = tween(700, delayMillis = 400)
                                )
                    ) {

                    }

                    
                    AnimatedVisibility(
                        visible = uiVisible,
                        enter = fadeIn(animationSpec = tween(700, delayMillis = 500)) +
                                slideInVertically(
                                    initialOffsetY = { 40 },
                                    animationSpec = tween(700, delayMillis = 500)
                                )
                    ) {
                        GlassmorphicOutlinedTextField(
                            value = serverHostName,
                            onValueChange = {
                                serverHostName = it
                                if (it.isNotEmpty()) {
                                    mainScreenViewModel.selectCaptureModeModel(
                                        captureModeModel.copy(serverHostName = it)
                                    )
                                }
                            },
                            label = "Server Address",
                            placeholder = "e.g., play.example.net",
                            singleLine = true,
                            enabled = !Services.isActive,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    AnimatedVisibility(
                        visible = uiVisible,
                        enter = fadeIn(animationSpec = tween(700, delayMillis = 600)) +
                                slideInVertically(
                                    initialOffsetY = { 40 },
                                    animationSpec = tween(700, delayMillis = 600)
                                )
                    ) {
                        GlassmorphicOutlinedTextField(
                            value = serverPort,
                            onValueChange = {
                                serverPort = it
                                val port = it.toIntOrNull()
                                if (port != null && port in 0..65535) {
                                    mainScreenViewModel.selectCaptureModeModel(
                                        captureModeModel.copy(serverPort = port)
                                    )
                                }
                            },
                            label = "Server Port",
                            placeholder = "e.g., 19132",
                            singleLine = true,
                            enabled = !Services.isActive,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        
        AnimatedVisibility(
            visible = uiVisible,
            enter = fadeIn(animationSpec = tween(600, delayMillis = 500)) +
                    slideInVertically(
                        initialOffsetY = { it / 3 },
                        animationSpec = tween(600, easing = FastOutSlowInEasing)
                    ),
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 20.dp, start = 16.dp, end = 16.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                
                AnimatedVisibility(
                    visible = uiVisible,
                    enter = fadeIn(animationSpec = tween(600, delayMillis = 700)),
                    modifier = Modifier.align(Alignment.BottomStart)
                ) {
                    Text(
                        text = "© Project Lumina 2025 | v${getAppVersion(context)}",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 9.sp
                    )
                }

                
                FlickeringStartButton(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .graphicsLayer {
                            alpha = if (uiVisible) 1f else 0f
                            scaleX = if (uiVisible) 1f else 0.8f
                            scaleY = if (uiVisible) 1f else 0.8f
                        },
                    onClick = {
                        if (!Settings.canDrawOverlays(context)) {
                            Toast.makeText(context, R.string.request_overlay_permission, Toast.LENGTH_SHORT).show()
                            overlayPermissionLauncher.launch(
                                Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, "package:${context.packageName}".toUri())
                            )
                            return@FlickeringStartButton
                        }

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            postNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            return@FlickeringStartButton
                        }

                        onPostPermissionResult(true)
                    }
                )

                
                AnimatedVisibility(
                    visible = uiVisible,
                    enter = fadeIn(animationSpec = tween(600, delayMillis = 700)),
                    modifier = Modifier.align(Alignment.BottomEnd)
                ) {
                    Text(
                        text = "The Game Ends When You Give Up",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 9.sp
                    )
                }
            }
        }

        
        AnimatedVisibility(
            visible = uiVisible,
            enter = fadeIn(animationSpec = tween(600, delayMillis = 800)) +
                    slideInHorizontally(
                        initialOffsetX = { it / 4 },
                        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy)
                    ),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 10.dp, bottom = 50.dp)
        ) {
            GlassmorphicFloatingNavBar(
                selectedIndex = selectedNavIndex,
                onItemSelected = { index ->
                    selectedNavIndex = index
                    currentPage = pages[index]
                }
            )
        }

        AnimatedVisibility(
            visible = uiVisible,
            enter = fadeIn(animationSpec = tween(450, delayMillis = 450)) +
                    slideInHorizontally(
                        initialOffsetX = { it / 6 },
                        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy)
                    ),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 10.dp, bottom = 30.dp)
        ) {
            VerticalNavButtons()
        }
    }
}