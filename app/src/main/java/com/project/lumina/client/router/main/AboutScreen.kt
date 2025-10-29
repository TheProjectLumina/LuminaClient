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
 * or don't bother pretending.
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

package com.project.lumina.client.router.main

import android.content.Intent
import android.net.Uri
import android.content.Context
import android.widget.Toast
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.VerticalDivider
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.project.lumina.client.R

@Composable
private fun getAppVersion(context: android.content.Context): String {
    return try {
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        packageInfo.versionName ?: "Unknown"
    } catch (e: Exception) {
        "Unknown"
    }
}

private fun getDeviceModel(): String {
    return "${Build.MANUFACTURER} ${Build.MODEL}"
}

private fun getAndroidVersion(): String {
    return "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})"
}

private fun getDeviceInfo(): List<Pair<String, String>> {
    return listOf(
        "Device" to getDeviceModel(),
        "Android Version" to getAndroidVersion(),
        "Architecture" to (Build.SUPPORTED_ABIS.firstOrNull() ?: "Unknown"),
        "Board" to Build.BOARD,
        "Brand" to Build.BRAND,
        "Hardware" to Build.HARDWARE,
        "Product" to Build.PRODUCT,
        "Build ID" to Build.ID,
        "Build Type" to Build.TYPE,
        "Build Tags" to Build.TAGS
    )
}

private fun getLuminaTechnicalInfo(context: Context): List<Pair<String, String>> {
    val packageInfo = try {
        context.packageManager.getPackageInfo(context.packageName, 0)
    } catch (e: Exception) {
        null
    }

    return listOf(
        "Version" to (packageInfo?.versionName ?: "Unknown"),
        "Version Code" to (packageInfo?.longVersionCode?.toString() ?: "Unknown"),
        "Package Name" to context.packageName,
        "Target SDK" to (packageInfo?.applicationInfo?.targetSdkVersion?.toString() ?: "Unknown"),
        "Min SDK" to "21",
        "Build Type" to "Release",
        "Architecture" to "ARM64/ARM32/X86/X86_64",
        "Engine" to "Bedrock Protocol"
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun InfoItem(
    label: String,
    value: String,
    isClickable: Boolean = false,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isClickable && onClick != null) {
                    Modifier.combinedClickable(
                        onClick = onClick,
                        onLongClick = onLongClick
                    )
                } else {
                    Modifier
                }
            )
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(0.4f)
        )

        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = if (isClickable) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            fontWeight = if (isClickable) FontWeight.Medium else FontWeight.Normal,
            modifier = Modifier.weight(0.6f),
            textAlign = TextAlign.End
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AboutScreen() {
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("SettingsPrefs", Context.MODE_PRIVATE)
    var devToolsEnabled by remember { mutableStateOf(sharedPreferences.getBoolean("devToolsEnabled", false)) }
    var tapCount by rememberSaveable { mutableStateOf(0) }

    ElevatedCard(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            // Left Column - About Lumina
            Column(
                modifier = Modifier
                    .weight(0.6f)
                    .fillMaxHeight()
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Text(
                    stringResource(R.string.about_lumina),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        stringResource(R.string.lumina_introduction),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        stringResource(R.string.lumina_expectation),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        stringResource(R.string.lumina_compatibility),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.weight(1f))


                Text(
                    stringResource(R.string.lumina_copyright),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    stringResource(R.string.lumina_team),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        stringResource(R.string.connect_with_us),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.padding(top = 12.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SocialMediaIcon(
                            icon = painterResource(id = R.drawable.ic_github),
                            label = "GitHub",
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/TheProjectLumina/LuminaClient"))
                                context.startActivity(intent)
                            }
                        )

                        SocialMediaIcon(
                            icon = painterResource(id = R.drawable.ic_discord),
                            label = "Discord",
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://discord.com/invite/6kz3dcndrN"))
                                context.startActivity(intent)
                            }
                        )

                        SocialMediaIcon(
                            icon = Icons.Filled.Public,
                            label = "Website",
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://projectlumina.netlify.app"))
                                context.startActivity(intent)
                            }
                        )

                        SocialMediaIcon(
                            icon = painterResource(id = R.drawable.ic_youtube),
                            label = "YouTube",
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://youtube.com/@prlumina"))
                                context.startActivity(intent)
                            }
                        )
                    }
                }
            }

            VerticalDivider(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(horizontal = 24.dp),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )

            Column(
                modifier = Modifier
                    .weight(0.4f)
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Text(
                    "Device Information",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    getDeviceInfo().forEach { (key, value) ->
                        InfoItem(label = key, value = value)
                    }
                }

                Text(
                    "Technical Information",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp)
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    getLuminaTechnicalInfo(context).forEach { (key, value) ->
                        InfoItem(
                            label = key,
                            value = value,
                            isClickable = key == "Version",
                            onClick = if (key == "Version") {
                                {
                                    if (devToolsEnabled) {
                                        Toast.makeText(context, "Developer tools already enabled", Toast.LENGTH_SHORT).show()
                                    } else {
                                        tapCount += 1
                                        Toast.makeText(context, "Tap ${7 - tapCount} more times to enable dev tools", Toast.LENGTH_SHORT).show()
                                        if (tapCount >= 7) {
                                            sharedPreferences.edit().putBoolean("devToolsEnabled", true).apply()
                                            devToolsEnabled = true
                                            Toast.makeText(context, "Developer tools enabled", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            } else null,
                            onLongClick = if (key == "Version") {
                                {
                                    if (devToolsEnabled) {
                                        with(sharedPreferences.edit()) {
                                            putBoolean("devToolsEnabled", false)
                                            putBoolean("removeAccountLimitEnabled", false)
                                            putBoolean("disableAutoStartEnabled", false)
                                            putBoolean("disableAuthRequiredEnabled", false)
                                            apply()
                                        }
                                        devToolsEnabled = false
                                        tapCount = 0
                                        Toast.makeText(context, "Developer tools disabled", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            } else null
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FeatureItem(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.Check,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun SocialMediaIcon(
    icon: Any,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        when (icon) {
            is androidx.compose.ui.graphics.painter.Painter -> {
                Icon(
                    painter = icon,
                    contentDescription = label,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
            }
            is androidx.compose.ui.graphics.vector.ImageVector -> {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
        
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary
        )
    }
} 