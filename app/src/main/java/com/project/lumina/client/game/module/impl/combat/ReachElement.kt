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

package com.project.lumina.client.game.module.impl.combat

import com.project.lumina.client.R
import com.project.lumina.client.game.InterceptablePacket
import com.project.lumina.client.constructors.Element
import com.project.lumina.client.constructors.CheatCategory
import com.project.lumina.client.game.entity.Entity
import com.project.lumina.client.game.entity.LocalPlayer
import com.project.lumina.client.game.entity.Player
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket
import com.project.lumina.client.util.AssetManager

class ReachElement(iconResId: Int = AssetManager.getAsset("ic_ghost_black_24dp")) : Element(
    name = "Reach",
    category = CheatCategory.Combat,
    iconResId,
    displayNameResId = AssetManager.getString("module_reach_display_name")
) {
    
    private var combatReachEnabled by boolValue("Combat", true)
    private var combatReach by floatValue("PvP Reach", 3f, 3f..6f) 



    
    private var lastAttackTime = 0L
    private val attackDelayMs: Long = 100

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) return

        val packet = interceptablePacket.packet
        if (packet !is PlayerAuthInputPacket) return

        val currentTime = System.currentTimeMillis()

        if (combatReachEnabled && currentTime - lastAttackTime >= attackDelayMs) {
            val targets = findTargets()
            if (targets.isNotEmpty()) {
                targets.forEach { target ->
                    session.localPlayer.swing()
                    session.localPlayer.attack(target)
                }
                lastAttackTime = currentTime
            }
        }
   }

    private fun findTargets(): List<Entity> {
        return session.level.entityMap.values
            .filter { entity ->
                entity is Player &&
                        entity !is LocalPlayer &&
                        !isBot(entity as Player) &&
                        entity.distance(session.localPlayer) <= combatReach &&
                        entity.distance(session.localPlayer) > 0f
            }
            .sortedBy { it.distance(session.localPlayer) }
    }

    private fun isBot(player: Player): Boolean {
        if (player is LocalPlayer) return false
        val playerList = session.level.playerMap[player.uuid] ?: return true
        return playerList.name.toString().isBlank()
    }
}