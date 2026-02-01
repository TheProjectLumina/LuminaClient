package com.project.lumina.client.chat

import android.util.Log
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import com.project.lumina.client.R
import com.project.lumina.client.overlay.manager.OverlayManager
import com.project.lumina.client.overlay.manager.OverlayWindow
import kotlinx.coroutines.delay

class ChatSettingsOverlayWindow : OverlayWindow() {
    
    private val _layoutParams by lazy {
        super.layoutParams.apply {
            flags = flags and WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE.inv() and
                    WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM.inv()
            flags = flags or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
            gravity = Gravity.CENTER
            width = WindowManager.LayoutParams.WRAP_CONTENT
            height = WindowManager.LayoutParams.WRAP_CONTENT
        }
    }
    
    override val layoutParams: WindowManager.LayoutParams
        get() = _layoutParams
    
    companion object {
        private val overlayInstance = ChatSettingsOverlayWindow()
        private var isVisible = false
        private var messages: List<ChatMessage> = emptyList()
        private var onUpdateCallback: ((List<ChatMessage>) -> Unit)? = null
        private var onDismissCallback: (() -> Unit)? = null
        
        fun show(
            messagesList: List<ChatMessage>,
            onUpdate: (List<ChatMessage>) -> Unit,
            onDismiss: () -> Unit
        ) {
            if (!isVisible) {
                messages = messagesList
                onUpdateCallback = onUpdate
                onDismissCallback = onDismiss
                isVisible = true
                try {
                    overlayInstance.lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_START)
                    OverlayManager.showOverlayWindow(overlayInstance)
                } catch (e: Exception) {
                    Log.e("ChatSettingsOverlay", "Error showing overlay", e)
                    isVisible = false
                    messages = emptyList()
                    onUpdateCallback = null
                    onDismissCallback = null
                }
            }
        }
        
        fun dismiss() {
            if (isVisible) {
                isVisible = false
                messages = emptyList()
                onUpdateCallback = null
                onDismissCallback = null
                try {
                    overlayInstance.lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
                    OverlayManager.dismissOverlayWindow(overlayInstance)
                } catch (e: Exception) {
                    Log.e("ChatSettingsOverlay", "Error dismissing overlay", e)
                }
            }
        }
    }
    
    @Composable
    override fun Content() {
        val configuration = LocalConfiguration.current
        val isLandscape = configuration.screenWidthDp > configuration.screenHeightDp
        
        var visible by remember { mutableStateOf(false) }
        var currentSpamMessage by remember { mutableStateOf(ChatSpamManager.getCurrentMessage()) }
        var isSpamming by remember { mutableStateOf(ChatSpamManager.isSpamming()) }
        
        LaunchedEffect(isVisible) {
            if (isVisible) {
                visible = true
            } else {
                visible = false
            }
        }
        
        LaunchedEffect(visible) {
            while (visible) {
                currentSpamMessage = ChatSpamManager.getCurrentMessage()
                isSpamming = ChatSpamManager.isSpamming()
                delay(300)
            }
        }
        
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(animationSpec = tween(300)) + scaleIn(
                initialScale = 0.8f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ),
            exit = fadeOut(animationSpec = tween(200)) + scaleOut(
                targetScale = 0.8f,
                animationSpec = tween(200)
            )
        ) {
            ElevatedCard(
                modifier = Modifier
                    .width(if (isLandscape) 400.dp else 320.dp)
                    .height(if (isLandscape) 300.dp else 400.dp),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Chat Settings",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        IconButton(
                            onClick = {
                                onDismissCallback?.invoke()
                                dismiss()
                            }
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Close",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    
                    if (isSpamming && currentSpamMessage != null) {
                        SpamStatusCard(currentSpamMessage!!) {
                            ChatSpamManager.stopSpam()
                        }
                    }
                    
                    Text(
                        text = "Messages",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(messages) { message ->
                            MessageSettingsItem(
                                message = message,
                                allMessages = messages,
                                onUpdate = { updatedMessage ->
                                    val updatedList = messages.map {
                                        if (it.id == updatedMessage.id) updatedMessage else it
                                    }
                                    messages = updatedList
                                    onUpdateCallback?.invoke(updatedList)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SpamStatusCard(
    message: ChatMessage,
    onStop: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Auto-Sending",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = message.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    maxLines = 1
                )
                Text(
                    text = "Interval: ${message.spamInterval}ms",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }

            IconButton(onClick = onStop) {
                Icon(
                    Icons.Default.Stop,
                    contentDescription = "Stop",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun MessageSettingsItem(
    message: ChatMessage,
    allMessages: List<ChatMessage>,
    onUpdate: (ChatMessage) -> Unit
) {
    var spamInterval by remember { mutableStateOf(message.spamInterval) }
    var currentSpamMessage by remember { mutableStateOf(ChatSpamManager.getCurrentMessage()) }

    LaunchedEffect(Unit) {
        while (true) {
            currentSpamMessage = ChatSpamManager.getCurrentMessage()
            delay(300)
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = message.message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Interval:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.width(50.dp)
                )

                Slider(
                    value = spamInterval.toFloat(),
                    onValueChange = {
                        spamInterval = it.toInt()
                        onUpdate(message.copy(spamInterval = spamInterval))
                    },
                    valueRange = 100f..5000f,
                    steps = 49,
                    modifier = Modifier.weight(1f),
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary,
                        inactiveTrackColor = MaterialTheme.colorScheme.outline
                    )
                )

                Text(
                    text = "${spamInterval}ms",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.width(60.dp)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    onClick = {
                        if (currentSpamMessage?.id == message.id) {
                            ChatSpamManager.stopSpam()
                        } else {
                            ChatSpamManager.startSpam(message)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (currentSpamMessage?.id == message.id)
                            MaterialTheme.colorScheme.error
                        else
                            MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = if (currentSpamMessage?.id == message.id)
                            Icons.Default.Stop
                        else
                            Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    Text(if (currentSpamMessage?.id == message.id) "Stop" else "Start")
                }
            }
        }
    }
}