/*
 * © Project Lumina 2026 — Licensed under GNU GPLv3
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
import org.cloudburstmc.math.vector.Vector3f
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import com.project.lumina.client.util.AssetManager

class InfiniteAuraElement(iconResId: Int = AssetManager.getAsset("ic_analyze")) : Element(
    name = "Infiniteaura",
    category = CheatCategory.Combat,
    iconResId,
    displayNameResId = AssetManager.getString("module_infiniteaura_display_name")
) {
    private var cps by intValue("CPS", 10, 1..20)
    private var behindOffset by floatValue("Behind Offset", 2f, 0.5f..10f)
    private var silentLagbacks by boolValue("Silent lagbacks", true)

    private var lastAttackTime = 0L
    private var serverSidePos: Vector3f? = null
    private var lastLagbackTime = 0L

    private val attackDelayMs: Long
        get() = 1000L / cps

    fun onEnable() {
        serverSidePos = session.localPlayer.vec3Position
    }

    fun onDisable() {
        serverSidePos = session.localPlayer.vec3Position
    }

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) return

        val packet = interceptablePacket.packet
        if (packet !is PlayerAuthInputPacket) return

        val currentTime = System.currentTimeMillis()
        val playerPos = session.localPlayer.vec3Position

        
        if (serverSidePos != null && serverSidePos!!.distance(playerPos) > 50f) {
            if (silentLagbacks) {
                serverSidePos = playerPos
                lastLagbackTime = currentTime
            } else {
                isEnabled = false
                return
            }
        }

        if (lastLagbackTime > 0 && currentTime - lastLagbackTime < 100) return

        
        val targets = findTargets()
        if (targets.isEmpty()) {
            serverSidePos = playerPos
            packet.position = playerPos
            return
        }

        val target = targets.first()
        val targetPos = Vector3f.from(
            target.vec3Position.x,
            target.vec3Position.y - 1.62f,
            target.vec3Position.z
        )

        
        val yawDeg = target.vec3Rotation.y
        val normalizedYaw = ((yawDeg % 360f + 360f) % 360f).let { if (it > 180f) it - 360f else it }
        val yawRad = Math.toRadians((normalizedYaw + 90f).toDouble()).toFloat()

        val newPos = Vector3f.from(
            targetPos.x - cos(yawRad) * behindOffset,
            targetPos.y,
            targetPos.z - sin(yawRad) * behindOffset
        )

        serverSidePos = newPos

        if (currentTime - lastAttackTime >= attackDelayMs) {
            session.localPlayer.swing()
            session.localPlayer.attack(target)
            lastAttackTime = currentTime
        }

        
        packet.position = serverSidePos!!
        packet.rotation = calculateRotation(targetPos)
    }

    private fun findTargets(): List<Entity> {
        return session.level.entityMap.values
            .filter { entity ->
                entity is Player &&
                        entity !is LocalPlayer &&
                        !isBot(entity as Player) &&
                        entity.distance(session.localPlayer) > 0f
            }
            .sortedBy { it.distance(session.localPlayer) }
            .take(1)
    }

    private fun isBot(player: Player): Boolean {
        if (player is LocalPlayer) return false
        val playerList = session.level.playerMap[player.uuid] ?: return true
        return playerList.name.toString().isBlank()
    }

    private fun calculateRotation(targetPos: Vector3f): Vector3f {
        val deltaX = targetPos.x - serverSidePos!!.x
        val deltaZ = targetPos.z - serverSidePos!!.z
        val yaw = (Math.toDegrees(atan2(deltaZ, deltaX).toDouble()).toFloat() - 90f + 360f) % 360f
        return Vector3f.from(session.localPlayer.vec3Rotation.x, yaw, session.localPlayer.vec3Rotation.z)
    }
}
