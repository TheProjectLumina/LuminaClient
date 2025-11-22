/*
 * © Project Lumina 2025 — Licensed under GNU GPLv3
 * You are free to use, modify, and redistribute this code under the terms
 * of the GNU General Public License v3. See the LICENSE file for details.
 */

package com.project.lumina.client.router.main

import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.widget.Toast
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlin.system.exitProcess
import com.project.lumina.client.util.NetworkOptimizer
import com.project.lumina.client.util.FirstTimeUserManager
import com.project.lumina.client.viewmodel.MainScreenViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.net.Socket
import com.project.lumina.client.devtools.LogcatService

@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val rightScrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

    val mainScreenViewModel: MainScreenViewModel = viewModel()
    val captureModeModel by mainScreenViewModel.captureModeModel.collectAsState()

    
    val sharedPreferences = context.getSharedPreferences("SettingsPrefs", Context.MODE_PRIVATE)

    
    var optimizeNetworkEnabled by remember {
        mutableStateOf(sharedPreferences.getBoolean("optimizeNetworkEnabled", false))
    }
    var priorityThreadsEnabled by remember {
        mutableStateOf(sharedPreferences.getBoolean("priorityThreadsEnabled", false))
    }
    var fastDnsEnabled by remember {
        mutableStateOf(sharedPreferences.getBoolean("fastDnsEnabled", false))
    }
    var injectNekoPackEnabled by remember {
        mutableStateOf(sharedPreferences.getBoolean("injectNekoPackEnabled", false))
    }

    var disableOpeningAnimation by remember {
        mutableStateOf(sharedPreferences.getBoolean("disableOpeningAnimation", false))
    }
    var selectedGUI by remember {
        mutableStateOf(sharedPreferences.getString("selectedGUI", "KitsuGUI") ?: "KitsuGUI")
    }

    var showPermissionDialog by remember { mutableStateOf(false) }
    var showServerConfigDialog by remember { mutableStateOf(false) }
    var showAppSelectionDialog by remember { mutableStateOf(false) }

    var serverIp by remember { mutableStateOf(captureModeModel.serverHostName) }
    var serverPort by remember { mutableStateOf(captureModeModel.serverPort.toString()) }

    var selectedAppPackage by remember {
        mutableStateOf(sharedPreferences.getString("selectedAppPackage", "com.mojang.minecraftpe") ?: "com.mojang.minecraftpe")
    }
    var installedApps by remember { mutableStateOf<List<PackageInfo>>(emptyList()) }

    var devToolsEnabled by remember {
        mutableStateOf(sharedPreferences.getBoolean("devToolsEnabled", false))
    }

    
    DisposableEffect(Unit) {
        val listener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == "devToolsEnabled") {
                devToolsEnabled = sharedPreferences.getBoolean("devToolsEnabled", false)
            }
        }
        sharedPreferences.registerOnSharedPreferenceChangeListener(listener)
        onDispose {
            sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }

    var removeAccountLimitEnabled by remember {
        mutableStateOf(sharedPreferences.getBoolean("removeAccountLimitEnabled", false))
    }

    var disableAutoStartEnabled by remember {
        mutableStateOf(sharedPreferences.getBoolean("disableAutoStartEnabled", false))
    }

    var disableAuthRequiredEnabled by remember {
        mutableStateOf(sharedPreferences.getBoolean("disableAuthRequiredEnabled", false))
    }

    var enableLogcatEnabled by remember {
        mutableStateOf(sharedPreferences.getBoolean("enableLogcatEnabled", false))
    }

    
    LaunchedEffect(devToolsEnabled) {
        if (!devToolsEnabled) {
            removeAccountLimitEnabled = false
            disableAutoStartEnabled = false
            disableAuthRequiredEnabled = false
            enableLogcatEnabled = false
        } else {
            
            removeAccountLimitEnabled = sharedPreferences.getBoolean("removeAccountLimitEnabled", false)
            disableAutoStartEnabled = sharedPreferences.getBoolean("disableAutoStartEnabled", false)
            disableAuthRequiredEnabled = sharedPreferences.getBoolean("disableAuthRequiredEnabled", false)
            enableLogcatEnabled = sharedPreferences.getBoolean("enableLogcatEnabled", false)
        }
    }

    var showRestartDialog by remember { mutableStateOf(false) }

    
    fun saveToggleState(key: String, value: Boolean) {
        with(sharedPreferences.edit()) {
            putBoolean(key, value)
            apply()
        }
    }

    fun saveGUISelection(value: String) {
        with(sharedPreferences.edit()) {
            putString("selectedGUI", value)
            apply()
        }
    }

    fun saveSelectedApp(packageName: String) {
        with(sharedPreferences.edit()) {
            putString("selectedAppPackage", packageName)
            apply()
        }
        mainScreenViewModel.selectGame(packageName)
    }

    fun getAppName(packageName: String): String {
        return try {
            val appInfo = context.packageManager.getApplicationInfo(packageName, 0)
            context.packageManager.getApplicationLabel(appInfo).toString()
        } catch (e: PackageManager.NameNotFoundException) {
            packageName
        }
    }

    fun getAppVersion(packageName: String): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(packageName, 0)
            packageInfo.versionName ?: "Unknown"
        } catch (e: PackageManager.NameNotFoundException) {
            "Unknown"
        }
    }

    fun getAppIcon(packageName: String): Drawable? {
        return try {
            context.packageManager.getApplicationIcon(packageName)
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
    }

    LaunchedEffect(Unit) {
        mainScreenViewModel.selectGame(selectedAppPackage)

        scope.launch(Dispatchers.IO) {
            try {
                val packageManager = context.packageManager
                val packages = packageManager.getInstalledPackages(0)
                    .filter { packageInfo ->
                        val appInfo = packageInfo.applicationInfo
                        appInfo != null &&
                        (appInfo.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM == 0) &&
                        packageManager.getLaunchIntentForPackage(packageInfo.packageName) != null
                    }
                    .sortedBy { packageInfo ->
                        val appInfo = packageInfo.applicationInfo
                        if (appInfo != null) {
                            packageManager.getApplicationLabel(appInfo).toString()
                        } else {
                            packageInfo.packageName
                        }
                    }
                installedApps = packages
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        
                        DropdownMenu(
                            modifier = Modifier.fillMaxWidth(),
                            options = listOf("GraceGUI", "KitsuGUI", "ProtohaxUi", "ClickGUI", "WClientUI"),
                            selectedOption = selectedGUI,
                            onOptionSelected = { newSelection ->
                                selectedGUI = newSelection
                                saveGUISelection(newSelection)
                            }
                        )

                        Divider()

                        
                        SettingToggle(
                            title = "Optimize Network",
                            description = "Initialize network optimization and configure sockets for better performance",
                            checked = optimizeNetworkEnabled,
                            onCheckedChange = { isEnabled ->
                                if (isEnabled) {
                                    scope.launch(Dispatchers.IO) {
                                        val success = NetworkOptimizer.init(context)
                                        if (success) {
                                            optimizeNetworkEnabled = true
                                            saveToggleState("optimizeNetworkEnabled", true)
                                            val socket = Socket()
                                            NetworkOptimizer.optimizeSocket(socket)
                                        } else {
                                            
                                            scope.launch(Dispatchers.Main) {
                                                showPermissionDialog = true
                                            }
                                        }
                                    }
                                } else {
                                    optimizeNetworkEnabled = false
                                    saveToggleState("optimizeNetworkEnabled", false)
                                }
                            }
                        )

                        Divider()

                        
                        SettingToggle(
                            title = "High Priority Threads",
                            description = "Set application threads to foreground priority for better performance",
                            checked = priorityThreadsEnabled,
                            onCheckedChange = { isEnabled ->
                                priorityThreadsEnabled = isEnabled
                                saveToggleState("priorityThreadsEnabled", isEnabled)
                                scope.launch(Dispatchers.IO) {
                                    if (isEnabled) {
                                        NetworkOptimizer.setThreadPriority()
                                    }
                                }
                            }
                        )

                        Divider()

                        
                        SettingToggle(
                            title = "Use Fast DNS",
                            description = "Use Google's DNS servers for faster name resolution",
                            checked = fastDnsEnabled,
                            onCheckedChange = { isEnabled ->
                                fastDnsEnabled = isEnabled
                                saveToggleState("fastDnsEnabled", isEnabled)
                                scope.launch(Dispatchers.IO) {
                                    if (isEnabled) {
                                        val success = NetworkOptimizer.useFastDNS()
                                        if (!success) {
                                            fastDnsEnabled = false
                                            saveToggleState("fastDnsEnabled", false)
                                        }
                                    }
                                }
                            }
                        )

                        Divider()


                        SettingToggle(
                            title = "Inject Neko Pack",
                            description = "Enable injection of Neko pack for enhanced features",
                            checked = injectNekoPackEnabled,
                            onCheckedChange = { isEnabled ->
                                injectNekoPackEnabled = isEnabled
                                saveToggleState("injectNekoPackEnabled", isEnabled)
                            }
                        )

                        Divider()

                        SettingToggle(
                            title = "Disable Opening Animation",
                            description = "Skip the animated intro when u open the app",
                            checked = disableOpeningAnimation,
                            onCheckedChange = { isEnabled ->
                                disableOpeningAnimation = isEnabled
                                saveToggleState("disableOpeningAnimation", isEnabled)
                            }
                        )
                    }
                }
            }

            
            Column(
                modifier = Modifier
                    .weight(0.8f)
                    .verticalScroll(rightScrollState),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .align(Alignment.CenterHorizontally),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Server Configuration",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )

                        Text(
                            text = "Configure server IP address and port",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "IP: $serverIp",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = "Port: $serverPort",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }

                            Button(
                                onClick = { showServerConfigDialog = true },
                                modifier = Modifier.padding(start = 8.dp)
                            ) {
                                Text("Config")
                            }
                        }
                    }
                }

                if (!disableAutoStartEnabled) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .align(Alignment.CenterHorizontally),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        )
                    ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "App Manager",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )

                        Text(
                            text = "Select the app that will automatically run when starting the service",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                val appIcon = getAppIcon(selectedAppPackage)
                                if (appIcon != null) {
                                    androidx.compose.foundation.Image(
                                        bitmap = appIcon.toBitmap(48, 48).asImageBitmap(),
                                        contentDescription = "App Icon",
                                        modifier = Modifier.size(32.dp)
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Apps,
                                        contentDescription = "Default App Icon",
                                        modifier = Modifier.size(32.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }

                                Column {
                                    Text(
                                        text = getAppName(selectedAppPackage),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "Selected Version: ${getAppVersion(selectedAppPackage)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Button(
                                onClick = { showAppSelectionDialog = true },
                                modifier = Modifier.padding(start = 8.dp)
                            ) {
                                Text("Select")
                            }
                        }
                    }
                    }
                }

                val currentAutoLaunchMode = FirstTimeUserManager.getAutoLaunchMode(context)
                if (currentAutoLaunchMode == "mobile") {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .align(Alignment.CenterHorizontally),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Client Mode",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )

                            Text(
                                text = "Settings for Mobile Client mode",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Start
                            ) {
                                Button(
                                    onClick = {
                                        FirstTimeUserManager.resetFirstTimeUser(context)
                                        Toast.makeText(context, "Auto Launch reset. You'll see the mode selection dialog on next startup.", Toast.LENGTH_LONG).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.errorContainer,
                                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                ) {
                                    Text("Reset Auto Launch")
                                }
                            }
                        }
                    }
                }

                if (devToolsEnabled) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .align(Alignment.CenterHorizontally),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Dev Tools",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )

                            Text(
                                text = "dev utilities for testing and maintenance stuff :3",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Crash App",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "Force an immediate crash for testing",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Button(onClick = {
                                    throw RuntimeException("Crash triggered from Dev Tools")
                                }) {
                                    Text("Crash")
                                }
                            }

                            Divider()

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Clear Cache & Data",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "Wipe app cache and preferences",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Button(onClick = {
                                    try { context.cacheDir.deleteRecursively() } catch (_: Exception) {}
                                    try { context.codeCacheDir.deleteRecursively() } catch (_: Exception) {}
                                    try { context.externalCacheDir?.deleteRecursively() } catch (_: Exception) {}
                                    try { context.filesDir.deleteRecursively() } catch (_: Exception) {}
                                    try {
                                        listOf("SettingsPrefs", "game_settings", "KitsuOverlayPrefs").forEach { name ->
                                            val p = context.getSharedPreferences(name, Context.MODE_PRIVATE)
                                            p.edit().clear().commit()
                                        }
                                    } catch (_: Exception) {}
                                    Toast.makeText(context, "Cache and data cleared", Toast.LENGTH_SHORT).show()
                                    showRestartDialog = true
                                }) {
                                    Text("Clear")
                                }
                            }

                            Divider()

                            SettingToggle(
                                title = "Unlimited Accounts",
                                description = "Remove the 2-account creation limit",
                                checked = removeAccountLimitEnabled,
                                onCheckedChange = { isEnabled ->
                                    removeAccountLimitEnabled = isEnabled
                                    saveToggleState("removeAccountLimitEnabled", isEnabled)
                                }
                            )

                            Divider()

                            SettingToggle(
                                title = "Disable Service AutoStart",
                                description = "Disable automatic app launch when starting the relay service",
                                checked = disableAutoStartEnabled,
                                onCheckedChange = { isEnabled ->
                                    disableAutoStartEnabled = isEnabled
                                    saveToggleState("disableAutoStartEnabled", isEnabled)
                                }
                            )

                            Divider()

                            SettingToggle(
                                title = "Disable Auth-Required",
                                description = "Enable Start button without authentication requirement",
                                checked = disableAuthRequiredEnabled,
                                onCheckedChange = { isEnabled ->
                                    disableAuthRequiredEnabled = isEnabled
                                    saveToggleState("disableAuthRequiredEnabled", isEnabled)
                                }
                            )

                            Divider()

                            SettingToggle(
                                title = "Enable Logcat",
                                description = "Enable background logcat debugging and save logs to Documents/",
                                checked = enableLogcatEnabled,
                                onCheckedChange = { isEnabled ->
                                    enableLogcatEnabled = isEnabled
                                    saveToggleState("enableLogcatEnabled", isEnabled)

                                    if (isEnabled) {
                                        LogcatService.startLogcatService()
                                    } else {
                                        LogcatService.stopLogcatService()
                                    }
                                }
                            )
                        }
                    }
                }

            }
        }
    }

    if (showRestartDialog) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("Restart Required") },
            text = { Text("A restart is needed for this app.") },
            confirmButton = {
                Button(onClick = {
                    val pm = context.packageManager
                    val intent = pm.getLaunchIntentForPackage(context.packageName)
                    if (intent != null) {
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        context.startActivity(intent)
                    }
                    exitProcess(0)
                }) {
                    Text("Restart")
                }
            }
        )
    }
    if (showPermissionDialog) {
        PermissionDialog(
            onDismiss = { showPermissionDialog = false },
            onRequestPermission = {
                NetworkOptimizer.openWriteSettingsPermissionPage(context)
                showPermissionDialog = false
            }
        )
    }

    if (showServerConfigDialog) {
        
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            ServerConfigDialog(
                initialIp = serverIp,
                initialPort = serverPort,
                onDismiss = { showServerConfigDialog = false },
                onSave = { ip, port ->
                    serverIp = ip
                    serverPort = port
                    showServerConfigDialog = false

                    try {
                        val portInt = port.toInt()
                        mainScreenViewModel.selectCaptureModeModel(
                            captureModeModel.copy(serverHostName = ip, serverPort = portInt)
                        )
                        Toast.makeText(context, "Server configuration updated", Toast.LENGTH_SHORT).show()
                    } catch (e: NumberFormatException) {
                        Toast.makeText(context, "Invalid port number", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }
    }

    if (showAppSelectionDialog) {
        AppSelectionDialog(
            installedApps = installedApps,
            selectedAppPackage = selectedAppPackage,
            onDismiss = { showAppSelectionDialog = false },
            onAppSelected = { packageName ->
                selectedAppPackage = packageName
                saveSelectedApp(packageName)
                showAppSelectionDialog = false
                Toast.makeText(context, "App selected: ${getAppName(packageName)}", Toast.LENGTH_SHORT).show()
            },
            getAppName = ::getAppName,
            getAppVersion = ::getAppVersion,
            getAppIcon = ::getAppIcon
        )
    }
}

@Composable
fun ServerConfigDialog(
    initialIp: String,
    initialPort: String,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }
    var ip by remember { mutableStateOf(initialIp) }
    var port by remember { mutableStateOf(initialPort) }
    val coroutineScope = rememberCoroutineScope()

    
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp
    val isCompactScreen = screenWidth < 600.dp

    
    val scale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.85f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "dialogScale"
    )

    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(400, easing = FastOutSlowInEasing),
        label = "dialogAlpha"
    )

    val offsetY by animateFloatAsState(
        targetValue = if (isVisible) 0f else 50f,
        animationSpec = tween(500, easing = FastOutSlowInEasing),
        label = "dialogOffsetY"
    )

    LaunchedEffect(Unit) {
        isVisible = true
    }

    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = (0.4f * alpha.coerceIn(0f, 1f))))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                coroutineScope.launch {
                    isVisible = false
                    delay(300)
                    onDismiss()
                }
            },
        contentAlignment = Alignment.Center
    ) {
        
        Card(
            modifier = Modifier
                .widthIn(min = 280.dp, max = min(screenWidth * 0.9f, 400.dp))
                .heightIn(max = screenHeight * 0.8f)
                .scale(scale)
                .alpha(alpha)
                .offset(y = offsetY.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { /* Consume click to prevent propagation */ },
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 6.dp
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(if (isCompactScreen) 16.dp else 24.dp)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Server Configuration",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    OutlinedTextField(
                        value = ip,
                        onValueChange = { ip = it },
                        label = { Text("IP Address") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Computer,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    )

                    OutlinedTextField(
                        value = port,
                        onValueChange = { port = it },
                        label = { Text("Port") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Dns,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = {
                                coroutineScope.launch {
                                    isVisible = false
                                    delay(300)
                                    onDismiss()
                                }
                            },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            ),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Cancel"
                                )
                                Text("Cancel")
                            }
                        }

                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    isVisible = false
                                    delay(300)
                                    onSave(ip, port)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Apply"
                                )
                                Text("Apply")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PermissionDialog(
    onDismiss: () -> Unit,
    onRequestPermission: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Permission Required") },
        text = { Text("Network optimization requires special permissions. Would you like to open settings to grant them?") },
        confirmButton = {
            Button(onClick = onRequestPermission) {
                Text("Open Settings")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun SettingToggle(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
fun DropdownMenu(
    modifier: Modifier = Modifier,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val rotationState by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "rotation"
    )
    
    
    val elevationState by animateFloatAsState(
        targetValue = if (expanded) 8f else 2f,
        label = "elevation"
    )

    Column(modifier = modifier) {
        Text(
            text = "Overlay GUI",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
        )
        
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .clickable { expanded = !expanded },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = elevationState.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(
                                    when (selectedOption) {
                                        "GraceGUI" -> MaterialTheme.colorScheme.primary
                                        "KitsuGUI" -> MaterialTheme.colorScheme.tertiary
                                        "ProtohaxUi" -> MaterialTheme.colorScheme.error
                                        "ClickGUI" -> MaterialTheme.colorScheme.secondary
                                        else -> MaterialTheme.colorScheme.primary
                                    }
                                )
                        )
                        
                        Text(
                            text = selectedOption,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = if (expanded) "Collapse" else "Expand",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .rotate(rotationState)
                            .size(20.dp)
                    )
                }
            }

            androidx.compose.material3.DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .width(220.dp)
                    .background(
                        MaterialTheme.colorScheme.surfaceContainerHigh,
                        RoundedCornerShape(8.dp)
                    )
            ) {
                options.forEach { option ->
                    val isSelected = option == selectedOption
                    
                    androidx.compose.material3.DropdownMenuItem(
                        text = { 
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(RoundedCornerShape(1.dp))
                                        .background(
                                            when (option) {
                                                "GraceGUI" -> MaterialTheme.colorScheme.primary
                                                "KitsuGUI" -> MaterialTheme.colorScheme.tertiary
                                                "ProtohaxUi" -> MaterialTheme.colorScheme.error
                                                "ClickGUI" -> MaterialTheme.colorScheme.secondary
                                                else -> MaterialTheme.colorScheme.primary
                                            }
                                        )
                                )
                                
                                Text(
                                    text = option,
                                    color = if (isSelected) 
                                        MaterialTheme.colorScheme.primary 
                                    else 
                                        MaterialTheme.colorScheme.onSurface,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                    style = MaterialTheme.typography.bodyMedium
                                ) 
                            }
                        },
                        onClick = {
                            onOptionSelected(option)
                            expanded = false
                        },
                        trailingIcon = {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        },
                        modifier = Modifier
                            .height(36.dp)
                            .background(
                                if (isSelected) 
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f) 
                                else 
                                    Color.Transparent
                            )
                    )
                }
            }
        }
    }
}


