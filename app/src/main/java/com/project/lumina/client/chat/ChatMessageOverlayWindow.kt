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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import com.project.lumina.client.R
import com.project.lumina.client.overlay.manager.OverlayManager
import com.project.lumina.client.overlay.manager.OverlayWindow

class ChatMessageOverlayWindow : OverlayWindow() {
    
    private val _layoutParams by lazy {
        super.layoutParams.apply {
            flags = flags and WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE.inv() and
                    WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM.inv()
            flags = flags or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
            width = WindowManager.LayoutParams.MATCH_PARENT
            height = WindowManager.LayoutParams.MATCH_PARENT
            gravity = Gravity.CENTER
            softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE or
                    WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE
        }
    }

    override val layoutParams: WindowManager.LayoutParams
        get() = _layoutParams

    companion object {
        private val overlayInstance by lazy { ChatMessageOverlayWindow() }
        private var isVisible = false
        private var editingMessage: ChatMessage? = null
        private var onSaveCallback: ((String) -> Unit)? = null
        private var onDismissCallback: (() -> Unit)? = null

        fun show(
            editingMessage: ChatMessage? = null,
            onSave: (String) -> Unit,
            onDismiss: () -> Unit
        ) {
            if (!isVisible) {
                this.editingMessage = editingMessage
                this.onSaveCallback = onSave
                this.onDismissCallback = onDismiss
                isVisible = true
                try {
                    overlayInstance.lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_START)
                    OverlayManager.showOverlayWindow(overlayInstance)
                } catch (e: Exception) {
                    Log.e("ChatMessageOverlay", "Error showing overlay", e)
                    isVisible = false
                    this.editingMessage = null
                    this.onSaveCallback = null
                    this.onDismissCallback = null
                }
            }
        }

        fun dismiss() {
            if (isVisible) {
                isVisible = false
                editingMessage = null
                onSaveCallback = null
                onDismissCallback = null
                try {
                    overlayInstance.lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
                    OverlayManager.dismissOverlayWindow(overlayInstance)
                } catch (e: Exception) {
                    Log.e("ChatMessageOverlay", "Error dismissing overlay", e)
                }
            }
        }
    }

    @Composable
    override fun Content() {
        val configuration = LocalConfiguration.current
        val isLandscape = configuration.screenWidthDp > configuration.screenHeightDp
        val keyboardController = LocalSoftwareKeyboardController.current
        val focusRequester = remember { FocusRequester() }
        
        var messageText by remember(editingMessage) { 
            mutableStateOf(editingMessage?.message ?: "") 
        }
        
        var visible by remember { mutableStateOf(false) }
        
        LaunchedEffect(isVisible) {
            if (isVisible) {
                visible = true
            } else {
                visible = false
                keyboardController?.hide()
            }
        }

        LaunchedEffect(visible) {
            if (visible) {
                kotlinx.coroutines.delay(300)
                try {
                    focusRequester.requestFocus()
                    kotlinx.coroutines.delay(100)
                    keyboardController?.show()
                } catch (e: Exception) {
                    Log.w("ChatMessageOverlay", "Focus request failed", e)
                }
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
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { 
                        keyboardController?.hide()
                        onDismissCallback?.invoke()
                        dismiss()
                    }
                    .padding(if (isLandscape) 32.dp else 16.dp),
                contentAlignment = Alignment.Center
            ) {
                ElevatedCard(
                    modifier = Modifier
                        .widthIn(
                            min = 300.dp,
                            max = if (isLandscape) 500.dp else 400.dp
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { },
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(24.dp)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = if (editingMessage != null)
                                stringResource(R.string.chat_edit_message)
                            else
                                stringResource(R.string.chat_add_message),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        OutlinedTextField(
                            value = messageText,
                            onValueChange = { messageText = it },
                            label = {
                                Text(stringResource(R.string.chat_message_hint))
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 56.dp)
                                .focusRequester(focusRequester),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                cursorColor = MaterialTheme.colorScheme.primary,
                                focusedLabelColor = MaterialTheme.colorScheme.primary,
                                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            shape = RoundedCornerShape(12.dp),
                            maxLines = 3,
                            keyboardOptions = KeyboardOptions(
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    if (messageText.isNotBlank()) {
                                        keyboardController?.hide()
                                        onSaveCallback?.invoke(messageText.trim())
                                        dismiss()
                                    }
                                }
                            ),
                            singleLine = false
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    keyboardController?.hide()
                                    onDismissCallback?.invoke()
                                    dismiss()
                                },
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.onSurface
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(stringResource(R.string.cancel))
                            }

                            Button(
                                onClick = {
                                    if (messageText.isNotBlank()) {
                                        keyboardController?.hide()
                                        onSaveCallback?.invoke(messageText.trim())
                                        dismiss()
                                    }
                                },
                                enabled = messageText.isNotBlank(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary,
                                    disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                                    disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(stringResource(R.string.save))
                            }
                        }
                    }
                }
            }
        }
    }
}
