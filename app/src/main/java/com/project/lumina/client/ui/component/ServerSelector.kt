/*
 * © Project Lumina 2026 — Licensed under GNU GPLv3
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

package com.project.lumina.client.ui.component

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.project.lumina.client.R
import com.project.lumina.client.data.CustomServer
import com.project.lumina.client.data.CustomServerManager
import com.project.lumina.client.ui.theme.PColorItem1
import com.project.lumina.client.viewmodel.MainScreenViewModel


data class Server(
    val name: String,
    val serverAddress: String,
    val port: Int = 19132,
    val onClick: () -> Unit
)

data class SubServerInfo(
    val id: String,
    val region: String,
    val serverAddress: String,
    val serverPort: Int
)

@Composable
fun ServerSelector(
    onShowZeqaBottomSheet: () -> Unit = {},
    onShowAddServerDialog: () -> Unit = {},
    onShowEditServerDialog: (CustomServer) -> Unit = {},
    refreshTrigger: Int = 0
) {
    val mainScreenViewModel: MainScreenViewModel = viewModel()
    val captureModeModel by mainScreenViewModel.captureModeModel.collectAsState()
    val customServerManager = remember { CustomServerManager.getInstance() }

    var customServers by remember { mutableStateOf(customServerManager.getServers()) }
    var selectedServer by remember { mutableStateOf<Server?>(null) }
    var selectedCustomServer by remember { mutableStateOf<CustomServer?>(null) }

    LaunchedEffect(refreshTrigger) {
        customServers = customServerManager.getServers()
    }

    val rawServers = listOf(
        Triple("2b2tpe", "2b2tpe.org", 19132),
        Triple("Sega MC", "segamc.net", 19132),
        Triple("The Hive", "geo.hivebedrock.network", 19132),
        Triple("Lifeboat MC", "play.lbsg.net", 19132),
        Triple("Nether Games", "ap.nethergames.org", 19132),
        Triple("Cube Craft", "play.cubecraft.net", 19132),
        Triple("Galaxite", "play.galaxite.net", 19132),
        Triple("Zeqa MC", "zeqa.net", 19132),
        Triple("Venity", "play.venitymc.com", 19132),
        Triple("PixelBlock", "buzz.pixelblockmc.com", 19132)
    )

    val servers = rawServers.map { (name, address, port) ->
        Server(name, address, port) {
            selectedCustomServer = null
            mainScreenViewModel.selectCaptureModeModel(
                captureModeModel.copy(serverHostName = address, serverPort = port)
            )
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 8.dp)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Servers",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )

                    Button(
                        onClick = onShowAddServerDialog,
                        modifier = Modifier.height(32.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White.copy(alpha = 0.15f),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp)
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Add Server",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            if (customServers.isNotEmpty()) {
                item {
                    Column {
                        Text(
                            text = "My Servers",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White.copy(alpha = 0.8f),
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )

                        HorizontalDivider(
                            color = Color.White.copy(alpha = 0.3f),
                            thickness = 1.dp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                }

                items(customServers) { customServer ->
                    CustomServerCard(
                        server = customServer,
                        isSelected = selectedCustomServer?.id == customServer.id,
                        onSelect = {
                            selectedServer = null
                            selectedCustomServer = customServer
                            mainScreenViewModel.selectCaptureModeModel(
                                captureModeModel.copy(
                                    serverHostName = customServer.serverAddress,
                                    serverPort = customServer.port
                                )
                            )
                        },
                        onEdit = {
                            onShowEditServerDialog(customServer)
                        },
                        onDelete = {
                            customServerManager.deleteServer(customServer.id)
                            customServers = customServerManager.getServers()
                            if (selectedCustomServer?.id == customServer.id) {
                                selectedCustomServer = null
                            }
                        }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Public Servers",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White.copy(alpha = 0.8f),
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    HorizontalDivider(
                        color = Color.White.copy(alpha = 0.3f),
                        thickness = 1.dp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
            }
            items(servers) { server ->
                val isSelected = (server == selectedServer ||
                        captureModeModel.serverHostName == server.serverAddress) &&
                        selectedCustomServer == null

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clickable {
                            if (server.name == "Zeqa MC") {
                                onShowZeqaBottomSheet()
                            } else {
                                selectedServer = server
                                selectedCustomServer = null
                                server.onClick()
                            }
                        }
                        .border(
                            width = if (isSelected) 1.dp else 0.dp,
                            color = if (isSelected) PColorItem1 else Color.Transparent,
                            shape = MaterialTheme.shapes.medium
                        ),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = server.name,
                            fontSize = 14.sp,
                            color = if (isSelected) Color.White else Color.White.copy(alpha = 0.8f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        if (isSelected) {
                            Text(
                                text = "✓",
                                fontSize = 16.sp,
                                color = PColorItem1,
                                modifier = Modifier.align(Alignment.CenterEnd)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CustomServerCard(
    server: CustomServer,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clickable { onSelect() }
            .border(
                width = if (isSelected) 1.dp else 0.dp,
                color = if (isSelected) PColorItem1 else Color.Transparent,
                shape = MaterialTheme.shapes.medium
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = server.name,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit Server",
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(16.dp)
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete Server",
                        tint = Color.Red.copy(alpha = 0.7f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

