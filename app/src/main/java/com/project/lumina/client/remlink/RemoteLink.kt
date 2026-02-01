package com.project.lumina.client.remlink

import android.content.Context
import android.net.wifi.WifiManager
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.project.lumina.client.application.AppContext
import com.project.lumina.client.model.CaptureModeModel
import com.project.lumina.client.overlay.mods.NotificationType
import com.project.lumina.client.overlay.mods.SimpleOverlayNotification
import com.project.lumina.client.service.Services
import com.project.lumina.client.ui.component.RemAccountScreen
import com.project.lumina.client.ui.component.RemConfigCategoryContent
import com.project.lumina.client.viewmodel.MainScreenViewModel
import com.project.lumina.client.R
import com.project.lumina.client.router.main.RealmsScreen
import com.project.lumina.client.ui.component.RealmsSelector
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.bouncycastle.math.raw.Mod
import java.net.InetAddress
import java.net.NetworkInterface
import java.text.SimpleDateFormat
import java.util.*


fun getLocalIpAddress(context: Context): String {
    try {

        val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
        if (wifiManager.isWifiEnabled) {
            val wifiInfo = wifiManager.connectionInfo
            val ipAddress = wifiInfo.ipAddress
            if (ipAddress != 0) {
                return String.format(
                    "%d.%d.%d.%d",
                    (ipAddress and 0xff),
                    (ipAddress shr 8 and 0xff),
                    (ipAddress shr 16 and 0xff),
                    (ipAddress shr 24 and 0xff)
                )
            } else {
                TerminalViewModel.addTerminalLog("Warning", "Wi-Fi enabled but no IP address available")
            }
        } else {
            TerminalViewModel.addTerminalLog("Warning", "Wi-Fi is disabled")
        }


        val interfaces = NetworkInterface.getNetworkInterfaces()
        while (interfaces.hasMoreElements()) {
            val networkInterface = interfaces.nextElement()
            val addresses = networkInterface.inetAddresses
            while (addresses.hasMoreElements()) {
                val address = addresses.nextElement()
                if (!address.isLoopbackAddress && address is InetAddress && address.hostAddress.contains(".")) {
                    return address.hostAddress
                }
            }
        }
        TerminalViewModel.addTerminalLog("Error", "No valid IP address found on any network interface")
    } catch (e: Exception) {
        TerminalViewModel.addTerminalLog("Error", "Failed to retrieve IP address: ${e.message}")
        e.printStackTrace()
    }
    return "Unknown"
}

class TerminalViewModel : ViewModel() {
    private val _isServiceRunning = MutableStateFlow(false)
    val isServiceRunning = _isServiceRunning.asStateFlow()

    private val _terminalLogs = MutableStateFlow(listOf(LogEntry("System", "Terminal initialized", System.currentTimeMillis())))
    val terminalLogs: StateFlow<List<LogEntry>> = _terminalLogs.asStateFlow()

    private val gameSettingsSharedPreferences by lazy {
        AppContext.instance.getSharedPreferences("game_settings", Context.MODE_PRIVATE)
    }

    private val _captureModeModel = MutableStateFlow(initialCaptureModeModel())
    val captureModeModel = _captureModeModel.asStateFlow()

    private fun initialCaptureModeModel(): CaptureModeModel {
        return CaptureModeModel.from(gameSettingsSharedPreferences)
    }

    fun addLog(source: String, message: String) {
        _terminalLogs.value = _terminalLogs.value + LogEntry(source, message, System.currentTimeMillis())
    }

    fun toggleService(context: Context, captureModeModel: CaptureModeModel) {
        viewModelScope.launch {
            val newStatus = !_isServiceRunning.value
            _isServiceRunning.value = newStatus

            if (newStatus) {
                Services.RemisOnline = true
                val localIp = getLocalIpAddress(context)
                addLog("System", "Join: $localIp:19132")
            } else {
                addLog("System", "Stopping service...")
                Services.RemisOnline = false
            }

            Services.toggle(context, captureModeModel)

            delay(1000)
            if (newStatus) {
                if (Services.isActive) {
                    addLog("System", "Service started successfully")
                } else {
                    addLog("Error", "Service failed to start")
                }
            } else {
                addLog("System", "Service stopped")
            }
        }
    }

