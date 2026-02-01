/*
 * © Project Lumina 2026 — Licensed under GNU GPLv3
 * You are free to use, modify, and redistribute this code under the terms
 * of the GNU General Public License v3. See the LICENSE file for details.
 */

package com.project.lumina.client.router.main

import android.app.Activity
import android.content.Intent
import android.webkit.CookieManager
import android.webkit.WebStorage
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.project.lumina.client.activity.DiscordLoginActivity
import com.project.lumina.client.discord.DiscordUserManager
import com.project.lumina.client.discord.RPCCallback
import com.project.lumina.client.discord.RPCService
import com.project.lumina.client.discord.TokenManager
import com.project.lumina.client.discord.model.DiscordUser
import com.project.lumina.client.router.main.components.ProfileSection
import com.project.lumina.client.router.main.components.RpcControlsSection
import com.project.lumina.client.router.main.components.ServerVisibilitySection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun DiscordScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isLoggedIn by remember { mutableStateOf(TokenManager.hasToken(context)) }
    var discordUser by remember { mutableStateOf<DiscordUser?>(null) }

    val isRpcConnected by RPCService.isConnected.collectAsState()
    val isRpcConnecting by RPCService.isConnecting.collectAsState()
    val rightScrollState = rememberScrollState()

    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn) {
            discordUser = DiscordUserManager.getUser(context)
        } else {
            discordUser = null
        }
    }

    val loginLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val token = result.data?.getStringExtra(DiscordLoginActivity.EXTRA_TOKEN)
            if (!token.isNullOrEmpty()) {
                TokenManager.saveToken(context, token)
                isLoggedIn = true
                RPCService.start(context, object : RPCCallback {
                    override fun onConnected(user: DiscordUser?) {
                        scope.launch(Dispatchers.Main) {
                            if (user != null) {
                                DiscordUserManager.saveUser(context, user)
                                discordUser = user
                            }
                            Toast.makeText(context, "Discord connected", Toast.LENGTH_SHORT).show()
                        }
                    }
                    override fun onDisconnected() {}
                    override fun onError(message: String) {
                        scope.launch(Dispatchers.Main) {
                            Toast.makeText(context, "Connection error: $message", Toast.LENGTH_LONG).show()
                        }
                    }
                })
            }
        }
    }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            ProfileSection(user = discordUser)
        }
        VerticalDivider(
            modifier = Modifier
                .fillMaxHeight()
                .padding(vertical = 16.dp),
            color = MaterialTheme.colorScheme.outlineVariant
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .verticalScroll(rightScrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ExtendedFloatingActionButton(
                onClick = {
                    if (isLoggedIn) {
                        if (isRpcConnected || isRpcConnecting) {
                            RPCService.stop(context)
                        }
                        TokenManager.clearToken(context)
                        DiscordUserManager.clearUser(context)
                        CookieManager.getInstance().removeAllCookies(null)
                        WebStorage.getInstance().deleteAllData()
                        discordUser = null
                        isLoggedIn = false
                    } else {
                        val intent = Intent(context, DiscordLoginActivity::class.java)
                        loginLauncher.launch(intent)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                containerColor = if (isLoggedIn) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer,
                contentColor = if (isLoggedIn) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer,
                icon = {
                    Icon(
                        imageVector = if (isLoggedIn) Icons.Default.Logout else Icons.Default.Login,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                },
                text = {
                    Text(
                        text = if (isLoggedIn) "Logout from Discord" else "Login to Discord",
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            )
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                )
            ) {
                RpcControlsSection(
                    isLoggedIn = isLoggedIn,
                    isRpcConnected = isRpcConnected,
                    isRpcConnecting = isRpcConnecting,
                    onRpcToggle = { isEnabled ->
                        if (isRpcConnecting) return@RpcControlsSection
                        if (isEnabled) {
                            RPCService.start(context, object : RPCCallback {
                                override fun onConnected(user: DiscordUser?) {
                                    scope.launch(Dispatchers.Main) {
                                        if (user != null) {
                                            DiscordUserManager.saveUser(context, user)
                                            discordUser = user
                                        }
                                        Toast.makeText(context, "Discord RPC connected", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                override fun onDisconnected() {}
                                override fun onError(message: String) {
                                    scope.launch(Dispatchers.Main) {
                                        Toast.makeText(context, "RPC Error: $message", Toast.LENGTH_LONG).show()
                                    }
                                }
                            })
                        } else {
                            RPCService.stop(context)
                        }
                    }
                )
            }
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                )
            ) {
                ServerVisibilitySection(isLoggedIn = isLoggedIn)
            }
        }
    }
}