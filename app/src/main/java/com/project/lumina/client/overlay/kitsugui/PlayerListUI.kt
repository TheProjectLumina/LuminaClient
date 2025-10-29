package com.project.lumina.client.overlay.kitsugui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.project.lumina.client.constructors.GameDataManager
import com.project.lumina.client.constructors.GameManager
import com.project.lumina.client.ui.theme.TheNotBackgroundColorForOverlayUi
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

@Composable
fun PlayerListUI() {
    var players by remember { mutableStateOf<List<GameDataManager.PlayerInfo>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        while (true) {
            try {
                val currentPlayers = GameManager.netBound?.getCurrentPlayers() ?: emptyList()
                players = currentPlayers
                isLoading = false
            } catch (e: Exception) {
                isLoading = false
            }
            delay(2.seconds)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(6.dp) 
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                TheNotBackgroundColorForOverlayUi.copy(alpha = 0.2f),
                                TheNotBackgroundColorForOverlayUi.copy(alpha = 0.1f)
                            )
                        )
                    )
                    .border(
                        1.dp,
                        TheNotBackgroundColorForOverlayUi.copy(alpha = 0.3f),
                        RoundedCornerShape(10.dp)
                    )
                    .padding(8.dp) 
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp) 
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp) 
                                .clip(CircleShape)
                                .background(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            TheNotBackgroundColorForOverlayUi.copy(alpha = 0.8f),
                                            TheNotBackgroundColorForOverlayUi.copy(alpha = 0.4f)
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Groups,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(12.dp) 
                            )
                        }

                        Text(
                            text = "Players",
                            fontSize = 14.sp, 
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        TheNotBackgroundColorForOverlayUi.copy(alpha = 0.6f),
                                        TheNotBackgroundColorForOverlayUi.copy(alpha = 0.4f)
                                    )
                                )
                            )
                            .border(
                                1.dp,
                                TheNotBackgroundColorForOverlayUi.copy(alpha = 0.5f),
                                RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 3.dp) 
                    ) {
                        Text(
                            text = "${players.size}",
                            fontSize = 10.sp, 
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp)) 

            when {
                isLoading -> LoadingState()
                players.isEmpty() -> EmptyState()
                else -> PlayersList(players)
            }
        }
    }
}

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF1E1E1E).copy(alpha = 0.85f))
            .border(
                1.dp,
                TheNotBackgroundColorForOverlayUi.copy(alpha = 0.3f),
                RoundedCornerShape(10.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp), 
                strokeWidth = 1.5.dp,
                color = TheNotBackgroundColorForOverlayUi
            )
            Text(
                text = "Loading players...",
                fontSize = 12.sp, 
                color = Color.White.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun EmptyState() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF1E1E1E).copy(alpha = 0.85f))
            .border(
                1.dp,
                TheNotBackgroundColorForOverlayUi.copy(alpha = 0.3f),
                RoundedCornerShape(10.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.PersonOutline,
                contentDescription = null,
                modifier = Modifier.size(32.dp), 
                tint = Color.White.copy(alpha = 0.6f)
            )
            Text(
                text = "No players online",
                fontSize = 14.sp, 
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.8f)
            )
            Text(
                text = "Join a server to see players",
                fontSize = 10.sp, 
                color = Color.White.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun PlayersList(players: List<GameDataManager.PlayerInfo>) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(4.dp), 
        modifier = Modifier.fillMaxSize()
    ) {
        items(players) { player ->
            ModernPlayerCard(player = player)
        }
    }
}

