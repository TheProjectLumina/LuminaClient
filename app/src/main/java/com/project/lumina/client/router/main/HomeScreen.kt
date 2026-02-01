/**
 * © Project Lumina 2026 — Licensed under GNU GPLv3
 * You are free to use, modify, and redistribute this code under the terms
 * of the GNU General Public License v3. See the LICENSE file for details.
 */

package com.project.lumina.client.router.main

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import android.widget.Toast
import com.project.lumina.client.R
import com.project.lumina.client.constructors.AccountManager
import com.project.lumina.client.util.InjectNeko
import com.project.lumina.client.util.MCPackUtils
import com.project.lumina.client.util.ServerInit
import com.project.lumina.client.overlay.mods.NotificationType
import com.project.lumina.client.overlay.mods.SimpleOverlayNotification
import com.project.lumina.client.service.Services
import com.project.lumina.client.ui.component.ServerSelector
import com.project.lumina.client.viewmodel.MainScreenViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.material3.rememberModalBottomSheetState

import com.project.lumina.client.ui.component.SubServerInfo
import net.lenni0451.commons.httpclient.HttpClient
import net.raphimc.minecraftauth.MinecraftAuth
import net.raphimc.minecraftauth.service.realms.BedrockRealmsService
import net.raphimc.minecraftauth.service.realms.model.RealmsWorld
import net.raphimc.minecraftauth.step.bedrock.session.StepFullBedrockSession
import net.raphimc.minecraftauth.step.msa.StepMsaDeviceCode
import net.raphimc.minecraftauth.util.MicrosoftConstants

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.project.lumina.client.ui.component.RealmsSelector
import com.project.lumina.client.util.RealmErrorHandler
import java.io.File
import java.util.concurrent.CompletableFuture

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onStartToggle: () -> Unit
) {
    val mainScreenViewModel: MainScreenViewModel = viewModel()
    val captureModeModel by mainScreenViewModel.captureModeModel.collectAsState()
    var selectedView by remember { mutableStateOf("ServerSelector") }
    var previousView by remember { mutableStateOf("ServerSelector") }

    var showCustomNotification by remember { mutableStateOf(false) }
    var customNotificationMessage by remember { mutableStateOf("") }
    var customNotificationType by remember { mutableStateOf<NotificationType>(NotificationType.INFO) }
    var lastCustomNotificationTime by remember { mutableLongStateOf(0L) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var progress by remember { mutableFloatStateOf(0f) }

    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showBottomSheet by remember { mutableStateOf(false) }
    var showConnectionDialog by remember { mutableStateOf(false) }

    var isLaunchingMinecraft by remember { mutableStateOf(false) }

    LaunchedEffect(Services.isActive) {
        if (Services.isActive) {
            delay(600)
            showBottomSheet = false
        } else {
            showBottomSheet = false
        }
    }

    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp
    val density = LocalDensity.current

    var showProgressDialog by remember { mutableStateOf(false) }
    var downloadProgress by remember { mutableStateOf(0f) }
    var currentPackName by remember { mutableStateOf("") }

    var showZeqaBottomSheet by remember { mutableStateOf(false) }
    var showAddServerDialog by remember { mutableStateOf(false) }
    var editingServer by remember { mutableStateOf<com.project.lumina.client.data.CustomServer?>(null) }
    var serverRefreshTrigger by remember { mutableStateOf(0) }

    val sharedPreferences = context.getSharedPreferences("SettingsPrefs", Context.MODE_PRIVATE)
    var InjectNekoPack by remember {
        mutableStateOf(sharedPreferences.getBoolean("injectNekoPackEnabled", false))
    }

    val isCompactScreen = screenWidth < 600.dp

    val leftColumnWidth = if (isCompactScreen) 0.4f else 0.5f
    val localIp = "localhost"

    val httpClient = remember { HttpClient() }
    var bedrockSession by remember { mutableStateOf<StepFullBedrockSession.FullBedrockSession?>(null) }
    var realms by remember { mutableStateOf<List<RealmsWorld>>(emptyList()) }
    var isFetchingRealms by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        loadRealmsSession(context, httpClient) { session ->
            bedrockSession = session
        }
    }
    val showNotification: (String, NotificationType) -> Unit = { message, type ->
        SimpleOverlayNotification.show(
            message = message,
            type = type,
            durationMs = 3000
        )
    }

    DisposableEffect(Unit) {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { prefs, key ->
            if (key == "injectNekoPackEnabled") {
                InjectNekoPack = prefs.getBoolean("injectNekoPackEnabled", false)
            }
        }
        sharedPreferences.registerOnSharedPreferenceChangeListener(listener)
        onDispose {
            sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }

    Row(
        Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        Column(
            Modifier
                .fillMaxHeight()
                .fillMaxWidth(leftColumnWidth)
                .padding(
                    start = if (isCompactScreen) 12.dp else 24.dp,
                    end = if (isCompactScreen) 8.dp else 24.dp,
                    top = if (isCompactScreen) 16.dp else 24.dp,
                    bottom = if (isCompactScreen) 16.dp else 24.dp
                ),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {

            if (isCompactScreen) {

                Column(
                    Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(bottom = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    val tabs = listOf("ServerSelector", "View2", "View3", "View4")
                    val tabNames = listOf(R.string.servers, R.string.accounts, R.string.packs, R.string.realms)

                    tabs.forEachIndexed { index, tab ->
                        val isSelected = selectedView == tab
                        val interactionSource = remember { MutableInteractionSource() }

                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                previousView = selectedView
                                selectedView = tab
                            },
                            label = {
                                Text(
                                    stringResource(tabNames[index]),
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Center
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(32.dp),
                            interactionSource = interactionSource
                        )
                    }
                }
            } else {

                Row(
                    Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    val tabs = listOf("ServerSelector", "View2", "View3", "View4")
                    val tabNames = listOf(R.string.servers, R.string.accounts, R.string.packs, R.string.realms)

                    tabs.forEachIndexed { index, tab ->
                        val isSelected = selectedView == tab
                        val interactionSource = remember { MutableInteractionSource() }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .animateContentSize(
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessLow
                                    )
                                )
                        ) {
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    previousView = selectedView
                                    selectedView = tab
                                },
                                label = {
                                    Text(
                                        stringResource(tabNames[index]),
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.fillMaxWidth(),
                                        textAlign = TextAlign.Center
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(32.dp),
                                interactionSource = interactionSource
                            )
                        }
                    }
                }
            }


            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {

                val currentTabIndex = when (selectedView) {
                    "ServerSelector" -> 0
                    "View2" -> 1
                    "View3" -> 2
                    "View4" -> 3
                    else -> 0
                }

                val previousTabIndex = when (previousView) {
                    "ServerSelector" -> 0
                    "View2" -> 1
                    "View3" -> 2
                    "View4" -> 3
                    else -> 0
                }

                val enterTransition = if (currentTabIndex > previousTabIndex) {

                    slideInHorizontally(initialOffsetX = { it }) + fadeIn()
                } else {

                    slideInHorizontally(initialOffsetX = { -it }) + fadeIn()
                }

                val exitTransition = if (currentTabIndex > previousTabIndex) {

                    slideOutHorizontally(targetOffsetX = { -it }) + fadeOut()
                } else {

                    slideOutHorizontally(targetOffsetX = { it }) + fadeOut()
                }

                AnimatedContent(
                    targetState = selectedView,
                    transitionSpec = {
                        enterTransition togetherWith exitTransition using SizeTransform(clip = false)
                    },
                    label = "tabContentAnimation"
                ) { targetView ->
                    when (targetView) {
                        "ServerSelector" -> ServerSelector(
                            onShowZeqaBottomSheet = { showZeqaBottomSheet = true },
                            onShowAddServerDialog = {
                                editingServer = null
                                showAddServerDialog = true
                            },
                            onShowEditServerDialog = { server ->
                                editingServer = server
                                showAddServerDialog = true
                            },
                            refreshTrigger = serverRefreshTrigger
                        )
                        "View2" -> AccountScreen(showNotification)
                        "View3" -> PacksScreen()
                        "View4" -> RealmsSelector()
                    }
                }
            }
        }


        if (!isCompactScreen) {
            VerticalDivider(
                modifier = Modifier
                    .fillMaxHeight(0.97f)
                    .width(1.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
            )
        }


        Box(
            Modifier
                .fillMaxHeight()
                .fillMaxWidth()
                .padding(
                    start = if (isCompactScreen) 8.dp else 16.dp,
                    end = if (isCompactScreen) 12.dp else 16.dp,
                    top = if (isCompactScreen) 16.dp else 16.dp,
                    bottom = if (isCompactScreen) 16.dp else 16.dp
                )
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    horizontalAlignment = Alignment.End
                ) {
                    AnimatedContent(
                        targetState = AccountManager.currentAccount?.remark,
                        transitionSpec = {
                            (slideInVertically { height -> -height } + fadeIn()) togetherWith
                                    (slideOutVertically { height -> height } + fadeOut())
                        },
                        label = "accountAnimation"
                    ) { accountRemark ->
                        if (accountRemark != null) {

                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight(if (isCompactScreen) 0.2f else 0.25f)
                                    .animateContentSize(
                                        animationSpec = spring(
                                            dampingRatio = 0.7f,
                                            stiffness = 400f
                                        )
                                    ),
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerLow,
                                tonalElevation = 2.dp,
                                shadowElevation = 1.dp
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {

                                    if (isCompactScreen) {

                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 12.dp, vertical = 8.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {

                                            Surface(
                                                modifier = Modifier.size(40.dp),
                                                shape = CircleShape,
                                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Rounded.AccountCircle,
                                                    contentDescription = null,
                                                    modifier = Modifier
                                                        .padding(2.dp)
                                                        .size(36.dp),
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                            }


                                            Text(
                                                text = "Hello!",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurface.copy(
                                                    alpha = 0.7f
                                                )
                                            )

                                            Text(
                                                text = accountRemark,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    } else {

                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 24.dp, vertical = 16.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                                        ) {

                                            Surface(
                                                modifier = Modifier.size(56.dp),
                                                shape = CircleShape,
                                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Rounded.AccountCircle,
                                                    contentDescription = null,
                                                    modifier = Modifier
                                                        .padding(4.dp)
                                                        .size(48.dp),
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                            }


                                            Row(
                                                horizontalArrangement = Arrangement.SpaceEvenly,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "Hello!",
                                                    style = MaterialTheme.typography.titleMedium,
                                                    color = MaterialTheme.colorScheme.onSurface.copy(
                                                        alpha = 0.7f
                                                    )
                                                )

                                                Text(
                                                    modifier = Modifier.padding(start = 4.dp),
                                                    text = accountRemark,
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Thin,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }


                    Spacer(modifier = Modifier.height(if (isCompactScreen) 8.dp else 16.dp))


                    AnimatedVisibility(
                        visible = showCustomNotification,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Card(
                            modifier = Modifier
                                .width(if (isCompactScreen) 240.dp else 320.dp)
                                .wrapContentHeight(),
                            shape = MaterialTheme.shapes.medium,
                            colors = CardDefaults.cardColors(
                                containerColor = when (customNotificationType) {
                                    NotificationType.SUCCESS -> MaterialTheme.colorScheme.primaryContainer
                                    NotificationType.ERROR -> MaterialTheme.colorScheme.errorContainer
                                    NotificationType.INFO -> MaterialTheme.colorScheme.surfaceContainerHigh
                                    NotificationType.WARNING -> MaterialTheme.colorScheme.tertiaryContainer
                                }
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        horizontal = if (isCompactScreen) 12.dp else 16.dp,
                                        vertical = if (isCompactScreen) 8.dp else 12.dp
                                    ),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = customNotificationMessage,
                                    style = if (isCompactScreen)
                                        MaterialTheme.typography.bodySmall else
                                        MaterialTheme.typography.bodyMedium,
                                    color = when (customNotificationType) {
                                        NotificationType.SUCCESS -> MaterialTheme.colorScheme.onPrimaryContainer
                                        NotificationType.ERROR -> MaterialTheme.colorScheme.onErrorContainer
                                        NotificationType.INFO -> MaterialTheme.colorScheme.onSurface
                                        NotificationType.WARNING -> MaterialTheme.colorScheme.onTertiaryContainer
                                    }
                                )
                            }
                        }


                        Spacer(modifier = Modifier.height(if (isCompactScreen) 8.dp else 16.dp))
                    }


                    AnimatedVisibility(
                        visible = captureModeModel.serverHostName.isNotBlank(),
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = if (isCompactScreen) 70.dp else 90.dp)
                                .wrapContentHeight(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                                contentColor = MaterialTheme.colorScheme.onSurface
                            ),
                            elevation = CardDefaults.cardElevation(
                                defaultElevation = 2.dp,
                                pressedElevation = 4.dp
                            )
                        ) {
                            Column(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(if (isCompactScreen) 12.dp else 16.dp),
                                horizontalAlignment = Alignment.Start,
                                verticalArrangement = Arrangement.spacedBy(if (isCompactScreen) 4.dp else 8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(if (isCompactScreen) 4.dp else 8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.PlayArrow,
                                        contentDescription = null,
                                        modifier = Modifier.size(if (isCompactScreen) 16.dp else 20.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = stringResource(R.string.selected_server),
                                        style = if (isCompactScreen)
                                            MaterialTheme.typography.bodyLarge else
                                            MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Text(
                                    text = captureModeModel.serverHostName,
                                    style = if (isCompactScreen)
                                        MaterialTheme.typography.bodyLarge else
                                        MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = stringResource(R.string.port, captureModeModel.serverPort),
                                    style = if (isCompactScreen)
                                        MaterialTheme.typography.bodySmall else
                                        MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }


                Spacer(modifier = Modifier.weight(1f))


                Box(
                    Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(top = if (isCompactScreen) 12.dp else 20.dp),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    AnimatedContent(
                        targetState = Services.isActive,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(300)) togetherWith
                                    fadeOut(animationSpec = tween(200))
                        },
                        label = "buttonLayoutAnimation"
                    ) { isActive ->

                        val scaleAnimation by animateFloatAsState(
                            targetValue = if (isActive) 1f else 0.95f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioLowBouncy,
                                stiffness = Spring.StiffnessMedium
                            ),
                            label = "buttonScaleAnimation"
                        )

                        if (isActive) {
                            Button(
                                onClick = {
                                    isLaunchingMinecraft = false
                                    onStartToggle()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error,
                                    contentColor = MaterialTheme.colorScheme.onError
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(if (isCompactScreen) 48.dp else 56.dp),
                                shape = RoundedCornerShape(if (isCompactScreen) 12.dp else 16.dp)
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Pause,
                                        contentDescription = "Stop",
                                        modifier = Modifier.size(if (isCompactScreen) 20.dp else 24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = stringResource(R.string.stop),
                                        style = if (isCompactScreen)
                                            MaterialTheme.typography.bodyLarge else
                                            MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        } else {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = if (isCompactScreen) 8.dp else 12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                val sharedPreferences = context.getSharedPreferences("SettingsPrefs", Context.MODE_PRIVATE)
                                val disableAuthRequiredEnabled = sharedPreferences.getBoolean("disableAuthRequiredEnabled", false)
                                val isAccountLoggedIn = AccountManager.currentAccount != null || disableAuthRequiredEnabled

                                ExtendedFloatingActionButton(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(if (isCompactScreen) 48.dp else 56.dp)
                                        .scale(scaleAnimation)
                                        .animateContentSize(),
                                    onClick = {
                                        if (AccountManager.currentAccount == null && !disableAuthRequiredEnabled) {
                                            Toast.makeText(
                                                context,
                                                "Please login and select an account first to start the service",
                                                Toast.LENGTH_LONG
                                            ).show()
                                            return@ExtendedFloatingActionButton
                                        }

                                        scope.launch {
                                            delay(100)
                                            isLaunchingMinecraft = true
                                            Services.isLaunchingMinecraft = true
                                            onStartToggle()

                                            delay(2500)
                                            if (!Services.isActive) {
                                                isLaunchingMinecraft = false
                                                Services.isLaunchingMinecraft = false
                                                return@launch
                                            }

                                            val disableAutoStartEnabled = sharedPreferences.getBoolean("disableAutoStartEnabled", false)
                                            if (!disableAutoStartEnabled) {
                                                val selectedGame = mainScreenViewModel.selectedGame.value
                                                if (selectedGame != null) {
                                                val intent = context.packageManager.getLaunchIntentForPackage(selectedGame)
                                                if (intent != null && Services.isActive) {
                                                    context.startActivity(intent)

                                                    delay(3000)
                                                    isLaunchingMinecraft = false
                                                    Services.isLaunchingMinecraft = false
                                                    
                                                    try {
                                                        when {
                                                            InjectNekoPack == true && PackSelectionManager.selectedPack != null -> {
                                                                PackSelectionManager.selectedPack?.let { selectedPack ->
                                                                    currentPackName = selectedPack.name
                                                                    showProgressDialog = true
                                                                    downloadProgress = 0f

                                                                    try {
                                                                        MCPackUtils.downloadAndOpenPack(
                                                                            context,
                                                                            selectedPack
                                                                        ) { progress ->
                                                                            downloadProgress = progress
                                                                        }
                                                                        showProgressDialog = false
                                                                    } catch (e: Exception) {
                                                                        showProgressDialog = false
                                                                        showNotification(
                                                                            "Failed to download pack: ${e.message}",
                                                                            NotificationType.ERROR
                                                                        )
                                                                    }
                                                                }
                                                            }

                                                            InjectNekoPack == true -> {
                                                                try {
                                                                    InjectNeko.injectNeko(
                                                                        context = context,
                                                                        onProgress = {
                                                                            progress = it
                                                                        }
                                                                    )
                                                                } catch (e: Exception) {
                                                                    showNotification(
                                                                        "Failed to inject Neko: ${e.message}",
                                                                        NotificationType.ERROR
                                                                    )
                                                                }
                                                            }

                                                            else -> {
                                                                if (selectedGame == "com.mojang.minecraftpe") {
                                                                    try {
                                                                        ServerInit.addMinecraftServer(
                                                                            context,
                                                                            localIp
                                                                        )
                                                                    } catch (e: Exception) {
                                                                        showNotification(
                                                                            "Failed to initialize server: ${e.message}",
                                                                            NotificationType.ERROR
                                                                        )
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    } catch (e: Exception) {
                                                        showNotification(
                                                            "An unexpected error occurred: ${e.message}",
                                                            NotificationType.ERROR
                                                        )
                                                    }
                                                } else {
                                                    showNotification(
                                                        "Failed to launch game",
                                                        NotificationType.ERROR
                                                    )
                                                }
                                            }
                                            } else {
                                                isLaunchingMinecraft = false
                                                Services.isLaunchingMinecraft = false
                                            }
                                        }
                                    },
                                    containerColor = if (isAccountLoggedIn)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                    contentColor = if (isAccountLoggedIn)
                                        MaterialTheme.colorScheme.onPrimary
                                    else
                                        MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                                    shape = RoundedCornerShape(if (isCompactScreen) 12.dp else 16.dp),
                                    elevation = FloatingActionButtonDefaults.elevation(
                                        defaultElevation = 4.dp,
                                        pressedElevation = 8.dp
                                    ),
                                    icon = {
                                        if (isAccountLoggedIn) {
                                            Icon(
                                                imageVector = Icons.Rounded.PlayArrow,
                                                contentDescription = null,
                                                modifier = Modifier.size(if (isCompactScreen) 20.dp else 24.dp)
                                            )
                                        } else {
                                            Icon(
                                                imageVector = Icons.Rounded.Lock,
                                                contentDescription = null,
                                                modifier = Modifier.size(if (isCompactScreen) 18.dp else 22.dp)
                                            )
                                        }
                                    },
                                    text = {
                                        Text(
                                            text = stringResource(R.string.start),
                                            style = if (isCompactScreen)
                                                MaterialTheme.typography.bodyLarge else
                                                MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                )

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
            }
        }
    }

    if (showZeqaBottomSheet) {
        ZeqaSubServerBottomSheet(
            onDismiss = { showZeqaBottomSheet = false },
            onSelect = { subServer ->
                mainScreenViewModel.selectCaptureModeModel(
                    captureModeModel.copy(serverHostName = subServer.serverAddress, serverPort = subServer.serverPort)
                )
                showZeqaBottomSheet = false
            }
        )
    }

    
    if (showAddServerDialog) {
        com.project.lumina.client.ui.component.AddServerDialog(
            editingServer = editingServer,
            onDismiss = {
                showAddServerDialog = false
                editingServer = null
            },
            onSave = { server ->
                val customServerManager = com.project.lumina.client.data.CustomServerManager.getInstance()
                customServerManager.saveServer(server)
                serverRefreshTrigger++ 
                showAddServerDialog = false
                editingServer = null
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZeqaSubServerBottomSheet(
    onDismiss: () -> Unit,
    onSelect: (SubServerInfo) -> Unit
) {
    val subServers = listOf(
        SubServerInfo("NA1", "North America", "na.zeqa.net", 10001),
        SubServerInfo("NA2", "North America", "na.zeqa.net", 10002),
        SubServerInfo("NA3", "North America", "na.zeqa.net", 10003),
        SubServerInfo("NA4", "North America", "na.zeqa.net", 10004),
        SubServerInfo("NA5", "North America", "na.zeqa.net", 10005),
        SubServerInfo("NA6", "North America", "na.zeqa.net", 10006),
        SubServerInfo("EU1", "Europe", "eu.zeqa.net", 10001),
        SubServerInfo("EU2", "Europe", "eu.zeqa.net", 10002),
        SubServerInfo("EU3", "Europe", "eu.zeqa.net", 10003),
        SubServerInfo("EU4", "Europe", "eu.zeqa.net", 10004),
        SubServerInfo("EU5", "Europe", "eu.zeqa.net", 10005),
        SubServerInfo("EU6", "Europe", "eu.zeqa.net", 10006),
        SubServerInfo("AS1", "Asia", "as.zeqa.net", 10001),
        SubServerInfo("AS2", "Asia", "as.zeqa.net", 10002),
        SubServerInfo("AS3", "Asia", "as.zeqa.net", 10003),
        SubServerInfo("AS4", "Asia", "as.zeqa.net", 10004),
        SubServerInfo("AS5", "Asia", "as.zeqa.net", 10005),
        SubServerInfo("AS6", "Asia", "as.zeqa.net", 10006),
        SubServerInfo("SA1", "South America", "sa.zeqa.net", 10001),
        SubServerInfo("AU1", "Australia", "au.zeqa.net", 10001),
        SubServerInfo("ZA1", "South Africa", "za.zeqa.net", 10001)
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        dragHandle = {
            Box(
                modifier = Modifier
                    .width(32.dp)
                    .height(4.dp)
                    .background(
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        RoundedCornerShape(2.dp)
                    )
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Select Zeqa Sub-Server",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                TextButton(onClick = onDismiss) {
                    Text(
                        "Cancel",
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Text(
                "Choose a sub-server based on your region",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(subServers) { subServer ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .clickable {
                                onSelect(subServer)
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        shape = RoundedCornerShape(8.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = subServer.id,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = subServer.region,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.weight(1f))
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = subServer.serverAddress,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Port: ${subServer.serverPort}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private const val REALMS_TAG = "RealmsView"
private const val REALMS_SESSION_FILE = "bedrock_session.json"
private const val REALMS_BEDROCK_CLIENT_VERSION = "1.21.100"

private val REALMS_BEDROCK_AUTH_FLOW = MinecraftAuth.builder()
    .withClientId(MicrosoftConstants.BEDROCK_ANDROID_TITLE_ID)
    .withScope(MicrosoftConstants.SCOPE_TITLE_AUTH)
    .deviceCode()
    .withDeviceToken("Android")
    .sisuTitleAuthentication(MicrosoftConstants.BEDROCK_XSTS_RELYING_PARTY)
    .buildMinecraftBedrockChainStep(true, true)

@Composable
fun RealmsView(
    bedrockSession: StepFullBedrockSession.FullBedrockSession?,
    realms: List<RealmsWorld>,
    httpClient: HttpClient,
    isFetchingRealms: Boolean,
    onFetchRealms: () -> Unit
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isCompactScreen = configuration.screenWidthDp.dp < 600.dp
    var realmAddresses by remember { mutableStateOf<Map<Int, String>>(emptyMap()) }
    var statusMessage by remember { mutableStateOf("") }

    val isLoggedIn = bedrockSession != null && bedrockSession?.realmsXsts != null

    if (isLoggedIn) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(if (isCompactScreen) 12.dp else 16.dp),
            verticalArrangement = Arrangement.spacedBy(if (isCompactScreen) 12.dp else 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Your Realms",
                    style = if (isCompactScreen)
                        MaterialTheme.typography.headlineSmall else
                        MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                Button(
                    onClick = onFetchRealms,
                    enabled = !isFetchingRealms,
                    modifier = Modifier.wrapContentSize(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(if (isCompactScreen) 8.dp else 12.dp)
                ) {
                    if (isFetchingRealms) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(if (isCompactScreen) 14.dp else 16.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Text(
                        text = if (isFetchingRealms) "Fetching..." else "Fetch Realms",
                        style = if (isCompactScreen)
                            MaterialTheme.typography.bodySmall else
                            MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            if (realms.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "No Realms Found",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            "Use the Fetch Realms button above to load your realms",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(realms) { world ->
                        RealmCard(
                            world = world,
                            realmAddress = realmAddresses[world.id.toInt()],
                            onJoinRealm = {
                                statusMessage = "Joining Realm: ${world.name}..."
                                joinRealm(httpClient, bedrockSession, world) { address, error ->
                                    if (error != null) {
                                        statusMessage = "Error joining Realm: $error"
                                    } else if (address != null) {
                                        realmAddresses = realmAddresses + (world.id.toInt() to address)
                                        statusMessage = "Joined Realm: ${world.name}"
                                    }
                                }
                            },
                            enabled = world.isCompatible && !world.isExpired && bedrockSession?.realmsXsts != null
                        )
                    }
                }
            }
        }
    } else {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier.padding(32.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.8f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Login Required",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Please login to your Microsoft account in the main Realms section to view your realms here.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun RealmCard(
    world: RealmsWorld,
    realmAddress: String?,
    onJoinRealm: () -> Unit,
    enabled: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                world.name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )

            Text(
                "Owner: ${world.ownerName}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )

            Text(
                "Max Players: ${world.maxPlayers}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )

            realmAddress?.let { address ->
                Text(
                    "Address: $address",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Button(
                onClick = onJoinRealm,
                enabled = enabled,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Join Realm")
            }
        }
    }
}

private fun loadRealmsSession(
    context: Context,
    httpClient: HttpClient,
    callback: (StepFullBedrockSession.FullBedrockSession?) -> Unit
) {
    CompletableFuture.supplyAsync {
        try {
            val file = File(context.filesDir, REALMS_SESSION_FILE)
            if (!file.exists()) {
                Log.d(REALMS_TAG, "Session file does not exist")
                return@supplyAsync null
            }

            val jsonString = file.readText()
            if (jsonString.isBlank()) {
                Log.e(REALMS_TAG, "Session file is empty")
                return@supplyAsync null
            }

            val json = JsonParser.parseString(jsonString) as JsonObject
            val session = REALMS_BEDROCK_AUTH_FLOW.fromJson(json)
            Log.d(REALMS_TAG, "Session loaded successfully")

            if (session.realmsXsts == null) {
                Log.e(REALMS_TAG, "Session missing realmsXsts token")
                return@supplyAsync null
            }

            session
        } catch (e: Exception) {
            Log.e(REALMS_TAG, "Error loading session", e)
            null
        }
    }.thenAccept { session ->
        callback(session)
    }
}

private fun fetchRealms(
    httpClient: HttpClient,
    session: StepFullBedrockSession.FullBedrockSession?,
    callback: (List<RealmsWorld>?, String?) -> Unit
) {
    Log.d(REALMS_TAG, "Fetching Realms")

    if (session?.realmsXsts == null) {
        callback(null, "No authentication session available")
        return
    }

    try {
        val realmsService = BedrockRealmsService(httpClient, REALMS_BEDROCK_CLIENT_VERSION, session.realmsXsts)

        realmsService.getWorlds()
            .thenAccept { worlds ->
                Log.d(REALMS_TAG, "Successfully fetched ${worlds.size} Realms")
                callback(worlds, null)
            }
            .exceptionally { throwable ->
                Log.e(REALMS_TAG, "Error fetching worlds", throwable)
                callback(null, RealmErrorHandler.translateFetchError(throwable))
                null
            }
    } catch (e: Exception) {
        Log.e(REALMS_TAG, "Exception creating BedrockRealmsService", e)
        callback(null, RealmErrorHandler.translateFetchError(e))
    }
}

private fun joinRealm(
    httpClient: HttpClient,
    session: StepFullBedrockSession.FullBedrockSession?,
    world: RealmsWorld,
    callback: (String?, String?) -> Unit
) {
    Log.d(REALMS_TAG, "Joining Realm: ${world.name}")

    if (session?.realmsXsts == null) {
        callback(null, RealmErrorHandler.translateJoinError(RuntimeException("No authentication session available")))
        return
    }

    try {
        val realmsService = BedrockRealmsService(httpClient, REALMS_BEDROCK_CLIENT_VERSION, session.realmsXsts)

        realmsService.joinWorld(world)
            .thenApply { address -> address.toString() }
            .thenAccept { addressString ->
                Log.d(REALMS_TAG, "Successfully joined Realm ${world.name}, address: $addressString")
                
                if (isNethernetAddress(addressString)) {
                    Log.w(REALMS_TAG, "Realm ${world.name} uses Nethernet protocol (address: $addressString)")
                    callback(null, "Nethernet Realms are not supported yet")
                    return@thenAccept
                }
                
                callback(addressString, null)
            }
            .exceptionally { throwable ->
                Log.e(REALMS_TAG, "Error joining Realm ${world.name}", throwable)
                callback(null, RealmErrorHandler.translateJoinError(throwable))
                null
            }
    } catch (e: Exception) {
        Log.e(REALMS_TAG, "Exception creating BedrockRealmsService", e)
        callback(null, RealmErrorHandler.translateJoinError(e))
    }
}

private fun isNethernetAddress(address: String): Boolean {
    val uuidPattern = Regex("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$", RegexOption.IGNORE_CASE)
    return uuidPattern.matches(address)
}
