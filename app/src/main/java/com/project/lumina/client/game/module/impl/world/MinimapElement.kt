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

package com.project.lumina.client.game.module.impl.world

import com.project.lumina.client.R
import com.project.lumina.client.game.InterceptablePacket
import com.project.lumina.client.constructors.Element
import com.project.lumina.client.constructors.CheatCategory
import com.project.lumina.client.game.entity.Entity
import com.project.lumina.client.game.entity.EntityUnknown
import com.project.lumina.client.game.entity.LocalPlayer
import com.project.lumina.client.game.entity.MobList
import com.project.lumina.client.game.entity.Player
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket
import org.cloudburstmc.protocol.bedrock.packet.MoveEntityAbsolutePacket
import kotlin.math.PI

class MinimapElement : Element(
    name = "Minimap",
    category = CheatCategory.World,
    displayNameResId = R.string.module_minimap_display_name
) {


    private val playerOnly by boolValue("Players", false)
    private var mobsOnly by boolValue("Mobs", true)
    private var rangeValue = 1000
    private val sizeOption by intValue("Size", 100, 60..300)

    
    private val zoomOption by floatValue("Zoom", 1.0f, 0.5f..2.0f)

    
    private val dotSizeOption by intValue("DotSize", 5, 1..10)

    override fun onEnabled() {
        super.onEnabled()

        try {
            if (isSessionCreated) {
                session.enableMinimap(true)
                updateMiniMapSettings()
            } else {
                println("Session not created, cannot enable Minimap.")
            }
        } catch (e: Exception) {
            println("Error enabling Minimap: ${e.message}")
        }
    }

    override fun onDisabled() {
        super.onDisabled()

        if (isSessionCreated) {
            session.enableMinimap(false)
        }
    }

    override fun onDisconnect(reason: String) {
        if (isSessionCreated) {
            session.clearEntityPositions()
        }
    }

    private fun updateMiniMapSettings() {
        session.updateMinimapSize(sizeOption.toFloat())
        session.updateMinimapZoom(zoomOption)
        session.updateDotSize(dotSizeOption)
    }

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled || !isSessionCreated) return

        val packet = interceptablePacket.packet


        if(packet is PlayerAuthInputPacket) {
            val closestEntities = searchForClosestEntities()
            if (closestEntities.isEmpty()) return


            closestEntities.forEach { entity ->

                val entityId = entity.runtimeEntityId
                val position = entity.vec3Position



                session.updateEntityPosition(entityId, position.x, position.z)
                updateMiniMapSettings()


            }

        }
        
        if (packet is PlayerAuthInputPacket) {
            val position = packet.position
            session.updatePlayerPosition(position.x, position.z)

            
            val yawRadians = (packet.rotation.y * PI / 180).toFloat()
            session.updatePlayerRotation(yawRadians)
        }

        

    }

    private fun Entity.isTarget(): Boolean {
        return when (this) {
            is LocalPlayer -> false
            is Player -> {
                if (playerOnly || (playerOnly && mobsOnly)) {
                    !this.isBot()
                } else {
                    false
                }
            }

            is EntityUnknown -> {
                if (mobsOnly || (playerOnly && mobsOnly)) {
                    isMob()
                } else {
                    false
                }
            }

            else -> false
        }
    }


    private fun EntityUnknown.isMob(): Boolean {
        return this.identifier in MobList.mobTypes
    }

    private fun Player.isBot(): Boolean {
        if (this is LocalPlayer) return false
        val playerList = session.level.playerMap[this.uuid] ?: return true
        return playerList.name.toString().isBlank()
    }

    private fun searchForClosestEntities(): List<Entity> {
        return session.level.entityMap.values
            .filter { entity -> entity.distance(session.localPlayer) < rangeValue && entity.isTarget() }
    }
}

