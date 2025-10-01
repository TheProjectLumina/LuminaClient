package com.project.lumina.client.router.main


import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.*
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.with
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.BorderStroke
import com.project.lumina.client.R
import com.project.lumina.client.constructors.AccountManager
import com.project.lumina.client.util.AuthWebView
import com.project.lumina.client.util.getActivityWindow
import com.project.lumina.client.util.getDialogWindow
import com.project.lumina.client.util.windowFullScreen
import com.project.lumina.relay.util.XboxDeviceInfo
import androidx.compose.foundation.verticalScroll
import com.project.lumina.client.overlay.mods.SimpleOverlayNotification
import com.project.lumina.client.overlay.mods.NotificationType
import kotlinx.coroutines.launch

@Composable
fun AccountScreen(showNotification: (String, NotificationType) -> Unit) {
    Row(
        Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        AccountCard(showNotification)
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun AccountCard(showNotification: (String, NotificationType) -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var showAddAccountMenu by remember { mutableStateOf(false) }
    var selectedAccountForAction by remember { mutableStateOf<String?>(null) }
    var deviceInfo: XboxDeviceInfo? by remember { mutableStateOf(null) }

    val sharedPreferences = remember {
        context.getSharedPreferences("SettingsPrefs", Context.MODE_PRIVATE)
    }
    val removeAccountLimitEnabled = remember {
        sharedPreferences.getBoolean("removeAccountLimitEnabled", false)
    }

    val isAccountLimitReached = if (removeAccountLimitEnabled) false else AccountManager.accounts.size >= 2

    OutlinedCard(
        modifier = Modifier
            .width(340.dp)
            .heightIn(max = 500.dp)
            .wrapContentHeight()
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = 0.7f,
                    stiffness = 300f
                )
            ),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        ),
        elevation = CardDefaults.outlinedCardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    stringResource(R.string.account),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                
                IconButton(
                    onClick = { 
                        if (!isAccountLimitReached) {
                            showAddAccountMenu = !showAddAccountMenu
                        } else {
                            showNotification(
                                "Maximum of 2 accounts allowed",
                                NotificationType.WARNING
                            )
                        }
                    },
                    modifier = Modifier
                        .size(36.dp)
                        .border(
                            width = 1.dp,
                            color = if (isAccountLimitReached) 
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                            else 
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                            shape = CircleShape
                        ),
                    enabled = !isAccountLimitReached
                ) {
                    AnimatedContent(
                        targetState = showAddAccountMenu,
                        transitionSpec = {
                            scaleIn() with scaleOut()
                        },
                        label = "AddButtonIcon"
                    ) { isOpen ->
                        Icon(
                            imageVector = if (isOpen) Icons.Rounded.Close else Icons.Rounded.Add,
                            contentDescription = if (isOpen) "Close Menu" else "Add Account",
                            tint = if (isAccountLimitReached) 
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                            else 
                                MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            
            AnimatedVisibility(
                visible = isAccountLimitReached,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Maximum of 2 accounts reached",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }


            AnimatedVisibility(
                visible = showAddAccountMenu && !isAccountLimitReached,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHighest,
                    tonalElevation = 2.dp
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.add_account),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Divider(modifier = Modifier.padding(vertical = 4.dp))

                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 200.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(XboxDeviceInfo.devices.values.toList()) { device ->
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable {
                                            deviceInfo = device
                                            showAddAccountMenu = false
                                        },
                                    color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.7f)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 10.dp, horizontal = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Text(
                                            stringResource(R.string.login_in, device.deviceType),
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (AccountManager.accounts.isNotEmpty()) {
                Divider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
            }


            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 40.dp, max = 220.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                if (AccountManager.accounts.isEmpty()) {

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.no_account_added_yet),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    AccountManager.accounts.forEach { account ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    if (account == AccountManager.currentAccount) {
                                        AccountManager.selectAccount(null)
                                    } else {
                                        AccountManager.selectAccount(account)
                                    }
                                }
                                .animateContentSize(
                                    animationSpec = spring(
                                        dampingRatio = 0.8f,
                                        stiffness = 400f
                                    )
                                ),
                            color = if (account == AccountManager.currentAccount)
                                MaterialTheme.colorScheme.primaryContainer
                            else
                                MaterialTheme.colorScheme.surfaceContainerLow,
                            tonalElevation = if (account == AccountManager.currentAccount) 4.dp else 0.dp
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = account.remark,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium
                                    )
                                    
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = account.platform.deviceType,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (account == AccountManager.currentAccount)
                                                MaterialTheme.colorScheme.onPrimaryContainer
                                            else
                                                MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        
                                        if (account == AccountManager.currentAccount) {
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                            ) {
                                                Text(
                                                    stringResource(R.string.has_been_selected),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                                
                                IconButton(
                                    onClick = {
                                        AccountManager.accounts.remove(account)
                                        if (account == AccountManager.currentAccount) {
                                            AccountManager.selectAccount(null)
                                        }
                                        AccountManager.save()
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        Icons.Outlined.DeleteOutline,
                                        contentDescription = "Delete",
                                        modifier = Modifier.size(20.dp),
                                        tint = if (account == AccountManager.currentAccount)
                                            MaterialTheme.colorScheme.onPrimaryContainer
                                        else
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    deviceInfo?.let {
        if (removeAccountLimitEnabled || AccountManager.accounts.size < 2) {
            AccountDialog(it) { success ->
                deviceInfo = null
                if (success) {
                    coroutineScope.launch {
                        SimpleOverlayNotification.show(
                            message = context.getString(R.string.fetch_account_successfully),
                            type = NotificationType.SUCCESS,
                            durationMs = 3000
                        )
                    }
                } else {
                    coroutineScope.launch {
                        SimpleOverlayNotification.show(
                            message = context.getString(R.string.fetch_account_failed),
                            type = NotificationType.ERROR,
                            durationMs = 3000
                        )
                    }
                }
            }
        } else {
            deviceInfo = null
            coroutineScope.launch {
                SimpleOverlayNotification.show(
                    message = "Maximum of 2 accounts allowed",
                    type = NotificationType.WARNING,
                    durationMs = 3000
                )
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AccountDialog(
    deviceInfo: XboxDeviceInfo,
    callback: (success: Boolean) -> Unit
) {
    BasicAlertDialog(
        onDismissRequest = {},
        properties = DialogProperties(
            usePlatformDefaultWidth = true,
            decorFitsSystemWindows = false
        )
    ) {
        val activityWindow = getActivityWindow()
        val dialogWindow = getDialogWindow()

        SideEffect {
            windowFullScreen(activityWindow, dialogWindow)
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            stringResource(R.string.add_account),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = (-0.5).sp
                            )
                        )
                    },
                    actions = {
                        IconButton(
                            onClick = { callback(false) },
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .size(48.dp)
                                .border(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                                    shape = CircleShape
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "Close",
                                modifier = Modifier.size(22.dp),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp).copy(alpha = 0.95f),
                        titleContentColor = MaterialTheme.colorScheme.onSurface,
                        navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier
                        .shadow(elevation = 4.dp)
                        .clip(RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp))
                )
            }
        ) {
            Column(
                Modifier
                    .padding(it)
                    .fillMaxSize()
            ) {
                AndroidView(
                    factory = { context ->
                        AuthWebView(context).also { authWebView ->
                            authWebView.deviceInfo = deviceInfo
                            authWebView.callback = callback
                        }.also { authWebView ->
                            authWebView.addAccount()
                        }
                    },
                    modifier = Modifier
                        .fillMaxSize()
                )
            }
        }
    }
}
