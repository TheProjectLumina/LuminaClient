package com.project.lumina.client.router.main.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.project.lumina.client.discord.PresenceStateManager

@Composable
fun RpcControlsSection(
    isLoggedIn: Boolean,
    isRpcConnected: Boolean,
    isRpcConnecting: Boolean,
    onRpcToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val controlsAlpha = if (isLoggedIn) 1f else 0.5f

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .alpha(controlsAlpha)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Enable Rich Presence",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Show your activity status on Discord",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (isRpcConnecting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Switch(
                    checked = isRpcConnected,
                    onCheckedChange = { if (isLoggedIn && !isRpcConnecting) onRpcToggle(it) },
                    enabled = isLoggedIn && !isRpcConnecting
                )
            }
        }
    }
}

@Composable
fun ServerVisibilitySection(
    isLoggedIn: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val controlsAlpha = if (isLoggedIn) 1f else 0.5f
    val showServerInfo by PresenceStateManager.showServerInfo.collectAsState()
    val showJoinButton by PresenceStateManager.showJoinButton.collectAsState()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .alpha(controlsAlpha),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Server Visibility",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Display server IP and username in presence",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = showServerInfo,
                onCheckedChange = { PresenceStateManager.setShowServerInfo(context, it) },
                enabled = isLoggedIn
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Join Button",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Show join server button when playing",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = showJoinButton,
                onCheckedChange = { PresenceStateManager.setShowJoinButton(context, it) },
                enabled = isLoggedIn
            )
        }
    }
}