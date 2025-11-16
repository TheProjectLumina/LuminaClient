/**
 * © Project Lumina 2025 — Licensed under GNU GPLv3
 * You are free to use, modify, and redistribute this code under the terms
 * of the GNU General Public License v3. See the LICENSE file for details.
 */

/*
* ██████╗ ███████╗ █████╗ ██╗     ███╗   ███╗███████╗ █████╗  ██████╗████████╗██╗██╗   ██╗██╗████████╗██╗   ██╗
* ██╔══██╗██╔════╝██╔══██╗██║     ████╗ ████║██╔════╝██╔══██╗██╔════╝╚══██╔══╝██║██║   ██║██║╚══██╔══╝╚██╗ ██╔╝
* ██████╔╝█████╗  ███████║██║     ██╔████╔██║███████╗███████║██║        ██║   ██║██║   ██║██║   ██║    ╚████╔╝
* ██╔══██╗██╔══╝  ██╔══██║██║     ██║╚██╔╝██║╚════██║██╔══██║██║        ██║   ██║╚██╗ ██╔╝██║   ██║     ╚██╔╝
* ██║  ██║███████╗██║  ██║███████╗██║ ╚═╝ ██║███████║██║  ██║╚██████╗   ██║   ██║ ╚████╔╝ ██║   ██║      ██║
* ╚═╝  ╚═╝╚══════╝╚═╝  ╚═╝╚══════╝╚═╝     ╚═╝╚══════╝╚═╝  ╚═╝ ╚═════╝   ╚═╝   ╚═╝  ╚═══╝  ╚═╝   ╚═╝      ╚═╝
*
* Implemented by Lisa & Loding ❤️
*/

package com.project.lumina.client.router.main

