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


package io.lumina.luminaux.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.runtime.Stable
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.height
import androidx.compose.ui.graphics.toArgb

@Composable
fun FloatingBottomNavBar(
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit
) {
    val icons = listOf(
        Icons.Default.Create,
        Icons.Default.Home,
        Icons.Default.Check,
    )

    val bgColor = Color.Transparent
    val accent = Color(0xFFC9C0BB)
    val accent2 = Color(0xFFFFFFFF)
    
    // Remember previous selection for animation direction
    var previousIndex by remember { mutableStateOf(selectedIndex) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 20.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(32.dp))
                .height(72.dp)
                .padding(horizontal = 12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(
                    16.dp,
                    Alignment.CenterHorizontally
                ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                icons.forEachIndexed { index, icon ->
                    val isSelected = index == selectedIndex
                    val wasRecentlySelected = index == previousIndex
                    val shape = RoundedCornerShape(12.dp)

                    // Animate icon size with improved spring animation
                    val iconSize by animateDpAsState(
                        targetValue = if (isSelected) 24.dp else 15.dp,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        ),
                        label = "iconSize"
                    )

                    // Animate container size with improved spring animation
                    val containerSize by animateDpAsState(
                        targetValue = if (isSelected) 44.dp else 28.dp,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        ),
                        label = "containerSize"
                    )
                    
                    // Add color animation
                    val iconColor by animateColorAsState(
                        targetValue = if (isSelected) accent2 else accent,
                        animationSpec = tween(durationMillis = 300),
                        label = "iconColor"
                    )
                    
                    // Add border width animation
                    val borderWidth by animateDpAsState(
                        targetValue = if (isSelected) 1.dp else 0.dp,
                        animationSpec = tween(durationMillis = 200),
                        label = "borderWidth"
                    )
                    
                    // Add elevation effect when selected
                    val elevation by animateFloatAsState(
                        targetValue = if (isSelected) 4f else 0f,
                        animationSpec = tween(durationMillis = 300),
                        label = "elevation"
                    )
                    
                    // Add rotation effect for transition
                    val rotation by animateFloatAsState(
                        targetValue = if (isSelected) 0f else if (wasRecentlySelected) -10f else 0f,
                        animationSpec = tween(durationMillis = 300),
                        label = "rotation"
                    )
                    
                    // Add bounce effect
                    val scale by animateFloatAsState(
                        targetValue = if (isSelected) 1f else 0.9f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        ),
                        label = "scale"
                    )

                    Box(
                        modifier = Modifier
                            .clip(shape)
                            .background(Color.Transparent)
                            .border(
                                width = borderWidth,
                                color = if (isSelected) accent2 else accent,
                                shape = shape
                            )
                            .size(containerSize)
                            .graphicsLayer {
                                this.shadowElevation = elevation
                                this.rotationZ = rotation
                                this.scaleX = scale
                                this.scaleY = scale
                            }
                            .clickable { 
                                if (!isSelected) {
                                    previousIndex = selectedIndex
                                    onItemSelected(index) 
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = iconColor,
                            modifier = Modifier.size(iconSize)
                        )
                    }
                }
            }
        }
    }
}