    companion object {
        private var instance: TerminalViewModel? = null

        fun getInstance(): TerminalViewModel {
            if (instance == null) {
                instance = TerminalViewModel()
            }
            return instance!!
        }

        fun addTerminalLog(source: String, message: String) {
            getInstance().addLog(source, message)
        }
    }
}

data class LogEntry(
    val source: String,
    val message: String,
    val timestamp: Long
)

@OptIn(ExperimentalMaterial3Api::class)
sealed class Screen(val route: String, val title: String, val icon: @Composable () -> Unit) {
    object Link : Screen("link", "Link", { Icon(Icons.Filled.Link, contentDescription = "Link") })
    object Modules : Screen("modules", "Modules", { Icon(Icons.Filled.Code, contentDescription = "Modules") })
    object Account : Screen("account", "Account", { Icon(Icons.Filled.AccountCircle, contentDescription = "Account") })
    object Config : Screen("config", "Settings", { Icon(Icons.Filled.Settings, contentDescription = "Settings") })
    object Realms : Screen("realm", "Realms", { Icon(painter = painterResource(R.drawable.ic_cube_outline_black_24dp), contentDescription = "Settings")})
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemoteLink(viewModel: TerminalViewModel = viewModel { TerminalViewModel.getInstance() }) {
    val navController = rememberNavController()
    val terminalLogs by viewModel.terminalLogs.collectAsState()
    val isServiceRunning by viewModel.isServiceRunning.collectAsState()
    val context = LocalContext.current
    val captureModeModel by viewModel.captureModeModel.collectAsState()
    val listState = rememberLazyListState()
    val mainScreenViewModel: MainScreenViewModel = viewModel()
    val coroutineScope = rememberCoroutineScope()

    val screens = listOf(
        Screen.Link,
        Screen.Modules,
        Screen.Account,
        Screen.Config,
        Screen.Realms
    )


    LaunchedEffect(terminalLogs) {
        if (terminalLogs.isNotEmpty()) {
            listState.animateScrollToItem(terminalLogs.size - 1)
        }
    }

    val showNotification: (String, NotificationType) -> Unit = { message, type ->
        SimpleOverlayNotification.show(
            message = message,
            type = type,
            durationMs = 3000
        )
    }

    Scaffold(
        topBar = {
            TopBarWithAnimation(navController, screens)
        },
        bottomBar = {
            AnimatedNavigationBar(navController, screens)
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Link.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(
                Screen.Link.route,
                enterTransition = {
                    slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(300)) +
                            fadeIn(animationSpec = tween(300))
                },
                exitTransition = {
                    slideOutHorizontally(targetOffsetX = { -it }, animationSpec = tween(300)) +
                            fadeOut(animationSpec = tween(300))
                }
            ) {
                LinkScreen(
                    terminalLogs = terminalLogs,
                    listState = listState,
                    isServiceRunning = isServiceRunning,
                    onToggleService = {
                        coroutineScope.launch {
                            viewModel.toggleService(context, captureModeModel)
                        }
                    }
                )
            }
            composable(
                Screen.Modules.route,
                enterTransition = {
                    slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(300)) +
                            fadeIn(animationSpec = tween(300))
                },
                exitTransition = {
                    slideOutHorizontally(targetOffsetX = { -it }, animationSpec = tween(300)) +
                            fadeOut(animationSpec = tween(300))
                }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .animateEnterExit(
                            enter = fadeIn(animationSpec = tween(300)),
                            exit = fadeOut(animationSpec = tween(300))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    RemUi()
                }
            }
            composable(
                Screen.Account.route,
                enterTransition = {
                    slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(300)) +
                            fadeIn(animationSpec = tween(300))
                },
                exitTransition = {
                    slideOutHorizontally(targetOffsetX = { -it }, animationSpec = tween(300)) +
                            fadeOut(animationSpec = tween(300))
                }
            ) {
                RemAccountScreen(showNotification)
            }
            composable(
                Screen.Config.route,
                enterTransition = {
                    slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(300)) +
                            fadeIn(animationSpec = tween(300))
                },
                exitTransition = {
                    slideOutHorizontally(targetOffsetX = { -it }, animationSpec = tween(300)) +
                            fadeOut(animationSpec = tween(300))
                }
            ) {
                RemConfigCategoryContent()
            }
            composable(
                Screen.Realms.route,
                enterTransition = {
                    slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(300)) +
                            fadeIn(animationSpec = tween(300))
                },
                exitTransition = {
                    slideOutHorizontally(targetOffsetX = { -it }, animationSpec = tween(300)) +
                            fadeOut(animationSpec = tween(300))
                }
            ) {

                    RemRealmsScreen()


            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBarWithAnimation(navController: androidx.navigation.NavHostController, screens: List<Screen>) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentScreen = screens.find { screen ->
        currentDestination?.hierarchy?.any { it.route == screen.route } == true
    } ?: Screen.Link


    val titleState = remember { mutableStateOf(currentScreen.title) }
    val targetTitleAlpha = remember { Animatable(1f) }

    LaunchedEffect(currentScreen) {
        targetTitleAlpha.snapTo(0f)
        titleState.value = currentScreen.title
        targetTitleAlpha.animateTo(1f, animationSpec = tween(300))
    }

    TopAppBar(
        title = {
            Text(
                text = titleState.value,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .graphicsLayer {
                        alpha = targetTitleAlpha.value
                        translationY = (1f - targetTitleAlpha.value) * -20f
                    }
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.primary
        )
    )
}

@Composable
fun AnimatedNavigationBar(navController: androidx.navigation.NavHostController, screens: List<Screen>) {
    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination

        screens.forEach { screen ->
            val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
            val scale = animateFloatAsState(
                targetValue = if (selected) 1.15f else 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                ),
                label = "navItemScale"
            )

            NavigationBarItem(
                icon = {
                    Box(
                        modifier = Modifier
                            .scale(scale.value)
                    ) {
                        screen.icon()
                    }
                },
                label = {
                    Text(
                        screen.title,
                        modifier = Modifier.graphicsLayer {
                            alpha = if (selected) 1f else 0.7f
                        }
                    )
                },
                selected = selected,
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}

@Composable
fun LinkScreen(
    terminalLogs: List<LogEntry>,
    listState: androidx.compose.foundation.lazy.LazyListState,
    isServiceRunning: Boolean,
    onToggleService: () -> Unit
) {
    val serviceButtonColor by animateColorAsState(
        targetValue = if (isServiceRunning) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
        label = "serviceButtonColor"
    )
    val context = LocalContext.current
    val localIp = remember(isServiceRunning) { if (isServiceRunning) getLocalIpAddress(context) else "" }
    val port = "19132"
    val showNewEntryAnimation = remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()


    val scale = remember { Animatable(1f) }


    val pulseAnimation = rememberInfiniteTransition(label = "pulse")
    val pulseScale = if (isServiceRunning) {
        pulseAnimation.animateFloat(
            initialValue = 1.0f,
            targetValue = 1.2f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulseScale"
        )
    } else {
        remember { mutableStateOf(1f) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .graphicsLayer {
                    alpha = 1f
                    scaleX = 1f
                    scaleY = 1f
                }
                .animateContentSize(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 4.dp
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Terminal Output",
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp
                    )

                    StatusIndicator(isActive = isServiceRunning, pulseScale = pulseScale.value)
                }

                Divider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize()
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(top = 8.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF1E1E1E))
                        .padding(8.dp)
                ) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(terminalLogs) { log ->
                            AnimatedTerminalLogEntry(
                                log = log,
                                isNewEntry = terminalLogs.indexOf(log) == terminalLogs.size - 1 && showNewEntryAnimation.value
                            )
                        }
                    }


                    LaunchedEffect(terminalLogs.size) {
                        if (terminalLogs.isNotEmpty()) {
                            showNewEntryAnimation.value = true
                            delay(300)
                            showNewEntryAnimation.value = false
                        }
                    }
                }
            }
        }


        AnimatedVisibility(
            visible = isServiceRunning,
            enter = expandVertically(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ) + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ConnectionInfoDisplay(localIp, port)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))


        Button(
            onClick = {
                coroutineScope.launch {

                    scale.animateTo(
                        targetValue = 0.9f,
                        animationSpec = tween(100)
                    )
                    scale.animateTo(
                        targetValue = 1f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    )
                    onToggleService()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .scale(scale.value),
            colors = ButtonDefaults.buttonColors(
                containerColor = serviceButtonColor
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {

                val rotationAnimation = animateFloatAsState(
                    targetValue = if (isServiceRunning) 180f else 0f,
                    animationSpec = tween(500),
                    label = "rotation"
                )

                Icon(
                    imageVector = if (isServiceRunning) Icons.Outlined.Stop else Icons.Outlined.PlayArrow,
                    contentDescription = if (isServiceRunning) "Stop Service" else "Start Service",
                    modifier = Modifier.graphicsLayer {
                        rotationZ = rotationAnimation.value
                    }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isServiceRunning) "Stop Remote Link" else "Start Remote Link")
            }
        }
    }
}