import android.content.Context
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseInOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Web
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import net.lenni0451.commons.httpclient.HttpClient
import net.raphimc.minecraftauth.MinecraftAuth
import net.raphimc.minecraftauth.service.realms.BedrockRealmsService
import net.raphimc.minecraftauth.service.realms.model.RealmsWorld
import net.raphimc.minecraftauth.step.bedrock.session.StepFullBedrockSession
import net.raphimc.minecraftauth.step.msa.StepMsaDeviceCode
import net.raphimc.minecraftauth.util.MicrosoftConstants
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.JsonSyntaxException
import com.project.lumina.client.util.RealmsAuthWebView
import java.io.File
import java.util.concurrent.CompletableFuture

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RealmsScreen() {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val scrollState = rememberScrollState()
    val httpClient = remember { HttpClient() }

    var bedrockSession by remember { mutableStateOf<StepFullBedrockSession.FullBedrockSession?>(null) }
    var statusMessage by remember { mutableStateOf("Loading session...") }
    var isSessionLoaded by remember { mutableStateOf(false) }
    var isLoginButtonEnabled by remember { mutableStateOf(false) }
    var isLogoutButtonEnabled by remember { mutableStateOf(false) }
    var userCode by remember { mutableStateOf<String?>(null) }
    var verificationUri by remember { mutableStateOf<String?>(null) }
    var showWebViewDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (!isSessionLoaded) {
            loadSession(context, httpClient) { session ->
                bedrockSession = session
                if (session != null) {
                    statusMessage = "Session loaded successfully"
                    isLoginButtonEnabled = false
                    isLogoutButtonEnabled = true
                } else {
                    statusMessage = "No saved session found. Please login."
                    isLoginButtonEnabled = true
                    isLogoutButtonEnabled = false
                }
                isSessionLoaded = true
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        HeaderSection()

        AuthenticationStatusCard(
            statusMessage = statusMessage,
            isAuthenticated = bedrockSession != null
        )

        AnimatedVisibility(
            visible = userCode != null,
            enter = expandVertically(animationSpec = tween(300)) + fadeIn(),
            exit = shrinkVertically(animationSpec = tween(300)) + fadeOut()
        ) {
            userCode?.let { code ->
                DeviceCodeCard(
                    userCode = code,
                    verificationUri = verificationUri,
                    clipboardManager = clipboardManager
                )
            }
        }

        AuthenticationControls(
            isSessionLoaded = isSessionLoaded,
            isLoginButtonEnabled = isLoginButtonEnabled,
            isLogoutButtonEnabled = isLogoutButtonEnabled,
            onWebViewLogin = {
                showWebViewDialog = true
                statusMessage = "Opening authentication..."
            },
            onDeviceCodeLogin = {
                performLogin(httpClient, context) { session, code, uri, error ->
                    if (error != null) {
                        statusMessage = "Login error: $error"
                        isLoginButtonEnabled = true
                    } else if (session != null) {
                        bedrockSession = session
                        statusMessage = "Login successful"
                        isLoginButtonEnabled = false
                        isLogoutButtonEnabled = true
                        userCode = null
                        verificationUri = null
                        saveSession(context, session)
                    } else {
                        userCode = code
                        verificationUri = uri
                        statusMessage = "Please complete authentication in browser"
                    }
                }
            },
            onLogout = {
                bedrockSession = null
                statusMessage = "Logged out"
                isLoginButtonEnabled = true
                isLogoutButtonEnabled = false
                userCode = null
                verificationUri = null
                deleteSession(context)
            }
        )

        RealmsManagementInfoCard()
    }


    if (showWebViewDialog) {
        AuthenticationDialog(
            onDismiss = {
                showWebViewDialog = false
                statusMessage = "Authentication cancelled"
            },
            httpClient = httpClient,
            context = context,
            onResult = { session, error ->
                showWebViewDialog = false
                if (error != null) {
                    statusMessage = "Login error: $error"
                    isLoginButtonEnabled = true
                } else if (session != null) {
                    bedrockSession = session
                    statusMessage = "Login successful!"
                    isLoginButtonEnabled = false
                    isLogoutButtonEnabled = true
                    userCode = null
                    verificationUri = null
                    saveSession(context, session)
                }
            }
        )
    }
}

@Composable
private fun HeaderSection() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = "Minecraft Realms",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Text(
            text = "Connect to your Minecraft Bedrock Realms",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
private fun AuthenticationStatusCard(
    statusMessage: String,
    isAuthenticated: Boolean
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),

        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(RoundedCornerShape(50))
                    .background(
                        if (isAuthenticated)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.error
                    )
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isAuthenticated) "Connected" else "Disconnected",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isAuthenticated)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.error
                )

                Text(
                    text = statusMessage,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun DeviceCodeCard(
    userCode: String,
    verificationUri: String?,
    clipboardManager: androidx.compose.ui.platform.ClipboardManager
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )

                Text(
                    text = "Device Code Authentication",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.outlinedCardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Your Code",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )

                    Text(
                        text = userCode,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = MaterialTheme.typography.headlineSmall.letterSpacing * 1.5
                    )
                }
            }

            verificationUri?.let { uri ->
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.outlinedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Visit this URL",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )

                        Text(
                            text = uri,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Button(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(uri))
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Copy URL")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AuthenticationControls(
    isSessionLoaded: Boolean,
    isLoginButtonEnabled: Boolean,
    isLogoutButtonEnabled: Boolean,
    onWebViewLogin: () -> Unit,
    onDeviceCodeLogin: () -> Unit,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AnimatedVisibility(
            visible = isSessionLoaded && isLoginButtonEnabled,
            enter = fadeIn(animationSpec = tween(1100, easing = EaseInOut)) +
                    expandVertically(animationSpec = tween(1100, easing = EaseInOut)),
            exit = fadeOut(animationSpec = tween(1100, easing = EaseInOut)) +
                   shrinkVertically(animationSpec = tween(1100, easing = EaseInOut))
        ) {
            Button(
                onClick = onWebViewLogin,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Web,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Web Login")
            }
        }

        AnimatedVisibility(
            visible = isSessionLoaded && isLogoutButtonEnabled,
            enter = fadeIn(animationSpec = tween(1100, easing = EaseInOut)) +
                    expandVertically(animationSpec = tween(1100, easing = EaseInOut)),
            exit = fadeOut(animationSpec = tween(1100, easing = EaseInOut)) +
                   shrinkVertically(animationSpec = tween(1100, easing = EaseInOut))
        ) {
            Button(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Logout,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Logout")
            }
        }
    }
}

