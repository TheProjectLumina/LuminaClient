/**
 * © Project Lumina 2025 — Licensed under GNU GPLv3
 * You are free to use, modify, and redistribute this code under the terms
 * of the GNU General Public License v3. See the LICENSE file for details.
 */

package com.project.lumina.client.router.main

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.project.lumina.client.ui.component.PackItem
import com.project.lumina.client.util.API
import com.project.lumina.client.util.MCPack
import com.project.lumina.client.util.MCPackUtils


object PackSelectionManager {
    var selectedPack: MCPack? by mutableStateOf(null)
}

val nekourl = API.FILES_SERVICE_PACK_INDEX_JSON


@Composable
fun PacksScreen() {
    val context = LocalContext.current
    var packs by remember { mutableStateOf<List<MCPack>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        try {
            packs = MCPackUtils.fetchPacksFromJson(nekourl)
        } catch (e: Exception) {

        } finally {
            isLoading = false
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp)
    ) {
        item {
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (packs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No resource packs available",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        items(packs.size) { index ->
            val pack = packs[index]
            PackItem(
                pack = pack,
                isSelected = PackSelectionManager.selectedPack == pack,
                onClick = {
                    PackSelectionManager.selectedPack = pack
                }
            )
        }
    }
}