@Composable
fun ConnectionInfoDisplay(localIp: String, port: String) {
    val enterAnimation = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        enterAnimation.animateTo(
            targetValue = 1f,
            animationSpec = tween(500)
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                alpha = enterAnimation.value
                translationY = (1f - enterAnimation.value) * 20f
            },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Public,
                    contentDescription = "IP Address",
                    tint = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Local IP: $localIp",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Numbers,
                    contentDescription = "Port",
                    tint = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Port: $port",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun StatusIndicator(isActive: Boolean, pulseScale: Float = 1f) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .scale(pulseScale)
                .background(
                    color = if (isActive) Color(0xFF4CAF50) else Color(0xFFE57373),
                    shape = CircleShape
                )
                .zIndex(2f)
        )

        Spacer(modifier = Modifier.width(4.dp))

        Text(
            text = if (isActive) "Active" else "Inactive",
            color = if (isActive) Color(0xFF4CAF50) else Color(0xFFE57373),
            fontSize = 14.sp
        )
    }
}

@Composable
fun AnimatedTerminalLogEntry(log: LogEntry, isNewEntry: Boolean = false) {
    val dateFormat = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }
    val formattedTime = remember(log.timestamp) { dateFormat.format(Date(log.timestamp)) }


    val alpha = animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(300),
        label = "alpha"
    )

    val translateY = animateFloatAsState(
        targetValue = 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "translateY"
    )

    val logColor = when (log.source) {
        "System" -> Color(0xFF64B5F6)
        "Error" -> Color(0xFFE57373)
        "Warning" -> Color(0xFFFFD54F)
        else -> Color(0xFF81C784)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .alpha(if (isNewEntry) alpha.value else 1f)
            .graphicsLayer {
                this.translationY = if (isNewEntry) translateY.value * 20 else 0f
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "[$formattedTime]",
            color = Color(0xFF9E9E9E),
            fontSize = 12.sp,
            maxLines = 1
        )

        Spacer(modifier = Modifier.width(4.dp))


        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(logColor.copy(alpha = 0.2f))
                .padding(horizontal = 4.dp, vertical = 2.dp)
        ) {
            Text(
                text = log.source,
                color = logColor,
                fontSize = 12.sp,
                maxLines = 1
            )
        }

        Spacer(modifier = Modifier.width(4.dp))

        Text(
            text = log.message,
            color = Color.White,
            fontSize = 14.sp,
            modifier = Modifier.weight(1f),
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun animateColorAsState(
    targetValue: Color,
    label: String
): State<Color> {


    return remember(targetValue) {
        mutableStateOf(targetValue)
    }
}