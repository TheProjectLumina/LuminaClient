package com.project.lumina.client.router.main.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.project.lumina.client.discord.PresenceState
import com.project.lumina.client.discord.PresenceStateManager
import com.project.lumina.client.discord.RPCService
import com.project.lumina.client.discord.model.DiscordBadge
import com.project.lumina.client.discord.model.DiscordUser
import kotlinx.coroutines.delay
import java.util.Locale
import java.util.concurrent.TimeUnit

private const val DEFAULT_BANNER = "https://discord.com/assets/97ac61a0b98fd6f01b4de370c9ccdb56.png"
private const val NITRO_ICON = "https://cdn.discordapp.com/badge-icons/2ba85e8026a8614b640c2837bcdfe21b.png"
private const val LUMINA_ICON = "https://raw.githubusercontent.com/TheProjectLumina/LuminaClient/main/images/lumina2.jpg"
private val DISCORD_BLURPLE = Color(0xFF5865F2)

private val profileBorderColors = listOf(Color(0xFFa3a1ed), Color(0xFFA77798))
private val profileBackgroundColors = listOf(Color(0xFFC2C0FA), Color(0xFFFADAF0))

@Composable
fun ProfileSection(
    user: DiscordUser?,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Card(
        modifier = modifier
            .widthIn(min = 280.dp, max = 320.dp)
            .verticalScroll(scrollState)
            .clip(RoundedCornerShape(8.dp))
            .background(brush = Brush.verticalGradient(colors = profileBackgroundColors)),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(3.dp, Brush.verticalGradient(colors = profileBorderColors)),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        if (user != null) {
            UserProfileContent(user = user)
        } else {
            PlaceholderProfileContent()
        }
    }
}

@Composable
private fun UserProfileContent(user: DiscordUser) {
    Box {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(user.getBannerUrl() ?: DEFAULT_BANNER)
                .crossfade(true)
                .build(),
            contentDescription = "User Banner",
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            contentScale = ContentScale.Crop
        )
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(user.getAvatarUrl())
                .crossfade(true)
                .build(),
            contentDescription = "${user.getDisplayName()}'s avatar",
            modifier = Modifier
                .padding(12.dp, 50.dp, 12.dp, 4.dp)
                .size(90.dp)
                .border(width = 6.dp, color = profileBorderColors.first(), shape = CircleShape)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
        BadgeRow(
            user = user,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(12.dp, 6.dp)
        )
    }
    Column(
        modifier = Modifier
            .padding(12.dp, 4.dp, 12.dp, 12.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White)
            .fillMaxWidth()
    ) {
        Text(
            text = user.getDisplayName(),
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp
            ),
            color = Color.Black.copy(alpha = 0.9f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(16.dp, 10.dp, 16.dp, 0.dp)
        )
        Text(
            text = user.getFullUsername(),
            style = MaterialTheme.typography.labelSmall,
            color = Color.Black.copy(alpha = 0.7f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(16.dp, 2.dp, 16.dp, 6.dp)
        )
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp)
                .height(1.dp)
                .background(Color(0xFFC2C0FA))
        )
        if (!user.bio.isNullOrEmpty()) {
            Text(
                text = "ABOUT ME",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 11.sp
                ),
                color = Color.Black.copy(alpha = 0.9f),
                modifier = Modifier.padding(16.dp, 8.dp, 16.dp, 2.dp)
            )
            Text(
                text = user.bio,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Black.copy(alpha = 0.7f),
                modifier = Modifier.padding(16.dp, 0.dp, 16.dp, 8.dp)
            )
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp)
                    .height(1.dp)
                    .background(Color(0xFFC2C0FA))
            )
        }
        RpcActivityPreview()
    }
}

