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
import org.cloudburstmc.protocol.bedrock.packet.MovePlayerPacket
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket
import org.cloudburstmc.protocol.bedrock.packet.SetEntityMotionPacket
import kotlin.math.atan2
import kotlin.random.Random
import com.project.lumina.client.util.AssetManager

class CritBotElement(iconResId: Int = AssetManager.getAsset("ic_angle")) : Element(
    name = "Criticals",
    category = CheatCategory.Combat,
    iconResId,
    displayNameResId = AssetManager.getString("module_critbot_display_name")
) {
    
    private var maxRange by floatValue("Range", 4.0f, 2.0f..6.0f)
    private var cps by intValue("CPS", 10, 1..15)
    private var jumpHeight by floatValue("Jump Height", 0.3f, 0.1f..0.5f)
    private var randomizeTiming by boolValue("Random Timing", true)

    
    private var lastAttackTime = 0L
    private val attackDelayMs: Long
        get() = (1000L / cps) * if (randomizeTiming) Random.nextInt(80, 120) / 100 else 1

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) return

        val packet = interceptablePacket.packet
        if (packet !is PlayerAuthInputPacket) return

        val currentTime = System.currentTimeMillis()
        val targets = findTargetsInRange()
        if (targets.isEmpty()) return

        val target = targets.first()
        rotateToTarget(target)

        if (currentTime - lastAttackTime >= attackDelayMs) {
            executeCriticalHit(target, currentTime)
        }
    }

    private fun findTargetsInRange(): List<Entity> {
        return session.level.entityMap.values
            .filter { it is Player && it !is LocalPlayer && !isBot(it as Player) }
            .filter { it.distance(session.localPlayer) <= maxRange }
            .sortedBy { it.distance(session.localPlayer) }
            .take(1) 
    }

    private fun isBot(player: Player): Boolean {
        if (player is LocalPlayer) return false
        val playerList = session.level.playerMap[player.uuid] ?: return true
        return playerList.name.toString().isBlank()
    }

    private fun rotateToTarget(target: Entity) {
        val deltaX = target.vec3Position.x - session.localPlayer.vec3Position.x
        val deltaZ = target.vec3Position.z - session.localPlayer.vec3Position.z
        val yaw = (Math.toDegrees(atan2(deltaZ, deltaX).toDouble()).toFloat() - 90.0f + 360.0f) % 360.0f

        
        session.clientBound(MovePlayerPacket().apply {
            runtimeEntityId = session.localPlayer.runtimeEntityId
            position = session.localPlayer.vec3Position 
            rotation = Vector3f.from(session.localPlayer.vec3Rotation.x, yaw, session.localPlayer.vec3Rotation.z)
            mode = MovePlayerPacket.Mode.NORMAL
            onGround = true 
            tick = session.localPlayer.tickExists
        })
    }

    private fun executeCriticalHit(target: Entity, currentTime: Long) {
        
        session.clientBound(SetEntityMotionPacket().apply {
            runtimeEntityId = session.localPlayer.runtimeEntityId
            motion = Vector3f.from(0.0f, jumpHeight, 0.0f)
        })

        
        session.clientBound(MovePlayerPacket().apply {
            runtimeEntityId = session.localPlayer.runtimeEntityId
            position = session.localPlayer.vec3Position 
            rotation = session.localPlayer.vec3Rotation 
            mode = MovePlayerPacket.Mode.NORMAL
            onGround = false 
            tick = session.localPlayer.tickExists
        })

        
        session.localPlayer.attack(target)
        lastAttackTime = currentTime
    }
}