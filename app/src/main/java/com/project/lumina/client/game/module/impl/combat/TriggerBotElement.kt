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
import com.project.lumina.client.overlay.manager.OverlayManager
import com.project.lumina.client.overlay.mods.TopCenterOverlayNotification
import org.cloudburstmc.math.vector.Vector3f
import org.cloudburstmc.protocol.bedrock.packet.MovePlayerPacket
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket
import org.cloudburstmc.protocol.bedrock.packet.SetEntityMotionPacket
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random
import com.project.lumina.client.util.AssetManager

class TriggerBotElement(iconResId: Int = AssetManager.getAsset("ic_bullseye_arrow_black_24dp")) : Element(
    name = "TriggerBot",
    category = CheatCategory.Combat,
    iconResId,
    displayNameResId = AssetManager.getString("module_triggerbot_display_name")
) {
    
    private var maxRange by floatValue("Range", 15.0f, 2.0f..30.0f)
    private var cps by intValue("CPS", 12, 1..20)
    private var attackPackets by intValue("Packets", 2, 1..10)
    private var multiTarget by boolValue("Multi", false)
    private var maxTargets by intValue("Max Targets", 3, 1..5)
    private var randomizeTiming by boolValue("Random Timing", true)

    private var walkMode by boolValue("Walk Mode", true)
    private var strafeMode by boolValue("Strafe", false)
    private var teleportMode by boolValue("Teleport", false)
    private var adaptiveMode by boolValue("Adaptive", true)
    private var movementSpeed by floatValue("Speed", 0.7f, 0.1f..5.0f)
    private var strafeRadius by floatValue("Strafe Radius", 2.0f, 0.5f..5.0f)
    private var tpDistance by floatValue("TP Distance", 2.0f, 1.0f..5.0f)
    private var predictiveMovement by boolValue("Predictive Movement", true)

    
    private var lastAttackTime = 0L
    private var strafeAngle = 0f
    private val attackDelayMs: Long
        get() = (1000L / cps) * if (randomizeTiming) Random.nextInt(80, 120) / 100 else 1


    override fun onEnabled() {
        super.onEnabled()

        try {
            if (isSessionCreated) {
                TopCenterOverlayNotification.addNotification(
                    title = "Auto Fight",
                    subtitle = "ACS Enabled",
                    iconRes = R.drawable.ic_sword_cross_black_24dp,
                    progressDuration = 2000
                )
                OverlayManager.showOverlayWindow(TopCenterOverlayNotification())
            } else {
                println("Session not created, cannot enable TriggerBot overlay.")
            }
        } catch (e: Exception) {
            println("Error enabling TriggerBot overlay: ${e.message}")
        }
    }

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) return

        val packet = interceptablePacket.packet
        if (packet !is PlayerAuthInputPacket) return

        val currentTime = System.currentTimeMillis()
        val targets = findTargetsInRange()
        if (targets.isEmpty()) return

        val primaryTarget = targets.first()
        val chosenMode = chooseMovementMode(primaryTarget)
        when (chosenMode) {
            "WALK" -> walkToTarget(primaryTarget, packet)
            "STRAFE" -> strafeAroundTarget(primaryTarget)
            "TELEPORT" -> teleportToTarget(primaryTarget, currentTime)
        }

        if (currentTime - lastAttackTime >= attackDelayMs) {
            executeCombat(targets, currentTime)
        }
    }

    private fun findTargetsInRange(): List<Entity> {
        val targets = session.level.entityMap.values
            .filter { it is Player && it !is LocalPlayer && !isBot(it as Player) }
            .filter { it.distance(session.localPlayer) <= maxRange }
            .sortedBy { it.distance(session.localPlayer) }
        return if (multiTarget) targets.take(maxTargets) else targets.take(1)
    }

    private fun isBot(player: Player): Boolean {
        if (player is LocalPlayer) return false
        val playerList = session.level.playerMap[player.uuid] ?: return true
        return playerList.name.toString().isBlank()
    }

    private fun chooseMovementMode(target: Entity): String {
        if (adaptiveMode) {
            val distance = target.distance(session.localPlayer)
            return when {
                teleportMode && distance > 8.0f -> "TELEPORT"
                walkMode && distance > 3.0f -> "WALK"
                strafeMode -> "STRAFE"
                else -> "WALK"
            }
        }
        return when {
            teleportMode -> "TELEPORT"
            strafeMode -> "STRAFE"
            walkMode -> "WALK"
            else -> "WALK"
        }
    }

    private fun walkToTarget(target: Entity, packet: PlayerAuthInputPacket) {
        val predictedPos = if (predictiveMovement) predictTargetPosition(target) else target.vec3Position
        val yaw = atan2(
            predictedPos.z - session.localPlayer.vec3Position.z,
            predictedPos.x - session.localPlayer.vec3Position.x
        )
        val motionX = cos(yaw) * movementSpeed * if (randomizeTiming) Random.nextFloat() * 0.2f + 0.9f else 1.0f
        val motionZ = sin(yaw) * movementSpeed * if (randomizeTiming) Random.nextFloat() * 0.2f + 0.9f else 1.0f

        session.clientBound(SetEntityMotionPacket().apply {
            runtimeEntityId = session.localPlayer.runtimeEntityId
            motion = Vector3f.from(motionX, 0.0f, motionZ)
        })
    }

    private fun strafeAroundTarget(target: Entity) {
        val predictedPos = if (predictiveMovement) predictTargetPosition(target) else target.vec3Position
        strafeAngle = (strafeAngle + movementSpeed) % 360.0f
        val angleRadians = Math.toRadians(strafeAngle.toDouble())

        val offsetX = strafeRadius * cos(angleRadians).toFloat()
        val offsetZ = strafeRadius * sin(angleRadians).toFloat()

        val newPosition = Vector3f.from(
            predictedPos.x + offsetX,
            session.localPlayer.vec3Position.y,
            predictedPos.z + offsetZ
        )

        session.clientBound(MovePlayerPacket().apply {
            runtimeEntityId = session.localPlayer.runtimeEntityId
            position = newPosition
            rotation = calculateRotation(predictedPos)
            mode = MovePlayerPacket.Mode.NORMAL
            onGround = true
            tick = session.localPlayer.tickExists
        })
    }

    private fun teleportToTarget(target: Entity, currentTime: Long) {
        val predictedPos = if (predictiveMovement) predictTargetPosition(target) else target.vec3Position
        val dirX = predictedPos.x - session.localPlayer.vec3Position.x
        val dirZ = predictedPos.z - session.localPlayer.vec3Position.z
        val length = sqrt((dirX * dirX + dirZ * dirZ).toDouble()).toFloat()

        val newPosition = if (length > 0.0f) {
            Vector3f.from(
                predictedPos.x - (dirX / length) * tpDistance,
                session.localPlayer.vec3Position.y,
                predictedPos.z - (dirZ / length) * tpDistance
            )
        } else {
            Vector3f.from(
                predictedPos.x,
                session.localPlayer.vec3Position.y,
                predictedPos.z - tpDistance
            )
        }

        session.clientBound(MovePlayerPacket().apply {
            runtimeEntityId = session.localPlayer.runtimeEntityId
            position = newPosition
            rotation = calculateRotation(predictedPos)
            mode = MovePlayerPacket.Mode.NORMAL
            isOnGround = true
            tick = session.localPlayer.tickExists
        })
    }

    private fun predictTargetPosition(target: Entity): Vector3f {
        
        return target.vec3Position
    }

    private fun calculateRotation(targetPos: Vector3f): Vector3f {
        val deltaX = targetPos.x - session.localPlayer.vec3Position.x
        val deltaZ = targetPos.z - session.localPlayer.vec3Position.z
        val yaw = (Math.toDegrees(atan2(deltaZ, deltaX).toDouble()).toFloat() - 90.0f + 360.0f) % 360.0f
        return Vector3f.from(session.localPlayer.vec3Rotation.x, yaw, session.localPlayer.vec3Rotation.z)
    }

    private fun executeCombat(targets: List<Entity>, currentTime: Long) {
        targets.forEach { target ->
            repeat(attackPackets) { session.localPlayer.attack(target) }
        }
        lastAttackTime = currentTime
    }
}