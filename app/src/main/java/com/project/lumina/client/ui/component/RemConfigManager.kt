package com.project.lumina.client.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Done
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.Upload
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.project.lumina.client.R
import com.project.lumina.client.constructors.GameManager
import com.project.lumina.client.game.module.api.config.ConfigManagerElement
import android.app.Activity
import android.content.Intent
import com.project.lumina.client.activity.MainActivity
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.ActivityResult
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.TextButton
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import com.project.lumina.client.viewmodel.MainScreenViewModel
import com.project.lumina.client.model.CaptureModeModel
import com.project.lumina.client.util.FirstTimeUserManager


data class ServerStateHolder(
    var selectedServerName: String? = null,
    var serverIp: String = "",
    var serverPort: String = ""
)

@Composable
fun RemConfigCategoryContent() {
    val configManagerModule = GameManager.elements.find { it.name == "config_manager" } as? ConfigManagerElement
        ?: return
    val mainScreenViewModel: MainScreenViewModel = viewModel()

    var showZeqaBottomSheet by remember { mutableStateOf(false) }

    Scaffold { paddingValues ->

        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            ConfigManagerCard(configManagerModule)


            ServerManagementCard(
                mainScreenViewModel = mainScreenViewModel,
                onShowZeqaBottomSheet = { showZeqaBottomSheet = true }
            )

            
            Spacer(modifier = Modifier.height(8.dp))

            ServerConfigCard(mainScreenViewModel = mainScreenViewModel)

            val context = LocalContext.current
            val currentAutoLaunchMode = FirstTimeUserManager.getAutoLaunchMode(context)
            if (currentAutoLaunchMode == "remote") {
                AutoLaunchResetCard()
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showZeqaBottomSheet) {
        RemZeqaSubServerBottomSheet(
            onDismiss = { showZeqaBottomSheet = false },
            onSelect = { subServer ->
                mainScreenViewModel.selectCaptureModeModel(
                    mainScreenViewModel.captureModeModel.value.copy(
                        serverHostName = subServer.serverAddress,
                        serverPort = subServer.serverPort
                    )
                )
                showZeqaBottomSheet = false
            }
        )
    }
}

@Composable
private fun ConfigManagerCard(configManagerModule: ConfigManagerElement) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            
            Text(
                text = stringResource(R.string.save_config),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )

            SaveConfigSection(configManagerModule)

            Divider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )

            
            ExpandableLoadSection(configManagerModule)
        }
    }
}

@Composable
private fun SaveConfigSection(configManagerModule: ConfigManagerElement) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        
        SaveConfigButton(configManagerModule)
        
        
        ImportConfigButton()
    }
}

