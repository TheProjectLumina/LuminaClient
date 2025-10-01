package com.project.lumina.client.activity

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.amplitude.android.Amplitude
import com.amplitude.android.Configuration
import com.amplitude.android.DefaultTrackingOptions
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.lenni0451.commons.httpclient.HttpClient
import net.raphimc.minecraftauth.MinecraftAuth
import net.raphimc.minecraftauth.service.realms.BedrockRealmsService
import net.raphimc.minecraftauth.service.realms.model.RealmsWorld
import net.raphimc.minecraftauth.step.bedrock.session.StepFullBedrockSession
import net.raphimc.minecraftauth.step.msa.StepMsaDeviceCode
import net.raphimc.minecraftauth.util.MicrosoftConstants

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeoutException
import com.project.lumina.client.util.RealmErrorHandler
import com.project.lumina.client.util.TrackUtil
import java.util.concurrent.TimeUnit

class RealmsActivity : ComponentActivity() {

    companion object {
        private const val TAG = "MinecraftAuthApp"
        private const val SESSION_FILE = "bedrock_session.json"
        private const val BEDROCK_CLIENT_VERSION = "1.21.100"

        private val BEDROCK_REALMS_AUTH_FLOW = MinecraftAuth.builder()
            .withClientId(MicrosoftConstants.BEDROCK_ANDROID_TITLE_ID)
            .withScope(MicrosoftConstants.SCOPE_TITLE_AUTH)
            .deviceCode()
            .withDeviceToken("Android")
            .sisuTitleAuthentication(MicrosoftConstants.BEDROCK_XSTS_RELYING_PARTY)
            .buildMinecraftBedrockChainStep(true, true) 
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val amplitude = Amplitude(
            Configuration(
                apiKey = TrackUtil.TRACK_API,
                context = applicationContext,
                defaultTracking = DefaultTrackingOptions.ALL,
            )
        )
        amplitude.track("Realm Activity")

        setContent {
            MinecraftAuthApp()
        }
    }

