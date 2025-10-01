package com.project.lumina.client.ui.component

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.project.lumina.client.data.CustomServer
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AddServerDialog(
    editingServer: CustomServer? = null,
    onDismiss: () -> Unit,
    onSave: (CustomServer) -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }
    var serverName by remember(editingServer) { 
        mutableStateOf(editingServer?.name ?: "") 
    }
    var serverAddress by remember(editingServer) { 
        mutableStateOf(editingServer?.serverAddress ?: "") 
    }
    var serverPort by remember(editingServer) { 
        mutableStateOf(editingServer?.port?.toString() ?: "19132") 
    }
    val coroutineScope = rememberCoroutineScope()
    val focusRequester = remember { FocusRequester() }
    
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
                .widthIn(min = 280.dp, max = minOf(screenWidth * 0.9f, 400.dp))
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
                        text = if (editingServer != null) "Edit Server" else "Add Server",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                
                    OutlinedTextField(
                        value = serverName,
                        onValueChange = { serverName = it },
                        label = { Text("Server Name") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Storage,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                    )
                
                    OutlinedTextField(
                        value = serverAddress,
                        onValueChange = { serverAddress = it },
                        label = { Text("Server Address") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Computer,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                    )
                
                    OutlinedTextField(
                        value = serverPort,
                        onValueChange = {
                            if (it.all { char -> char.isDigit() } && it.length <= 5) {
                                serverPort = it
                            }
                        },
                        label = { Text("Port") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Dns,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (serverName.isNotBlank() && serverAddress.isNotBlank() && serverPort.isNotBlank()) {
                                val port = serverPort.toIntOrNull() ?: 19132
                                val server = if (editingServer != null) {
                                    editingServer.copy(
                                        name = serverName.trim(),
                                        serverAddress = serverAddress.trim(),
                                        port = port
                                    )
                                } else {
                                    CustomServer(
                                        name = serverName.trim(),
                                        serverAddress = serverAddress.trim(),
                                        port = port
                                    )
                                }
                                onSave(server)
                            }
                        }
                    )
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
                                if (serverName.isNotBlank() && serverAddress.isNotBlank() && serverPort.isNotBlank()) {
                                    val port = serverPort.toIntOrNull() ?: 19132
                                    val server = if (editingServer != null) {
                                        editingServer.copy(
                                            name = serverName.trim(),
                                            serverAddress = serverAddress.trim(),
                                            port = port
                                        )
                                    } else {
                                        CustomServer(
                                            name = serverName.trim(),
                                            serverAddress = serverAddress.trim(),
                                            port = port
                                        )
                                    }
                                    coroutineScope.launch {
                                        isVisible = false
                                        delay(300)
                                        onSave(server)
                                    }
                                }
                            },
                            enabled = serverName.isNotBlank() && serverAddress.isNotBlank() && serverPort.isNotBlank(),
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
                                    contentDescription = if (editingServer != null) "Update" else "Add"
                                )
                                Text(if (editingServer != null) "Update" else "Add")
                            }
                        }
                    }
                }
            }
        }
    }
}