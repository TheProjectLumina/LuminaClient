/*
 * © Project Lumina 2025 — Licensed under GNU GPLv3
 * You are free to use, modify, and redistribute this code under the terms
 * of the GNU General Public License v3. See the LICENSE file for details.
 *
 * ─────────────────────────────────────────────────────────────────────────────
 * This is open source — not open credit.
 *
 * If you're here to build, welcome. If you're here to repaint and reupload
 * with your tag slapped on it… you're not fooling anyone.
 *
 * Changing colors and class names doesn't make you a developer.
 * Copy-pasting isn't contribution.
 *
 * You have legal permission to fork. But ask yourself — are you improving,
 * or are you just recycling someone else's work to feed your ego?
 *
 * Open source isn't about low-effort clones or chasing clout.
 * It's about making things better. Sharper. Cleaner. Smarter.
 *
 * So go ahead, fork it — but bring something new to the table,
 * or don’t bother pretending.
 *
 * This message is philosophical. It does not override your legal rights under GPLv3.
 * ─────────────────────────────────────────────────────────────────────────────
 *
 * GPLv3 Summary:
 * - You have the freedom to run, study, share, and modify this software.
 * - If you distribute modified versions, you must also share the source code.
 * - You must keep this license and copyright intact.
 * - You cannot apply further restrictions — the freedom stays with everyone.
 * - This license is irrevocable, and applies to all future redistributions.
 *
 * Full text: https://www.gnu.org/licenses/gpl-3.0.html
 */


package io.lumina.luminaux

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.PlayerView
import io.lumina.luminaux.components.GlassmorphicBottomSheet
import io.lumina.luminaux.components.GlassmorphicButton
import io.lumina.luminaux.components.GlassmorphicCard
import io.lumina.luminaux.components.GlassmorphicCheckbox
import io.lumina.luminaux.components.GlassmorphicDialog
import io.lumina.luminaux.components.GlassmorphicDropdownMenu
import io.lumina.luminaux.components.GlassmorphicIconButton
import io.lumina.luminaux.components.GlassmorphicPopupNotification
import io.lumina.luminaux.components.GlassmorphicRadioButton
import io.lumina.luminaux.components.GlassmorphicSlider
import io.lumina.luminaux.components.GlassmorphicTextField
import io.lumina.luminaux.components.GlassmorphicToggle
import io.lumina.luminaux.ui.theme.LuminaUXTheme


@Composable
fun ShowcaseUI() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Video Background (similar to GameUI)
        VideoBackground()

        // Scrollable Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Glassmorphic Components Showcase",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            // Button
            GlassmorphicButton(
                text = "Sample Button",
                onClick = {},
                icon = Icons.Default.Check
            )

            // Toggle
            var toggleState by remember { mutableStateOf(false) }
            GlassmorphicToggle(
                checked = toggleState,
                onCheckedChange = { toggleState = it }
            )

            // Text Field
            var textValue by remember { mutableStateOf(TextFieldValue("")) }
            GlassmorphicTextField(
                value = textValue,
                onValueChange = { textValue = it },
                placeholder = "Enter text",
                modifier = Modifier.fillMaxWidth()
            )

            // Slider
            var sliderValue by remember { mutableStateOf(0.5f) }
            GlassmorphicSlider(
                value = sliderValue,
                onValueChange = { sliderValue = it },
                modifier = Modifier.fillMaxWidth()
            )

            // Checkbox
            var checkboxState by remember { mutableStateOf(false) }
            GlassmorphicCheckbox(
                checked = checkboxState,
                onCheckedChange = { checkboxState = it }
            )

            // Radio Button Group
            var selectedOption by remember { mutableStateOf("Option 1") }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Option 1", "Option 2").forEach { option ->
                    GlassmorphicRadioButton(
                        selected = selectedOption == option,
                        onClick = { selectedOption = option }
                    )
                }
            }

            // Card

            // Icon Button
            GlassmorphicIconButton(
                onClick = {},
                icon = Icons.Default.Settings,
                contentDescription = "Settings"
            )

            // Dropdown Menu
            var dropdownExpanded by remember { mutableStateOf(false) }
            GlassmorphicButton(
                text = "Open Dropdown",
                onClick = { dropdownExpanded = true }
            )
            GlassmorphicDropdownMenu(
                expanded = dropdownExpanded,
                onDismissRequest = { dropdownExpanded = false },
                items = listOf("Item 1", "Item 2", "Item 3"),
                onItemClick = {}
            )

            // Dialog
            var showDialog by remember { mutableStateOf(false) }
            GlassmorphicButton(
                text = "Show Dialog",
                onClick = { showDialog = true }
            )
            if (showDialog) {
                GlassmorphicDialog(
                    onDismissRequest = { showDialog = false },
                    title = "Sample Dialog",
                    content = {
                        Text(
                            text = "This is a customizable dialog.",
                            color = Color.White,
                            fontSize = 16.sp
                        )
                    },
                    buttons = {
                        GlassmorphicButton(
                            text = "Cancel",
                            onClick = { showDialog = false }
                        )
                        GlassmorphicButton(
                            text = "OK",
                            onClick = { showDialog = false },
                            icon = Icons.Default.Check
                        )
                    }
                )
            }



            // Popup Notification
            var showPopup by remember { mutableStateOf(false) }
            GlassmorphicButton(
                text = "Show Popup",
                onClick = { showPopup = true }
            )
            if (showPopup) {
                GlassmorphicPopupNotification(
                    message = "Notification: Action completed!",
                    onDismiss = { showPopup = false },
                    position = Alignment.TopCenter
                )
            }

            // Bottom Sheet
            var showBottomSheet by remember { mutableStateOf(false) }
            GlassmorphicButton(
                text = "Show Bottom Sheet",
                onClick = { showBottomSheet = true }
            )
            if (showBottomSheet) {
                GlassmorphicBottomSheet(
                    onDismissRequest = { showBottomSheet = false }
                ) {
                    Text(
                        text = "This is a bottom sheet content area.",
                        color = Color.White,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(16.dp)
                    )
                    GlassmorphicButton(
                        text = "Close",
                        onClick = { showBottomSheet = false }
                    )
                }
            }
        }
    }
}

@Composable
fun VideoBackground() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            val mediaItem = MediaItem.fromUri("https://erotic-antics.club/\uD83D\uDCF8/wh34dpxq.mp4")
            setMediaItem(mediaItem)
            prepare()
            playWhenReady = true
            repeatMode = ExoPlayer.REPEAT_MODE_ALL
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> exoPlayer.pause()
                Lifecycle.Event.ON_RESUME -> exoPlayer.play()
                Lifecycle.Event.ON_DESTROY -> exoPlayer.release()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            exoPlayer.release()
        }
    }

    val animatedAlpha by animateFloatAsState(
        targetValue = 0.5f,
        animationSpec = tween(700, easing = FastOutSlowInEasing),
        label = "overlayFade"
    )

    AndroidView(
        factory = {
            PlayerView(context).apply {
                player = exoPlayer
                useController = false
                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                setShowBuffering(PlayerView.SHOW_BUFFERING_WHEN_PLAYING)
            }
        },
        modifier = Modifier.fillMaxSize()
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = animatedAlpha))
    )
}