    @Composable
    fun MinecraftAuthApp() {
        val context = LocalContext.current
        val clipboardManager = LocalClipboardManager.current
        val httpClient = remember { MinecraftAuth.createHttpClient() }
        var bedrockSession by remember { mutableStateOf<StepFullBedrockSession.FullBedrockSession?>(null) }
        var realms by remember { mutableStateOf<List<RealmsWorld>>(emptyList()) }
        var statusMessage by remember { mutableStateOf("Loading session...") }
        var isLoginButtonEnabled by remember { mutableStateOf(true) }
        var isFetchRealmsButtonEnabled by remember { mutableStateOf(false) }
        var isLogoutButtonEnabled by remember { mutableStateOf(false) }
        var userCode by remember { mutableStateOf<String?>(null) }
        var verificationUri by remember { mutableStateOf<String?>(null) }
        var realmAddresses by remember { mutableStateOf<Map<Long, String>>(emptyMap()) }
        val coroutineScope = rememberCoroutineScope()

        LaunchedEffect(Unit) {
            coroutineScope.launch(Dispatchers.IO) {
                val session = loadSavedSession(context, httpClient)
                withContext(Dispatchers.Main) {
                    bedrockSession = session
                    isFetchRealmsButtonEnabled = session != null && session.realmsXsts != null
                    isLogoutButtonEnabled = session != null
                    statusMessage = when {
                        session == null -> "No valid session found. Please login."
                        session.realmsXsts == null -> "Session loaded but Realms token missing. Please login again."
                        else -> "Loaded session for ${session.mcChain.displayName}"
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = statusMessage,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (userCode != null && verificationUri != null) {
                Text(
                    text = "User Code: $userCode",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                        .clickable {
                            clipboardManager.setText(AnnotatedString(userCode!!))
                            Toast.makeText(context, "Code copied to clipboard", Toast.LENGTH_SHORT).show()
                        }
                )
                Text(
                    text = "Verification URL: $verificationUri",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                        .clickable {
                            clipboardManager.setText(AnnotatedString(verificationUri!!))
                            Toast.makeText(context, "URL copied to clipboard", Toast.LENGTH_SHORT).show()
                        }
                )
                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(verificationUri))
                        context.startActivity(intent)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Text("Open in Browser")
                }
            }

            Button(
                onClick = {
                    isLoginButtonEnabled = false
                    statusMessage = "Initiating login..."
                    Log.d(TAG, "Starting login flow")
                    startLoginFlow(httpClient) { session, code, uri, error ->
                        isLoginButtonEnabled = true
                        if (error != null) {
                            statusMessage = "Login failed: $error"
                            userCode = null
                            verificationUri = null
                            Log.e(TAG, "Login failed: $error")
                        } else if (session != null) {
                            bedrockSession = session
                            coroutineScope.launch(Dispatchers.IO) {
                                saveSession(context, session)
                                withContext(Dispatchers.Main) {
                                    isFetchRealmsButtonEnabled = session.realmsXsts != null
                                    isLogoutButtonEnabled = true
                                    statusMessage = if (session.realmsXsts != null) {
                                        "Login successful! Username: ${session.mcChain.displayName}"
                                    } else {
                                        "Login successful but Realms token missing. Try again."
                                    }
                                    userCode = null
                                    verificationUri = null
                                    Log.d(TAG, "Login successful, username: ${session.mcChain.displayName}, realmsXsts: ${session.realmsXsts != null}")
                                }
                            }
                        } else {
                            userCode = code
                            verificationUri = uri
                            statusMessage = "Please authenticate in your browser"
                            Log.d(TAG, "Login code generated: $code, URL: $uri")
                        }
                    }
                },
                enabled = isLoginButtonEnabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text("Login with Microsoft")
            }

            Button(
                onClick = {
                    isFetchRealmsButtonEnabled = false
                    statusMessage = "Fetching Realms..."
                    Log.d(TAG, "Fetching Realms")
                    fetchRealms(httpClient, bedrockSession) { worlds, error ->
                        isFetchRealmsButtonEnabled = bedrockSession != null && bedrockSession?.realmsXsts != null
                        if (error != null) {
                            statusMessage = "Error fetching Realms: $error"
                            Log.e(TAG, "Error fetching Realms: $error")
                        } else {
                            realms = worlds ?: emptyList()
                            statusMessage = if (realms.isEmpty()) {
                                "No Realms found"
                            } else {
                                "Fetched ${realms.size} Realms"
                            }
                            Log.d(TAG, "Fetched ${realms.size} Realms")
                        }
                    }
                },
                enabled = isFetchRealmsButtonEnabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text("Fetch Realms")
            }

            Button(
                onClick = {
                    coroutineScope.launch(Dispatchers.IO) {
                        deleteSession(context)
                        withContext(Dispatchers.Main) {
                            bedrockSession = null
                            isFetchRealmsButtonEnabled = false
                            isLogoutButtonEnabled = false
                            realms = emptyList()
                            realmAddresses = emptyMap()
                            statusMessage = "Logged out. Please login again."
                            Log.d(TAG, "Logged out, session file deleted")
                        }
                    }
                },
                enabled = isLogoutButtonEnabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text("Logout")
            }

            if (realms.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    items(realms) { world ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            elevation = CardDefaults.cardElevation(4.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "${world.ownerName} - ${world.name} (${world.state})",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = "MOTD: ${world.motd}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = "Version: ${world.activeVersion}, Players: ${world.maxPlayers}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = if (world.isCompatible && !world.isExpired) "Compatible" else "Incompatible/Expired",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (world.isCompatible && !world.isExpired) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.error
                                    }
                                )
                                realmAddresses[world.id]?.let { address ->
                                    Text(
                                        text = "Server Address: $address",
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier
                                            .padding(top = 8.dp)
                                            .clickable {
                                                clipboardManager.setText(AnnotatedString(address))
                                                Toast.makeText(context, "Address copied to clipboard", Toast.LENGTH_SHORT).show()
                                            }
                                    )
                                }
                                Button(
                                    onClick = {
                                        statusMessage = "Joining Realm: ${world.name}..."
                                        Log.d(TAG, "Joining Realm: ${world.name}")
                                        joinRealm(httpClient, bedrockSession, world) { address, error ->
                                            if (error != null) {
                                                statusMessage = "Error joining Realm: $error"
                                                Log.e(TAG, "Error joining Realm: $error")
                                            } else if (address != null) {
                                                realmAddresses = realmAddresses + (world.id to address)
                                                statusMessage = "Joined Realm: ${world.name}"
                                                Log.d(TAG, "Joined Realm: ${world.name}, Address: $address")
                                            }
                                        }
                                    },
                                    enabled = world.isCompatible && !world.isExpired && bedrockSession?.realmsXsts != null,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp)
                                ) {
                                    Text("Join Realm")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun startLoginFlow(
        httpClient: HttpClient,
        callback: (StepFullBedrockSession.FullBedrockSession?, String?, String?, String?) -> Unit
    ) {
        CompletableFuture.supplyAsync {
            try {
                
                val authFlow = BEDROCK_REALMS_AUTH_FLOW

                Log.d(TAG, "Auth flow created with Realms support")
                Log.d(TAG, "Initiating device code authentication flow")

                val session = authFlow.getFromInput(httpClient, StepMsaDeviceCode.MsaDeviceCodeCallback { msaDeviceCode ->
                    Log.d(TAG, "Device code callback - User code: ${msaDeviceCode.userCode}")
                    runOnUiThread {
                        callback(null, msaDeviceCode.userCode, msaDeviceCode.verificationUri, null)
                    }
                }) as StepFullBedrockSession.FullBedrockSession

                Log.d(TAG, "Authentication flow completed successfully")
                Log.d(TAG, "Session components:")
                Log.d(TAG, "  MCChain: ${session.mcChain != null}")
                Log.d(TAG, "  PlayFab: ${session.playFabToken != null}")
                Log.d(TAG, "  RealmsXsts: ${session.realmsXsts != null}")

                
                if (session.mcChain == null) {
                    throw IllegalStateException("Authentication failed: MCChain is missing")
                }
                if (session.playFabToken == null) {
                    throw IllegalStateException("Authentication failed: PlayFab token is missing")
                }
                if (session.realmsXsts == null) {
                    Log.e(TAG, "Critical: Authentication succeeded but realmsXsts token is missing!")
                    Log.e(TAG, "This means the auth flow didn't properly request Realms permissions")
                    throw IllegalStateException("Authentication succeeded but realmsXsts token is missing. Realms functionality will not work.")
                }

                Log.d(TAG, "All session components verified successfully")
                session
            } catch (e: Exception) {
                Log.e(TAG, "Login failed with exception", e)
                runOnUiThread {
                    callback(null, null, null, e.message ?: "Unknown error")
                }
                null
            }
        }.thenAccept { session ->
            if (session != null) {
                runOnUiThread {
                    callback(session, null, null, null)
                }
            }
        }
    }

    private fun saveSession(context: Context, session: StepFullBedrockSession.FullBedrockSession) {
        try {
            
            val json = BEDROCK_REALMS_AUTH_FLOW.toJson(session)
            val file = File(context.filesDir, SESSION_FILE)
            FileOutputStream(file).use { fos ->
                fos.write(json.toString().toByteArray())
                Log.d(TAG, "Session saved to ${file.absolutePath}")
                Log.d(TAG, "Session size: ${file.length()} bytes")
                Log.d(TAG, "Has realmsXsts: ${session.realmsXsts != null}")

                
                val jsonObj = JsonParser.parseString(json.toString()) as JsonObject
                Log.d(TAG, "Saved JSON contains realmsXsts: ${jsonObj.has("realmsXsts")}")
                Log.d(TAG, "Saved JSON fields: ${jsonObj.keySet()}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save session", e)
            runOnUiThread {
                Toast.makeText(context, "Failed to save session: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun loadSavedSession(
        context: Context,
        httpClient: HttpClient
    ): StepFullBedrockSession.FullBedrockSession? {
        return try {
            val file = File(context.filesDir, SESSION_FILE)
            if (!file.exists()) {
                Log.d(TAG, "Session file does not exist: ${file.absolutePath}")
                return null
            }

            Log.d(TAG, "Loading session from ${file.absolutePath}")
            val jsonString = FileInputStream(file).use { fis ->
                fis.readBytes().toString(Charsets.UTF_8)
            }

            if (jsonString.isBlank()) {
                Log.e(TAG, "Session file is empty")
                deleteSession(context)
                return null
            }

            Log.d(TAG, "Session JSON loaded, length: ${jsonString.length}")
            val json = JsonParser.parseString(jsonString) as JsonObject
            Log.d(TAG, "Session JSON fields: ${json.keySet()}")
            Log.d(TAG, "Has realmsXsts in JSON: ${json.has("realmsXsts")}")

            
            val session = BEDROCK_REALMS_AUTH_FLOW.fromJson(json)
            Log.d(TAG, "Session deserialized successfully")
            Log.d(TAG, "MCChain: ${session.mcChain != null}")
            Log.d(TAG, "PlayFab: ${session.playFabToken != null}")
            Log.d(TAG, "RealmsXsts: ${session.realmsXsts != null}")

            if (session.realmsXsts == null) {
                Log.e(TAG, "Session missing realmsXsts token, discarding")
                deleteSession(context)
                return null
            }

            
            if (session.isExpiredOrOutdated()) {
                Log.d(TAG, "Session is expired/outdated, attempting to refresh")
                try {
                    val refreshedSession = BEDROCK_REALMS_AUTH_FLOW.refresh(httpClient, session)
                    Log.d(TAG, "Session refreshed successfully")
                    Log.d(TAG, "Refreshed - RealmsXsts: ${refreshedSession.realmsXsts != null}")

                    if (refreshedSession.realmsXsts == null) {
                        Log.e(TAG, "Refreshed session missing realmsXsts token")
                        deleteSession(context)
                        return null
                    }

                    
                    saveSession(context, refreshedSession)
                    refreshedSession
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to refresh session", e)
                    deleteSession(context)
                    return null
                }
            } else {
                Log.d(TAG, "Session is valid, no refresh needed")
                session
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load session", e)
            runOnUiThread {
                Toast.makeText(context, "Failed to load session: ${e.message}", Toast.LENGTH_LONG).show()
            }
            deleteSession(context)
            null
        }
    }

    private fun deleteSession(context: Context) {
        try {
            val file = File(context.filesDir, SESSION_FILE)
            if (file.exists()) {
                if (file.delete()) {
                    Log.d(TAG, "Session file deleted: ${file.absolutePath}")
                } else {
                    Log.e(TAG, "Failed to delete session file: ${file.absolutePath}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting session file", e)
            runOnUiThread {
                Toast.makeText(context, "Failed to delete session: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun fetchRealms(
        httpClient: HttpClient,
        session: StepFullBedrockSession.FullBedrockSession?,
        callback: (List<RealmsWorld>?, String?) -> Unit
    ) {
        Log.d(TAG, "Fetching Realms")

        if (session?.realmsXsts == null) {
            callback(null, "No authentication session available")
            return
        }

        try {
            val realmsService = BedrockRealmsService(httpClient, BEDROCK_CLIENT_VERSION, session.realmsXsts)

            realmsService.isAvailable()
                .thenCompose { isAvailable ->
                    if (isAvailable) {
                        Log.d(TAG, "Realms is available, fetching worlds")
                        realmsService.getWorlds()
                    } else {
                        val errorMsg = "Realms is not supported for client version $BEDROCK_CLIENT_VERSION"
                        Log.e(TAG, errorMsg)
                        throw RuntimeException(errorMsg)
                    }
                }
                .thenAccept { worlds ->
                    Log.d(TAG, "Successfully fetched ${worlds.size} Realms")
                    callback(worlds, null)
                }
                .exceptionally { throwable ->
                    Log.e(TAG, "Error with Realms", throwable)
                    callback(null, RealmErrorHandler.translateFetchError(throwable))
                    null
                }
        } catch (e: Exception) {
            Log.e(TAG, "Exception creating BedrockRealmsService", e)
            callback(null, RealmErrorHandler.translateFetchError(e))
        }
    }

    private fun joinRealm(
        httpClient: HttpClient,
        session: StepFullBedrockSession.FullBedrockSession?,
        world: RealmsWorld,
        callback: (String?, String?) -> Unit
    ) {
        Log.d(TAG, "Joining Realm: ${world.name}")

        if (session?.realmsXsts == null) {
            callback(null, "No authentication session available")
            return
        }

        try {
            val realmsService = BedrockRealmsService(httpClient, BEDROCK_CLIENT_VERSION, session.realmsXsts)

            realmsService.joinWorld(world)
                .thenApply { address -> address.toString() }
                .thenAccept { addressString ->
                    Log.d(TAG, "Successfully joined Realm ${world.name}, address: $addressString")
                    callback(addressString, null)
                }
                .exceptionally { throwable ->
                    Log.e(TAG, "Error joining Realm ${world.name}", throwable)
                    callback(null, RealmErrorHandler.translateJoinError(throwable))
                    null
                }
        } catch (e: Exception) {
            Log.e(TAG, "Exception creating BedrockRealmsService", e)
            callback(null, RealmErrorHandler.translateJoinError(e))
        }
    }
}