@Composable
private fun SaveConfigButton(configManagerModule: ConfigManagerElement) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        FilledTonalIconButton(
            onClick = {
                configManagerModule.saveConfig()
            },
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                Icons.Rounded.Done,
                contentDescription = "Save configuration",
                tint = MaterialTheme.colorScheme.primary
            )
        }
        
        Spacer(modifier = Modifier.size(12.dp))
        
        Text(
            text = "Save Current Configuration",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun ImportConfigButton() {
    val context = LocalContext.current
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        FilledTonalIconButton(
            onClick = {
                try {
                    
                    Toast.makeText(context, "Opening file picker...", Toast.LENGTH_SHORT).show()
                    
                    com.project.lumina.client.activity.RemoteLinkActivity.launchConfigImport()
                } catch (e: Exception) {
                    Toast.makeText(context, "Error opening file picker: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                Icons.Rounded.Upload,
                contentDescription = "Import configuration",
                tint = MaterialTheme.colorScheme.secondary
            )
        }
        
        Spacer(modifier = Modifier.size(12.dp))
        
        Text(
            text = "Import Configuration",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun ExpandableLoadSection(configManagerModule: ConfigManagerElement) {
    var expanded by remember { mutableStateOf(false) }
    val visibilityState = remember { MutableTransitionState(false) }
    visibilityState.targetState = expanded

    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { expanded = !expanded },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = stringResource(R.string.load_config),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )

        val rotation by animateFloatAsState(
            targetValue = if (expanded) 180f else 0f,
            label = "rotation"
        )

        IconButton(
            onClick = { expanded = !expanded },
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.KeyboardArrowDown,
                contentDescription = if (expanded) "Collapse" else "Expand",
                modifier = Modifier.rotate(rotation)
            )
        }
    }

    
    AnimatedVisibility(
        visibleState = visibilityState,
        enter = fadeIn(animationSpec = tween(150)) + expandVertically(animationSpec = tween(200)),
        exit = fadeOut(animationSpec = tween(150)) + shrinkVertically(animationSpec = tween(200))
    ) {
        if (configManagerModule.configFiles.isEmpty()) {
            EmptyConfigList()
        } else {
            
            val maxHeight = if (configManagerModule.configFiles.size > 3) {
                
                180.dp
            } else {
                
                (configManagerModule.configFiles.size * 60 + 16).dp
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(maxHeight)
                    .padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(configManagerModule.configFiles.size) { index ->
                    val configFile = configManagerModule.configFiles[index]
                    ConfigFileItem(
                        configFile = configFile,
                        isSelected = configFile == configManagerModule.selectedConfig,
                        onLoadClick = { configManagerModule.loadConfig(configFile) },
                        onDeleteClick = { configManagerModule.deleteConfig(configFile) },
                        configManagerModule = configManagerModule
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyConfigList() {
    Box(
        modifier = Modifier
            .height(100.dp)
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "No configuration files found",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Icon(
                imageVector = Icons.Rounded.Add,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
private fun ConfigFileItem(
    configFile: ConfigManagerElement.ConfigFile,
    isSelected: Boolean,
    onLoadClick: () -> Unit,
    onDeleteClick: () -> Unit,
    configManagerModule: ConfigManagerElement
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected)
            MaterialTheme.colorScheme.primaryContainer
        else
            MaterialTheme.colorScheme.surfaceVariant,
        label = "backgroundColor"
    )

    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp) 
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onLoadClick() },
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        shape = RoundedCornerShape(8.dp), 
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 2.dp else 0.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .padding(end = 8.dp)
                )

                Spacer(modifier = Modifier.size(8.dp))
            }

            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = configFile.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )

                Text(
                    text = configFile.getFormattedTime(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            
            Row(
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                
                IconButton(
                    onClick = onLoadClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Done,
                        contentDescription = stringResource(R.string.load),
                        tint = if (isSelected)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }

                
                IconButton(
                    onClick = { configManagerModule.exportConfig(configFile) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Download,
                        contentDescription = stringResource(R.string.export),
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                
                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Delete,
                        contentDescription = stringResource(R.string.delete),
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ServerManagementCard(
    mainScreenViewModel: MainScreenViewModel,
    onShowZeqaBottomSheet: () -> Unit = {}
) {
    val captureModeModel by mainScreenViewModel.captureModeModel.collectAsState()


    val serverState = remember {
        ServerStateHolder(
            serverIp = captureModeModel.serverHostName,
            serverPort = captureModeModel.serverPort.toString()
        )
    }


    LaunchedEffect(captureModeModel) {
        serverState.serverIp = captureModeModel.serverHostName
        serverState.serverPort = captureModeModel.serverPort.toString()
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Server List",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary
            )

            ServerListSection(
                mainScreenViewModel = mainScreenViewModel,
                serverState = serverState,
                onShowZeqaBottomSheet = onShowZeqaBottomSheet
            )
        }
    }
}

@Composable
private fun ServerConfigCard(
    mainScreenViewModel: MainScreenViewModel
) {
    val captureModeModel by mainScreenViewModel.captureModeModel.collectAsState()

    val serverState = remember {
        ServerStateHolder(
            serverIp = captureModeModel.serverHostName,
            serverPort = captureModeModel.serverPort.toString()
        )
    }

    LaunchedEffect(captureModeModel) {
        serverState.serverIp = captureModeModel.serverHostName
        serverState.serverPort = captureModeModel.serverPort.toString()
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Server Config",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary
            )

            ServerConfigSection(mainScreenViewModel, serverState)
        }
    }
}

@Composable
private fun ServerListSection(
    mainScreenViewModel: MainScreenViewModel,
    serverState: ServerStateHolder,
    onShowZeqaBottomSheet: () -> Unit = {}
) {
    val captureModeModel by mainScreenViewModel.captureModeModel.collectAsState()

    val rawServers = listOf(
        Triple("2b2tpe", "2b2tpe.org", 19132),
        Triple("Sega MC", "segamc.net", 19132),
        Triple("The Hive", "geo.hivebedrock.network", 19132),
        Triple("Lifeboat MC", "play.lbsg.net", 19132),
        Triple("Nether Games", "ap.nethergames.org", 19132),
        Triple("Cube Craft", "play.cubecraft.net", 19132),
        Triple("Galaxite", "play.galaxite.net", 19132),
        Triple("Zeqa MC", "zeqa.net", 19132),
        Triple("Vanity", "play.venitymc.com", 19132),
        Triple("PixelBlock", "buzz.pixelblockmc.com", 19132)
    )

    val servers = rawServers.map { (name, address, port) ->
        ServerItem(name, address, port) {
            mainScreenViewModel.selectCaptureModeModel(
                captureModeModel.copy(serverHostName = address, serverPort = port)
            )
        }
    }

    
    LaunchedEffect(captureModeModel) {
        serverState.serverIp = captureModeModel.serverHostName
        serverState.serverPort = captureModeModel.serverPort.toString()
        
        serverState.selectedServerName = servers.find { it.serverAddress == captureModeModel.serverHostName }?.name
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(servers) { server ->
            val isSelected = server.name == serverState.selectedServerName ||
                    captureModeModel.serverHostName == server.serverAddress

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .clickable {
                        if (server.name == "Zeqa MC") {
                            onShowZeqaBottomSheet()
                        } else {
                            serverState.selectedServerName = server.name
                            serverState.serverIp = server.serverAddress
                            serverState.serverPort = server.port.toString()
                            server.onClick()
                        }
                    },
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(8.dp),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = if (isSelected) 2.dp else 0.dp
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = server.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Rounded.Done,
                            contentDescription = "Selected",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ServerConfigSection(
    mainScreenViewModel: MainScreenViewModel,
    serverState: ServerStateHolder
) {
    val captureModeModel by mainScreenViewModel.captureModeModel.collectAsState()
    var showConfigDialog by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = "Current Server",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "IP: ${captureModeModel.serverHostName}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Port: ${captureModeModel.serverPort}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        FilledTonalIconButton(
            onClick = { showConfigDialog = true },
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                Icons.Rounded.ArrowDropDown,
                contentDescription = "Configure Server",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }

    if (showConfigDialog) {
        ServerConfigDialog(
            initialIp = captureModeModel.serverHostName,
            initialPort = captureModeModel.serverPort.toString(),
            onDismiss = { showConfigDialog = false },
            onSave = { ip, port ->
                serverState.serverIp = ip
                serverState.serverPort = port
                showConfigDialog = false
                try {
                    val portInt = port.toInt()
                    mainScreenViewModel.selectCaptureModeModel(
                        captureModeModel.copy(serverHostName = ip, serverPort = portInt)
                    )
                } catch (e: NumberFormatException) {
                    
                }
            }
        )
    }
}

@Composable
private fun ServerConfigDialog(
    initialIp: String,
    initialPort: String,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var ip by remember { mutableStateOf(initialIp) }
    var port by remember { mutableStateOf(initialPort) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Server Configuration") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = ip,
                    onValueChange = { ip = it },
                    label = { Text("IP Address") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = port,
                    onValueChange = { port = it },
                    label = { Text("Port") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        },
        confirmButton = {
            Button(onClick = { onSave(ip, port) }) {
                Text("Apply")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

data class ServerItem(
    val name: String,
    val serverAddress: String,
    val port: Int = 19132,
    val onClick: () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RemZeqaSubServerBottomSheet(
    onDismiss: () -> Unit,
    onSelect: (RemSubServerInfo) -> Unit
) {
    val subServers = listOf(
        RemSubServerInfo("NA1", "North America", "na.zeqa.net", 10001),
        RemSubServerInfo("NA2", "North America", "na.zeqa.net", 10002),
        RemSubServerInfo("NA3", "North America", "na.zeqa.net", 10003),
        RemSubServerInfo("NA4", "North America", "na.zeqa.net", 10004),
        RemSubServerInfo("NA5", "North America", "na.zeqa.net", 10005),
        RemSubServerInfo("NA6", "North America", "na.zeqa.net", 10006),
        RemSubServerInfo("EU1", "Europe", "eu.zeqa.net", 10001),
        RemSubServerInfo("EU2", "Europe", "eu.zeqa.net", 10002),
        RemSubServerInfo("EU3", "Europe", "eu.zeqa.net", 10003),
        RemSubServerInfo("EU4", "Europe", "eu.zeqa.net", 10004),
        RemSubServerInfo("EU5", "Europe", "eu.zeqa.net", 10005),
        RemSubServerInfo("EU6", "Europe", "eu.zeqa.net", 10006),
        RemSubServerInfo("AS1", "Asia", "as.zeqa.net", 10001),
        RemSubServerInfo("AS2", "Asia", "as.zeqa.net", 10002),
        RemSubServerInfo("AS3", "Asia", "as.zeqa.net", 10003),
        RemSubServerInfo("AS4", "Asia", "as.zeqa.net", 10004),
        RemSubServerInfo("AS5", "Asia", "as.zeqa.net", 10005),
        RemSubServerInfo("AS6", "Asia", "as.zeqa.net", 10006),
        RemSubServerInfo("SA1", "South America", "sa.zeqa.net", 10001),
        RemSubServerInfo("AU1", "Australia", "au.zeqa.net", 10001),
        RemSubServerInfo("ZA1", "South Africa", "za.zeqa.net", 10001)
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

@Composable
private fun AutoLaunchResetCard() {
    val context = LocalContext.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.KeyboardArrowUp,
                    contentDescription = "Auto Launch",
                    tint = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = "Auto Launch Settings",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
            }

            Text(
                text = "Reset your connection mode preference to show the selection dialog on next startup",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                Button(
                    onClick = {
                        FirstTimeUserManager.resetFirstTimeUser(context)
                        Toast.makeText(
                            context,
                            "Remote Link Auto Launch reset. Connection mode selection will appear on next startup.",
                            Toast.LENGTH_LONG
                        ).show()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.KeyboardArrowUp,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Reset Auto Launch")
                }
            }
        }
    }
}

data class RemSubServerInfo(
    val id: String,
    val region: String,
    val serverAddress: String,
    val serverPort: Int
)