@Composable
fun AppSelectionDialog(
    installedApps: List<PackageInfo>,
    selectedAppPackage: String,
    onDismiss: () -> Unit,
    onAppSelected: (String) -> Unit,
    getAppName: (String) -> String,
    getAppVersion: (String) -> String,
    getAppIcon: (String) -> Drawable?
) {
    var isVisible by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val scale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.85f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "dialogScale"
    )

    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(400, easing = FastOutSlowInEasing),
        label = "dialogAlpha"
    )

    val offsetY by animateFloatAsState(
        targetValue = if (isVisible) 0f else 50f,
        animationSpec = tween(500, easing = FastOutSlowInEasing),
        label = "dialogOffsetY"
    )

    LaunchedEffect(Unit) {
        isVisible = true
    }

    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = (0.4f * alpha.coerceIn(0f, 1f))))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                coroutineScope.launch {
                    isVisible = false
                    delay(300)
                    onDismiss()
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .widthIn(min = 320.dp, max = min(screenWidth * 0.9f, 500.dp))
                .heightIn(max = screenHeight * 0.8f)
                .scale(scale)
                .alpha(alpha)
                .offset(y = offsetY.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { /* Consume click to prevent propagation */ },
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 6.dp
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = "Select App",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Choose the app that will automatically launch when starting the service",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(installedApps) { packageInfo ->
                        val packageName = packageInfo.packageName
                        val isSelected = packageName == selectedAppPackage

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    coroutineScope.launch {
                                        isVisible = false
                                        delay(300)
                                        onAppSelected(packageName)
                                    }
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected)
                                    MaterialTheme.colorScheme.primaryContainer
                                else
                                    MaterialTheme.colorScheme.surfaceVariant
                            ),
                            elevation = CardDefaults.cardElevation(
                                defaultElevation = if (isSelected) 4.dp else 2.dp
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                val appIcon = getAppIcon(packageName)
                                if (appIcon != null) {
                                    androidx.compose.foundation.Image(
                                        bitmap = appIcon.toBitmap(48, 48).asImageBitmap(),
                                        contentDescription = "App Icon",
                                        modifier = Modifier.size(40.dp)
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Apps,
                                        contentDescription = "Default App Icon",
                                        modifier = Modifier.size(40.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }

                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = getAppName(packageName),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        color = if (isSelected)
                                            MaterialTheme.colorScheme.onPrimaryContainer
                                        else
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "v${getAppVersion(packageName)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (isSelected)
                                            MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                        else
                                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )
                                }

                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = {
                            coroutineScope.launch {
                                isVisible = false
                                delay(300)
                                onDismiss()
                            }
                        },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cancel"
                            )
                            Text("Cancel")
                        }
                    }
                }
            }
        }
    }
}