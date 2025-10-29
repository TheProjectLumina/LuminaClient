/*
 * © Project Lumina 2025 — Licensed under GNU GPLv3
 * You are free to use, modify, and redistribute this code under the terms
 * of the GNU General Public License v3. See the LICENSE file for details.
 */

package com.project.lumina.client.ui.component

import android.content.Context
import android.util.Log
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.project.lumina.client.ui.theme.PColorItem1
import com.project.lumina.client.viewmodel.MainScreenViewModel
import net.lenni0451.commons.httpclient.HttpClient
import net.raphimc.minecraftauth.MinecraftAuth
import net.raphimc.minecraftauth.service.realms.BedrockRealmsService
import net.raphimc.minecraftauth.service.realms.model.RealmsWorld
import net.raphimc.minecraftauth.step.bedrock.session.StepFullBedrockSession
import net.raphimc.minecraftauth.util.MicrosoftConstants

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.io.File
import java.util.concurrent.CompletableFuture
import com.project.lumina.client.overlay.mods.NotificationType
import com.project.lumina.client.overlay.mods.SimpleOverlayNotification
import com.project.lumina.client.util.RealmErrorHandler

data class RealmInfo(
    val id: String,
    val name: String,
    val ownerName: String,
    val maxPlayers: Int,
    val isCompatible: Boolean,
    val isExpired: Boolean,
    val address: String? = null,
    val port: Int = 19132,
    val onClick: () -> Unit
)

