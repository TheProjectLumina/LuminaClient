package com.project.lumina.client.activity

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.amplitude.android.Amplitude
import com.amplitude.android.Configuration
import com.amplitude.android.DefaultTrackingOptions
import com.project.lumina.client.R
import com.project.lumina.client.ui.theme.LuminaClientTheme
import com.project.lumina.client.util.TrackUtil
import com.project.lumina.client.util.UpdateCheck

class CrashHandlerActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val amplitude = Amplitude(
            Configuration(
                apiKey = TrackUtil.TRACK_API,
                context = applicationContext,
                defaultTracking = DefaultTrackingOptions.ALL,
            )
        )
        amplitude.track("Lumina Crashed")
        val updateCheck = UpdateCheck()
        updateCheck.initiateHandshake(this)
        val crashMessage = intent?.getStringExtra("message") ?: return finish()

        val parts = parseCrashMessage(crashMessage)

        setContent {
            LuminaClientTheme {
                val context = LocalContext.current

                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = {
                                Text(
                                    text = stringResource(R.string.crash_happened),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                titleContentColor = MaterialTheme.colorScheme.onErrorContainer
                            ),
                            actions = {
                                IconButton(onClick = {
                                    copyToClipboard(context, crashMessage)
                                    Toast.makeText(context, context.getString(R.string.crash_copied), Toast.LENGTH_SHORT).show()
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = stringResource(R.string.copy_crash_log)
                                    )
                                }
                            }
                        )
                    },
                    floatingActionButton = {
                        var fabVisible by remember { mutableStateOf(false) }

                        LaunchedEffect(Unit) {
                            fabVisible = true
                        }

                        val scale by animateFloatAsState(
                            targetValue = if (fabVisible) 1f else 0f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            ),
                            label = "fabScale"
                        )

                        Box(
                            modifier = Modifier
                                .padding(16.dp)
                                .scale(scale)
                        ) {
                            FloatingActionButton(
                                onClick = { restartApp(context) },
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                shape = RoundedCornerShape(16.dp),
                                elevation = FloatingActionButtonDefaults.elevation(
                                    defaultElevation = 4.dp,
                                    pressedElevation = 8.dp
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = stringResource(R.string.restart_app),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    },
                    floatingActionButtonPosition = FabPosition.End
                ) { paddingValues ->
                    Column(
                        modifier = Modifier
                            .padding(paddingValues)
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.crash_description),
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )


                        parts.errorMessage?.let { errorMsg ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                tonalElevation = 4.dp,
                                color = MaterialTheme.colorScheme.errorContainer,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = errorMsg,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                            }
                        }


                        ExpandableSection(
                            title = stringResource(R.string.device_info),
                            content = parts.deviceInfo
                        )


                        ExpandableSection(
                            title = stringResource(R.string.thread_info),
                            content = parts.threadInfo
                        )


                        ExpandableSection(
                            title = stringResource(R.string.stack_trace),
                            content = parts.stackTrace,
                            initiallyExpanded = false
                        )
                    }
                }

                BackHandler {
                    Toast.makeText(this, getString(R.string.cannot_back), Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    private fun copyToClipboard(context: Context, text: String) {
        val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText("Crash Log", text)
        clipboardManager.setPrimaryClip(clipData)
    }

    private fun restartApp(context: Context) {
        val packageManager = context.packageManager
        val intent = packageManager.getLaunchIntentForPackage(context.packageName)
        val componentName = intent?.component
        val mainIntent = Intent.makeRestartActivityTask(componentName)
        context.startActivity(mainIntent)
        Runtime.getRuntime().exit(0)
    }

    private fun parseCrashMessage(message: String): CrashParts {
        val lines = message.lines()

        var errorMessage: String? = null
        var deviceInfoStart = -1
        var threadInfoStart = -1
        var stackTraceStart = -1

        for (i in lines.indices) {
            when {
                i == 0 -> errorMessage = lines[0]
                lines[i].startsWith("BOARD:") || lines[i].startsWith("BOOTLOADER:") -> {
                    if (deviceInfoStart == -1) deviceInfoStart = i
                }
                lines[i].startsWith("Thread:") -> {
                    if (threadInfoStart == -1) {
                        threadInfoStart = i
                        if (deviceInfoStart == -1) deviceInfoStart = i
                    }
                }
                lines[i].startsWith("Stack Trace:") -> {
                    stackTraceStart = i
                }
            }
        }

        val deviceInfo = if (deviceInfoStart != -1 && threadInfoStart != -1) {
            lines.subList(deviceInfoStart, threadInfoStart).joinToString("\n")
        } else {
            ""
        }

        val threadInfo = if (threadInfoStart != -1 && stackTraceStart != -1) {
            lines.subList(threadInfoStart, stackTraceStart).joinToString("\n")
        } else {
            ""
        }

        val stackTrace = if (stackTraceStart != -1) {
            lines.subList(stackTraceStart, lines.size).joinToString("\n")
        } else {
            message
        }

        return CrashParts(errorMessage, deviceInfo, threadInfo, stackTrace)
    }

    data class CrashParts(
        val errorMessage: String?,
        val deviceInfo: String,
        val threadInfo: String,
        val stackTrace: String
    )
}

@Composable
fun ExpandableSection(
    title: String,
    content: String,
    initiallyExpanded: Boolean = true
) {
    var expanded by remember { mutableStateOf(initiallyExpanded) }
    val rotationState by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        label = "rotation"
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(8.dp),
        tonalElevation = 2.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = if (expanded) "Collapse" else "Expand",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.rotate(rotationState)
                    )
                }
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                SelectionContainer {
                    Text(
                        text = content,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    )
                }
            }
        }
    }
}