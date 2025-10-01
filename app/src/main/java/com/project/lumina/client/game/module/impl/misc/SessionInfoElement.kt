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

package com.project.lumina.client.game.module.impl.misc

import com.project.lumina.client.game.InterceptablePacket
import com.project.lumina.client.constructors.Element
import com.project.lumina.client.constructors.CheatCategory
import com.project.lumina.client.overlay.mods.SessionStatsOverlay
import com.project.lumina.client.util.AssetManager
import org.cloudburstmc.protocol.bedrock.packet.AnimatePacket
import kotlin.math.floor

class SessionInfoElement(iconResId: Int = AssetManager.getAsset("ic_info_hexagon"))  : Element(
    name = "SessionInfo",
    category = CheatCategory.Misc,
    displayNameResId = AssetManager.getString("module_sessioni_display_name"),
    iconResId = iconResId
) {

    
    private var startTime = 0L
    private var attackCount = 0
    private var isAttacking = false

    
    private lateinit var statsOverlay: SessionStatsOverlay

    override fun onEnabled() {
        startTime = System.currentTimeMillis()
        attackCount = 0
        isAttacking = false

        if(isSessionCreated) {
            
            session.launchOnMain {
                statsOverlay = session.showSessionStatsOverlay(getInitialStats())
                updateStatsDisplay()
            }
        }
    }

    override fun onDisabled() {
        
        if (::statsOverlay.isInitialized) {
            statsOverlay.dismiss()
        }
    }

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        val packet = interceptablePacket.packet
        val currentTime = System.currentTimeMillis()

        
        when (packet) {
            is AnimatePacket -> updateAttackStatus(packet)
        }

        
        if (isEnabled && currentTime - startTime >= 1000) {
            updateStatsDisplay()
        }
    }

    private fun updateAttackStatus(packet: AnimatePacket) {
        if (packet.runtimeEntityId == session.localPlayer.runtimeEntityId) {
            when (packet.action) {
                AnimatePacket.Action.SWING_ARM -> {
                    attackCount++
                    isAttacking = true
                    session.launchOnMain {
                        updateStatsDisplay()
                        
                        kotlinx.coroutines.delay(500) 
                        isAttacking = false
                        updateStatsDisplay()
                    }
                }

                else -> {}
            }
        }
    }

    private fun getInitialStats(): List<String> {
        return listOf(
            "Playtime: 0h 0m 0s",
            "Attacks: 0",
            "Status: Not Attacking",
            "Coords: 0.0, 0.0, 0.0"
        )
    }

    private fun updateStatsDisplay() {
        if (!::statsOverlay.isInitialized) return 

        val currentTime = System.currentTimeMillis()
        val playtimeSeconds = ((currentTime - startTime) / 1000).toInt()
        val hours = floor(playtimeSeconds / 3600.0).toInt()
        val minutes = floor((playtimeSeconds % 3600) / 60.0).toInt()
        val seconds = playtimeSeconds % 60

        val coords = session.localPlayer.vec3Position
        val coordsStr = String.format("%.1f, %.1f, %.1f", coords.x, coords.y, coords.z)

        val stats = listOf(
            "Playtime: ${hours}h ${minutes}m ${seconds}s",
            "Attacks: $attackCount",
            "Status: ${if (isAttacking) "Attacking" else "Not Attacking"}",
            "Coords: $coordsStr"
        )

        statsOverlay.updateStats(stats)
    }
}