@Composable
fun RealmsSelector() {
    val context = LocalContext.current
    val mainScreenViewModel: MainScreenViewModel = viewModel()
    val captureModeModel by mainScreenViewModel.captureModeModel.collectAsState()

    val httpClient = remember { HttpClient() }
    var bedrockSession by remember { mutableStateOf<StepFullBedrockSession.FullBedrockSession?>(null) }
    var realms by remember { mutableStateOf<List<RealmInfo>>(emptyList()) }
    var isFetchingRealms by remember { mutableStateOf(false) }
    var isLoadingSession by remember { mutableStateOf(true) }
    var selectedRealm by remember { mutableStateOf<RealmInfo?>(null) }

    LaunchedEffect(Unit) {
        loadRealmsSession(context, httpClient) { session ->
            bedrockSession = session
            isLoadingSession = false
        }
    }

    LaunchedEffect(bedrockSession) {
        if (bedrockSession != null && realms.isEmpty()) {
            if (!isFetchingRealms) {
                isFetchingRealms = true
                fetchRealmsList(httpClient, bedrockSession) { realmsList, error ->
                    isFetchingRealms = false
                    if (error != null) {
                        SimpleOverlayNotification.show(
                            message = "Error fetching Realms: $error",
                            type = NotificationType.ERROR,
                            durationMs = 3000
                        )
                    } else if (realmsList != null) {
                        realms = realmsList.map { world ->
                            RealmInfo(
                                id = world.id.toString(),
                                name = world.name,
                                ownerName = world.ownerName,
                                maxPlayers = world.maxPlayers,
                                isCompatible = world.isCompatible,
                                isExpired = world.isExpired,
                                onClick = {
                                    if (world.isCompatible && !world.isExpired) {
                                        joinRealmAndSelect(context, httpClient, bedrockSession, world, mainScreenViewModel) { updatedRealm ->
                                            selectedRealm = updatedRealm
                                        }
                                    } else {
                                        SimpleOverlayNotification.show(
                                            message = "Realm is not compatible or expired",
                                            type = NotificationType.WARNING,
                                            durationMs = 3000
                                        )
                                    }
                                }
                            )
                        }

                        val savedAddress = loadSavedRealmSelection(context)
                        savedAddress?.let { address ->
                            selectedRealm = realms.find { it.address == address }
                        }

                        SimpleOverlayNotification.show(
                            message = if (realms.isEmpty()) "No Realms found" else "Fetched ${realms.size} Realms",
                            type = NotificationType.INFO,
                            durationMs = 3000
                        )
                    }
                }
            }
        }
    }

    val fetchRealms = {
        if (!isFetchingRealms && bedrockSession != null) {
            isFetchingRealms = true
            fetchRealmsList(httpClient, bedrockSession) { realmsList, error ->
                isFetchingRealms = false
                if (error != null) {
                    SimpleOverlayNotification.show(
                        message = "Error fetching Realms: $error",
                        type = NotificationType.ERROR,
                        durationMs = 3000
                    )
                } else if (realmsList != null) {
                    realms = realmsList.map { world ->
                        RealmInfo(
                            id = world.id.toString(),
                            name = world.name,
                            ownerName = world.ownerName,
                            maxPlayers = world.maxPlayers,
                            isCompatible = world.isCompatible,
                            isExpired = world.isExpired,
                            onClick = {
                                if (world.isCompatible && !world.isExpired) {
                                    joinRealmAndSelect(context, httpClient, bedrockSession, world, mainScreenViewModel) { updatedRealm ->
                                        selectedRealm = updatedRealm
                                    }
                                } else {
                                    SimpleOverlayNotification.show(
                                        message = "Realm is not compatible or expired",
                                        type = NotificationType.WARNING,
                                        durationMs = 3000
                                    )
                                }
                            }
                        )
                    }

                    val savedAddress = loadSavedRealmSelection(context)
                    savedAddress?.let { address ->
                        selectedRealm = realms.find { it.address == address }
                    }

                    SimpleOverlayNotification.show(
                        message = if (realms.isEmpty()) "No Realms found" else "Fetched ${realms.size} Realms",
                        type = NotificationType.INFO,
                        durationMs = 3000
                    )
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Your Realms",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )

            Button(
                onClick = fetchRealms,
                enabled = !isFetchingRealms && bedrockSession != null,
                modifier = Modifier.height(32.dp),
                contentPadding = PaddingValues(horizontal = 12.dp)
            ) {
                if (isFetchingRealms) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(14.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                }
                Text(
                    text = if (isFetchingRealms) "Fetching..." else "Fetch",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
        ) {
            when {
                isLoadingSession -> {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp
                                    )
                                    Text(
                                        text = "Loading session...",
                                        fontSize = 14.sp,
                                        color = Color.White.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }
                    }
                }

                bedrockSession == null -> {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "Not Logged In",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                    Text(
                                        text = "Please login in Realms section",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }

                realms.isEmpty() && !isFetchingRealms -> {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "No Realms Found",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Use Fetch button to load realms",
                                        fontSize = 12.sp,
                                        color = Color.White.copy(alpha = 0.8f),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }

                else -> {
                    items(realms) { realm ->
                        val isSelected = selectedRealm == realm ||
                                captureModeModel.serverHostName == realm.address

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(72.dp)
                                .clickable {
                                    selectedRealm = realm
                                    realm.onClick()
                                }
                                .border(
                                    width = if (isSelected) 1.dp else 0.dp,
                                    color = if (isSelected) PColorItem1 else Color.Transparent,
                                    shape = MaterialTheme.shapes.medium
                                ),
                            colors = CardDefaults.cardColors(
                                containerColor = if (!realm.isCompatible || realm.isExpired) {
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant
                                }
                            )
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Column(
                                    modifier = Modifier.align(Alignment.CenterStart),
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Text(
                                        text = realm.name,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isSelected) Color.White else Color.White.copy(alpha = 0.9f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "Owner: ${realm.ownerName}",
                                        fontSize = 11.sp,
                                        color = if (isSelected) Color.White.copy(alpha = 0.8f) else Color.White.copy(alpha = 0.6f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "Players: ${realm.maxPlayers} | ${
                                            when {
                                                !realm.isCompatible -> "Incompatible"
                                                realm.isExpired -> "Expired"
                                                else -> "Available"
                                            }
                                        }",
                                        fontSize = 10.sp,
                                        color = if (isSelected) Color.White.copy(alpha = 0.7f) else Color.White.copy(alpha = 0.5f)
                                    )
                                }

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
    }
}

private const val REALMS_TAG = "RealmsSelector"
private const val REALMS_SESSION_FILE = "bedrock_session.json"
private const val REALMS_BEDROCK_CLIENT_VERSION = "1.21.100"
private const val SHARED_PREFS_REALMS = "RealmsPrefs"
private const val KEY_SELECTED_REALM_ADDRESS = "selected_realm_address"
private const val KEY_SELECTED_REALM_PORT = "selected_realm_port"

private val REALMS_BEDROCK_AUTH_FLOW = MinecraftAuth.builder()
    .withClientId(MicrosoftConstants.BEDROCK_ANDROID_TITLE_ID)
    .withScope(MicrosoftConstants.SCOPE_TITLE_AUTH)
    .deviceCode()
    .withDeviceToken("Android")
    .sisuTitleAuthentication(MicrosoftConstants.BEDROCK_XSTS_RELYING_PARTY)
    .buildMinecraftBedrockChainStep(true, true)

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

private fun fetchRealmsList(
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

private fun joinRealmAndSelect(
    context: Context,
    httpClient: HttpClient,
    session: StepFullBedrockSession.FullBedrockSession?,
    world: RealmsWorld,
    mainScreenViewModel: MainScreenViewModel,
    onRealmSelected: (RealmInfo?) -> Unit
) {
    Log.d(REALMS_TAG, "Joining Realm: ${world.name}")

    if (session?.realmsXsts == null) {
        SimpleOverlayNotification.show(
            message = "No authentication session available",
            type = NotificationType.ERROR,
            durationMs = 5000
        )
        return
    }

    try {
        val realmsService = BedrockRealmsService(httpClient, REALMS_BEDROCK_CLIENT_VERSION, session.realmsXsts)

        realmsService.joinWorld(world)
            .thenApply { address -> address.toString() }
            .thenAccept { addressString ->
                Log.d(REALMS_TAG, "Successfully joined Realm: ${world.name}, address: $addressString")

                val parts = addressString.split(":")
                val serverAddress = parts[0]
                val serverPort = if (parts.size > 1) parts[1].toIntOrNull() ?: 19132 else 19132

                saveRealmSelection(context, serverAddress, serverPort)

                val captureModeModel = mainScreenViewModel.captureModeModel.value
                mainScreenViewModel.selectCaptureModeModel(
                    captureModeModel.copy(
                        serverHostName = serverAddress,
                        serverPort = serverPort
                    )
                )

                val updatedRealm = RealmInfo(
                    id = world.id.toString(),
                    name = world.name,
                    ownerName = world.ownerName,
                    maxPlayers = world.maxPlayers,
                    isCompatible = world.isCompatible,
                    isExpired = world.isExpired,
                    address = serverAddress,
                    port = serverPort,
                    onClick = {}
                )
                onRealmSelected(updatedRealm)

                SimpleOverlayNotification.show(
                    message = "Selected Realm: ${world.name}",
                    type = NotificationType.SUCCESS,
                    durationMs = 3000
                )
            }
            .exceptionally { throwable ->
                Log.e(REALMS_TAG, "Error joining Realm ${world.name}", throwable)
                SimpleOverlayNotification.show(
                    message = RealmErrorHandler.translateJoinError(throwable),
                    type = NotificationType.ERROR,
                    durationMs = 5000
                )
                null
            }
    } catch (e: Exception) {
        Log.e(REALMS_TAG, "Exception creating BedrockRealmsService", e)
        SimpleOverlayNotification.show(
            message = RealmErrorHandler.translateJoinError(e),
            type = NotificationType.ERROR,
            durationMs = 5000
        )
    }
}

private fun saveRealmSelection(context: Context, address: String, port: Int) {
    try {
        val sharedPrefs = context.getSharedPreferences(SHARED_PREFS_REALMS, Context.MODE_PRIVATE)
        sharedPrefs.edit()
            .putString(KEY_SELECTED_REALM_ADDRESS, address)
            .putInt(KEY_SELECTED_REALM_PORT, port)
            .apply()
        Log.d(REALMS_TAG, "Saved realm selection: $address:$port")
    } catch (e: Exception) {
        Log.e(REALMS_TAG, "Error saving realm selection", e)
    }
}

private fun loadSavedRealmSelection(context: Context): String? {
    return try {
        val sharedPrefs = context.getSharedPreferences(SHARED_PREFS_REALMS, Context.MODE_PRIVATE)
        val address = sharedPrefs.getString(KEY_SELECTED_REALM_ADDRESS, null)
        val port = sharedPrefs.getInt(KEY_SELECTED_REALM_PORT, 19132)
        if (address != null) "$address:$port" else null
    } catch (e: Exception) {
        Log.e(REALMS_TAG, "Error loading realm selection", e)
        null
    }
}