@Composable
fun ModernPlayerCard(player: GameDataManager.PlayerInfo) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val animatedScale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = tween(150),
        label = "playerCardScale"
    )

    val animatedBackgroundColor by animateColorAsState(
        targetValue = if (isPressed)
            Color(0xFF2A2A2A).copy(alpha = 0.95f)
        else
            Color(0xFF1E1E1E).copy(alpha = 0.85f),
        animationSpec = tween(200),
        label = "playerCardBackground"
    )

    val animatedBorderColor by animateColorAsState(
        targetValue = if (isPressed)
            TheNotBackgroundColorForOverlayUi.copy(alpha = 0.8f)
        else
            TheNotBackgroundColorForOverlayUi.copy(alpha = 0.4f),
        animationSpec = tween(200),
        label = "playerCardBorder"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .scale(animatedScale)
            .clip(RoundedCornerShape(8.dp)) 
            .background(animatedBackgroundColor)
            .border(
                1.dp,
                animatedBorderColor,
                RoundedCornerShape(8.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { /* Handle player click */ }
            .padding(8.dp) 
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp) 
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp) 
                    .clip(CircleShape)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                getPlayerColor(player).copy(alpha = 0.9f),
                                getPlayerColor(player).copy(alpha = 0.6f)
                            )
                        )
                    )
                    .border(
                        1.dp, 
                        getPlayerColor(player).copy(alpha = 0.5f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getPlayerIcon(player),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp) 
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp) 
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp) 
                ) {
                    Text(
                        text = player.name,
                        fontSize = 12.sp, 
                        fontWeight = FontWeight.Medium,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    ModernPlayerRoleBadges(player)
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp), 
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PlayerDetailChip(
                        text = "ID: ${player.entityId}",
                        color = TheNotBackgroundColorForOverlayUi.copy(alpha = 0.6f)
                    )

                    if (player.buildPlatform != 0) {
                        PlayerDetailChip(
                            text = getPlatformName(player.buildPlatform),
                            color = Color(0xFF4A90E2).copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PlayerDetailChip(text: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp)) 
            .background(color)
            .padding(horizontal = 4.dp, vertical = 2.dp) 
    ) {
        Text(
            text = text,
            fontSize = 8.sp, 
            color = Color.White.copy(alpha = 0.9f),
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun ModernPlayerRoleBadges(player: GameDataManager.PlayerInfo) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(3.dp) 
    ) {
        if (player.isHost) {
            ModernRoleBadge(
                text = "HOST",
                color = Color(0xFFFF4757),
                icon = Icons.Default.Star
            )
        }

        if (player.isTeacher) {
            ModernRoleBadge(
                text = "TEACHER",
                color = Color(0xFF3742FA),
                icon = Icons.Default.School
            )
        }

        if (player.isSubClient) {
            ModernRoleBadge(
                text = "SUB",
                color = Color(0xFF8E44AD),
                icon = null
            )
        }
    }
}

@Composable
fun ModernRoleBadge(
    text: String,
    color: Color,
    icon: ImageVector? = null
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        color.copy(alpha = 0.8f),
                        color.copy(alpha = 0.6f)
                    )
                )
            )
            .border(
                1.dp,
                color.copy(alpha = 0.4f),
                RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 4.dp, vertical = 2.dp) 
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp) 
        ) {
            icon?.let {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(8.dp) 
                )
            }
            Text(
                text = text,
                color = Color.White,
                fontSize = 8.sp, 
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private fun getPlayerColor(player: GameDataManager.PlayerInfo): Color {
    return when {
        player.isHost -> Color(0xFFFF4757)
        player.isTeacher -> Color(0xFF3742FA)
        player.isSubClient -> Color(0xFF8E44AD)
        else -> Color(0xFF2ED573)
    }
}

private fun getPlayerIcon(player: GameDataManager.PlayerInfo): ImageVector {
    return when {
        player.isHost -> Icons.Default.Star
        player.isTeacher -> Icons.Default.School
        else -> Icons.Default.Person
    }
}

private fun getPlatformName(buildPlatform: Int): String {
    return when (buildPlatform) {
        1 -> "Android"
        2 -> "iOS"
        3 -> "macOS"
        4 -> "FireOS"
        5 -> "GearVR"
        6 -> "HoloLens"
        7 -> "Win10"
        8 -> "Win32"
        9 -> "Server"
        10 -> "tvOS"
        11 -> "PS"
        12 -> "Switch"
        13 -> "Xbox"
        14 -> "WinPhone"
        else -> "Unknown"
    }
}

@Preview(showBackground = true)
@Composable
fun ModernPlayerListUIPreview() {
    PlayerListUI()
}