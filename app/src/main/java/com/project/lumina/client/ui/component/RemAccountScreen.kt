package com.project.lumina.client.ui.component

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.DialogProperties
import com.project.lumina.client.R
import com.project.lumina.client.constructors.AccountManager
import com.project.lumina.client.overlay.mods.NotificationType
import com.project.lumina.client.overlay.mods.SimpleOverlayNotification
import com.project.lumina.client.util.*
import com.project.lumina.relay.util.XboxDeviceInfo
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemAccountScreen(showNotification: (String, NotificationType) -> Unit) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {

        }
    ) { paddingValues ->
        AccountContent(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            showNotification = showNotification
        )
    }
}

@Composable
private fun AccountContent(modifier: Modifier = Modifier, showNotification: (String, NotificationType) -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var showAddAccountMenu by remember { mutableStateOf(false) }
    var deviceInfo: XboxDeviceInfo? by remember { mutableStateOf(null) }

    val sharedPreferences = remember {
        context.getSharedPreferences("SettingsPrefs", Context.MODE_PRIVATE)
    }
    val removeAccountLimitEnabled = remember {
        sharedPreferences.getBoolean("removeAccountLimitEnabled", false)
    }

    val isAccountLimitReached = if (removeAccountLimitEnabled) false else AccountManager.accounts.size >= 2

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Manage your gaming accounts",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )


        OutlinedButton(
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
            modifier = Modifier.fillMaxWidth(),
            enabled = !isAccountLimitReached,
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = if (!isAccountLimitReached)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            ),
            border = BorderStroke(
                width = 1.dp,
                color = if (isAccountLimitReached)
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                else
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = "Add Account"
                )
                Text(
                    text = "Add New Account",
                    style = MaterialTheme.typography.labelLarge
                )
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
                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Maximum of 2 accounts reached",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = showAddAccountMenu && !isAccountLimitReached,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Select Platform",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )

                        IconButton(
                            onClick = { showAddAccountMenu = false },
                            modifier = Modifier
                                .size(32.dp)
                                .border(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                    shape = CircleShape
                                )
                        ) {
                            Icon(
                                Icons.Rounded.Close,
                                contentDescription = "Close Menu",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Divider()

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 240.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(XboxDeviceInfo.devices.values.toList()) { device ->
                            ElevatedCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        deviceInfo = device
                                        showAddAccountMenu = false
                                    },
                                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                                colors = CardDefaults.elevatedCardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 16.dp, horizontal = 16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {

                                    Text(
                                        text = device.deviceType,
                                        style = MaterialTheme.typography.bodyLarge,
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Divider(
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.outlineVariant
                )
                Text(
                    text = "Your Accounts",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Divider(
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.outlineVariant
                )
            }
        }


        if (AccountManager.accounts.isEmpty()) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {



                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.no_account_added),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(R.string.add_a_gaming_account_to_get_started),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        } else {

            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                AccountManager.accounts.forEach { account ->
                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable {
                                if (AccountManager.currentAccount != account) {
                                    AccountManager.selectAccount(account)
                                } else {
                                    AccountManager.selectAccount(null)
                                }
                            }
                            .animateContentSize(),
                        elevation = CardDefaults.elevatedCardElevation(
                            defaultElevation = if (account == AccountManager.currentAccount) 4.dp else 1.dp
                        ),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = if (account == AccountManager.currentAccount)
                                MaterialTheme.colorScheme.primaryContainer
                            else
                                MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {

                                Surface(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape),
                                    color = if (account == AccountManager.currentAccount)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = account.remark.first().uppercase(),
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = if (account == AccountManager.currentAccount)
                                                MaterialTheme.colorScheme.onPrimary
                                            else
                                                MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Column(
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = account.remark,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = account.platform.deviceType,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = if (account == AccountManager.currentAccount)
                                                MaterialTheme.colorScheme.onPrimaryContainer
                                            else
                                                MaterialTheme.colorScheme.onSurfaceVariant
                                        )

                                        if (account == AccountManager.currentAccount) {
                                            Surface(
                                                shape = RoundedCornerShape(16.dp),
                                                color = MaterialTheme.colorScheme.primary
                                            ) {
                                                Text(
                                                    stringResource(R.string.has_been_selected),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onPrimary,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                )
                                            }
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
                                modifier = Modifier
                                    .size(40.dp)
                                    .border(
                                        width = 1.dp,
                                        color = if (account == AccountManager.currentAccount)
                                            MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f)
                                        else
                                            MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                        shape = CircleShape
                                    )
                            ) {
                                Icon(
                                    Icons.Outlined.DeleteOutline,
                                    contentDescription = "Delete",
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

        Spacer(modifier = Modifier.height(24.dp))
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
            usePlatformDefaultWidth = false,
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
                    navigationIcon = {
                        IconButton(onClick = { callback(false) }) {
                            Icon(
                                imageVector = Icons.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        ) { paddingValues ->
            Column(
                Modifier
                    .padding(paddingValues)
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
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}