@Composable
private fun RpcActivityPreview() {
    val isRpcConnected by RPCService.isConnected.collectAsState()
    val presenceState by PresenceStateManager.currentState.collectAsState()

    val elapsed by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var elapsedState by remember { mutableStateOf("00:00") }

    LaunchedEffect(isRpcConnected) {
        while (isRpcConnected) {
            delay(1000)
            elapsedState = formatElapsedTime(elapsed)
        }
    }

    if (!isRpcConnected) {
        Spacer(modifier = Modifier.height(10.dp))
        return
    }

    val details = getPresenceDetails(presenceState)
    val state = getPresenceState(presenceState)

    Text(
        text = "PLAYING A GAME",
        style = MaterialTheme.typography.titleSmall.copy(
            fontWeight = FontWeight.ExtraBold,
            fontSize = 11.sp
        ),
        color = Color.Black.copy(alpha = 0.9f),
        modifier = Modifier.padding(16.dp, 8.dp, 16.dp, 4.dp)
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp, 4.dp, 16.dp, 10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(DISCORD_BLURPLE),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(LUMINA_ICON)
                    .crossfade(true)
                    .build(),
                contentDescription = "Lumina Icon",
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(6.dp)),
                contentScale = ContentScale.Crop
            )
        }
        Column(
            modifier = Modifier.padding(start = 10.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = "Lumina Client",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                ),
                color = Color.Black.copy(alpha = 0.9f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (details != null) {
                Text(
                    text = details,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    color = Color.Black.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = state,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                color = Color.Black.copy(alpha = 0.7f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "$elapsedState elapsed",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                color = Color.Black.copy(alpha = 0.5f)
            )
        }
    }
}

private fun getPresenceDetails(state: PresenceState): String? {
    return when (state) {
        is PresenceState.PlayingInServer -> state.toDetailsString()
        else -> null
    }
}

private fun getPresenceState(state: PresenceState): String {
    return when (state) {
        is PresenceState.Connecting -> state.toStateString()
        is PresenceState.InSection -> state.toStateString()
        is PresenceState.JoiningServer -> state.toStateString()
        is PresenceState.PlayingInServer -> state.toStateString()
    }
}

private fun formatElapsedTime(startTime: Long): String {
    val elapsed = System.currentTimeMillis() - startTime
    if (elapsed < 0) return "00:00"

    var remainingMs = elapsed
    val hours = TimeUnit.MILLISECONDS.toHours(remainingMs)
    remainingMs -= TimeUnit.HOURS.toMillis(hours)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(remainingMs)
    remainingMs -= TimeUnit.MINUTES.toMillis(minutes)
    val seconds = TimeUnit.MILLISECONDS.toSeconds(remainingMs)

    return if (hours >= 1) {
        String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format(Locale.US, "%02d:%02d", minutes, seconds)
    }
}

@Composable
private fun PlaceholderProfileContent() {
    Box {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .background(Color(0xFFB8B6E8))
        )
        Box(
            modifier = Modifier
                .padding(12.dp, 50.dp, 12.dp, 4.dp)
                .size(90.dp)
                .border(width = 6.dp, color = profileBorderColors.first(), shape = CircleShape)
                .clip(CircleShape)
                .background(Color(0xFFD0CEF0)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "?",
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White
            )
        }
    }
    Column(
        modifier = Modifier
            .padding(12.dp, 4.dp, 12.dp, 12.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White)
            .fillMaxWidth()
    ) {
        Text(
            text = "Not logged in",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp
            ),
            color = Color.Black.copy(alpha = 0.6f),
            modifier = Modifier.padding(16.dp, 10.dp, 16.dp, 0.dp)
        )
        Text(
            text = "Login to see profile",
            style = MaterialTheme.typography.labelSmall,
            color = Color.Black.copy(alpha = 0.4f),
            modifier = Modifier.padding(16.dp, 2.dp, 16.dp, 6.dp)
        )
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp)
                .height(1.dp)
                .background(Color(0xFFC2C0FA))
        )
        Spacer(modifier = Modifier.height(10.dp))
    }
}

@Composable
private fun BadgeRow(
    user: DiscordUser,
    modifier: Modifier = Modifier
) {
    val hasBadges = !user.badges.isNullOrEmpty() || user.nitro
    if (!hasBadges) return

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color.White)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (user.nitro) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(NITRO_ICON)
                    .crossfade(true)
                    .build(),
                contentDescription = "Nitro",
                modifier = Modifier
                    .size(28.dp)
                    .padding(2.dp),
                contentScale = ContentScale.Fit
            )
        }
        user.badges?.take(5)?.forEach { badge ->
            BadgeIcon(badge = badge)
        }
    }
}

@Composable
private fun BadgeIcon(badge: DiscordBadge) {
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(badge.icon)
            .crossfade(true)
            .build(),
        contentDescription = badge.description,
        modifier = Modifier
            .size(28.dp)
            .padding(2.dp),
        contentScale = ContentScale.Fit
    )
}