@Composable
private fun RealmsManagementInfoCard() {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )

                Text(
                    text = "Realms Management",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Text(
                text = "The realms list and join functionality have been moved to the Home screen under the Realms tab for better accessibility.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )


        }
    }
}

@Composable
private fun AuthenticationDialog(
    onDismiss: () -> Unit,
    httpClient: HttpClient,
    context: Context,
    onResult: (StepFullBedrockSession.FullBedrockSession?, String?) -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        ElevatedCard(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Microsoft Authentication",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Login to access your Minecraft Realms",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }

                        FilledTonalIconButton(
                            onClick = onDismiss,
                            colors = IconButtonDefaults.filledTonalIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.onErrorContainer
                            )
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }
                }

                AndroidView(
                    factory = { context ->
                        RealmsAuthWebView(context).apply {
                            callback = { session, error ->
                                onResult(session, error)
                            }
                            startAuthentication(httpClient)
                        }
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                )
            }
        }
    }
}

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

private fun loadSession(
    context: Context,
    httpClient: HttpClient,
    callback: (StepFullBedrockSession.FullBedrockSession?) -> Unit
) {
    CompletableFuture.supplyAsync {
        try {
            val file = File(context.filesDir, SESSION_FILE)
            if (!file.exists()) {
                Log.d(TAG, "Session file does not exist")
                return@supplyAsync null
            }

            val jsonString = file.readText()
            if (jsonString.isBlank()) {
                Log.e(TAG, "Session file is empty")
                deleteSession(context)
                return@supplyAsync null
            }

            val json = JsonParser.parseString(jsonString) as JsonObject
            val session = BEDROCK_REALMS_AUTH_FLOW.fromJson(json)
            Log.d(TAG, "Session loaded successfully")

            if (session.realmsXsts == null) {
                Log.e(TAG, "Session missing realmsXsts token")
                deleteSession(context)
                return@supplyAsync null
            }

            session
        } catch (e: Exception) {
            Log.e(TAG, "Error loading session", e)
            null
        }
    }.thenAccept { session ->
        callback(session)
    }
}

private fun saveSession(
    context: Context,
    session: StepFullBedrockSession.FullBedrockSession
) {
    CompletableFuture.runAsync {
        try {
            val json = BEDROCK_REALMS_AUTH_FLOW.toJson(session)
            val file = File(context.filesDir, SESSION_FILE)
            file.writeText(json.toString())
            Log.d(TAG, "Session saved successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving session", e)
        }
    }
}

private fun deleteSession(context: Context) {
    CompletableFuture.runAsync {
        try {
            val sessionFile = File(context.filesDir, SESSION_FILE)
            if (sessionFile.exists()) {
                sessionFile.delete()
                Log.d(TAG, "Session file deleted")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting session", e)
        }
    }
}

private fun performLogin(
    httpClient: HttpClient,
    context: Context,
    callback: (StepFullBedrockSession.FullBedrockSession?, String?, String?, String?) -> Unit
) {
    CompletableFuture.supplyAsync {
        try {
            Log.d(TAG, "Starting authentication flow")
            val session = BEDROCK_REALMS_AUTH_FLOW.getFromInput(httpClient, StepMsaDeviceCode.MsaDeviceCodeCallback { msaDeviceCode ->
                Log.d(TAG, "Device code callback - User code: ${msaDeviceCode.userCode}")
                callback(null, msaDeviceCode.userCode, msaDeviceCode.verificationUri, null)
            }) as StepFullBedrockSession.FullBedrockSession

            Log.d(TAG, "Authentication flow completed successfully")

            if (session.realmsXsts == null) {
                Log.e(TAG, "Authentication succeeded but realmsXsts token is missing")
                throw IllegalStateException("Authentication succeeded but realmsXsts token is missing")
            }

            saveSession(context, session)
            session
        } catch (e: Exception) {
            Log.e(TAG, "Authentication error", e)
            throw e
        }
    }.thenAccept { session ->
        if (session != null) {
            callback(session, null, null, null)
        }
    }.exceptionally { throwable ->
        Log.e(TAG, "Authentication exception", throwable)
        callback(null, null, null, throwable.message)
        null
    }
}