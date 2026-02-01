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
import com.project.lumina.client.game.entity.LocalPlayer
import com.project.lumina.client.game.entity.Entity
import com.project.lumina.client.game.entity.Player
import org.cloudburstmc.math.vector.Vector3f
import org.cloudburstmc.protocol.bedrock.packet.MovePlayerPacket
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket
import org.cloudburstmc.protocol.bedrock.packet.SetEntityMotionPacket
import kotlin.random.Random
import com.project.lumina.client.util.AssetManager

class QuickAttackElement(iconResId: Int = AssetManager.getAsset("ic_flash_black_24dp")) : Element(
    name = "QuickAttack",
    category = CheatCategory.Combat,
    iconResId,
    displayNameResId = AssetManager.getString("module_quickattack_display_name")
) {
    private var maxRange by floatValue("Max Range", 5f, 2f..30f)
    private var attackCps by intValue("CPS", 15, 1..20)
    private var attackPackets by intValue("Packets", 1, 1..5)
    private var criticalHits by boolValue("Critical Hits", false)
    private var multiTarget by boolValue("Multi-Target", false)
    private var maxTargets by intValue("Max Targets", 2, 1..5)

    private var strikeDistance by floatValue("Strike Distance", 1.5f, 0.5f..3f)
    private var returnDelay by intValue("Return Delay", 100, 50..500)
    private var humanizeMovement by boolValue("Humanize Movement", true)

    private var randomizeTiming by boolValue("Randomize Timing", true)

    private var lastAttackTime = 0L
    private var attackStartTime: Long = 0L
    private var isReturning = false
    private val attackDelayMs: Long
        get() = (1000L / attackCps) * if (randomizeTiming) Random.nextInt(80, 120) / 100 else 1

    override fun onEnabled() {
        super.onEnabled()
        if (!isSessionCreated) {
            println("Session not created, cannot enable QuickAttack.")
        }
    }

    override fun onDisabled() {
        resetState()
    }

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled || interceptablePacket.packet !is PlayerAuthInputPacket) return

        val currentTime = System.currentTimeMillis()
        val targets = findTargets()

        if (targets.isEmpty()) {
            resetState()
            return
        }

        if (originalPosition == null && currentTime - lastAttackTime >= attackDelayMs) {
            originalPosition = session.localPlayer.vec3Position
        }

        when {
            originalPosition != null && !isReturning && currentTime - lastAttackTime >= attackDelayMs -> {
                val target = targets.first()
                strikeTarget(target, currentTime)
                attackStartTime = currentTime
            }
            !isReturning && currentTime - attackStartTime >= returnDelay -> {
                returnToOriginalPosition()
                isReturning = true
            }
            isReturning && currentTime - attackStartTime >= returnDelay + 50 -> {
                resetState()
            }
        }
    }

    private var originalPosition: Vector3f? = null

    private fun findTargets(): List<Entity> {
        return session.level.entityMap.values
            .filter { it.runtimeEntityId != session.localPlayer.runtimeEntityId && it.isValidTarget() }
            .filter { it.distance(session.localPlayer) < maxRange }
            .sortedBy { it.distance(session.localPlayer) }
            .take(if (multiTarget) maxTargets else 1)
    }

    private fun Entity.isValidTarget(): Boolean {
        return this is Player && !isBot() && this !is LocalPlayer
    }

    private fun Player.isBot(): Boolean {
        val name = session.level.playerMap[uuid]?.name
        return if (name != null) name.toString().isBlank() else true
    }

    private fun strikeTarget(target: Entity, currentTime: Long) {
        val directionX = target.vec3Position.x - session.localPlayer.vec3Position.x
        val directionZ = target.vec3Position.z - session.localPlayer.vec3Position.z
        val distance = kotlin.math.sqrt((directionX * directionX + directionZ * directionZ).toDouble()).toFloat()
        val normalizedX = directionX / distance
        val normalizedZ = directionZ / distance

        val humanizeOffsetX = if (humanizeMovement) Random.nextFloat() * 0.1f - 0.05f else 0f
        val humanizeOffsetZ = if (humanizeMovement) Random.nextFloat() * 0.1f - 0.05f else 0f

        val strikePos = Vector3f.from(
            target.vec3Position.x - normalizedX * strikeDistance + humanizeOffsetX,
            target.vec3Position.y + if (criticalHits) 0.5f else 0f,
            target.vec3Position.z - normalizedZ * strikeDistance + humanizeOffsetZ
        )

        
        val motionX = (strikePos.x - session.localPlayer.vec3Position.x) * 0.5f
        val motionY = (strikePos.y - session.localPlayer.vec3Position.y) * 0.5f
        val motionZ = (strikePos.z - session.localPlayer.vec3Position.z) * 0.5f
        session.clientBound(SetEntityMotionPacket().apply {
            runtimeEntityId = session.localPlayer.runtimeEntityId
            motion = Vector3f.from(motionX, motionY, motionZ)
        })

        
        session.clientBound(MovePlayerPacket().apply {
            runtimeEntityId = session.localPlayer.runtimeEntityId
            position = strikePos
            rotation = session.localPlayer.vec3Rotation
            mode = MovePlayerPacket.Mode.TELEPORT
            onGround = true
            tick = session.localPlayer.tickExists
        })

        repeat(attackPackets) {
            session.localPlayer.attack(target)
        }
        lastAttackTime = currentTime
    }

    private fun returnToOriginalPosition() {
        originalPosition?.let { pos ->
            val motionX = (pos.x - session.localPlayer.vec3Position.x) * 0.5f
            val motionY = (pos.y - session.localPlayer.vec3Position.y) * 0.5f
            val motionZ = (pos.z - session.localPlayer.vec3Position.z) * 0.5f
            session.clientBound(SetEntityMotionPacket().apply {
                runtimeEntityId = session.localPlayer.runtimeEntityId
                motion = Vector3f.from(motionX, motionY, motionZ)
            })

            session.clientBound(MovePlayerPacket().apply {
                runtimeEntityId = session.localPlayer.runtimeEntityId
                position = pos
                rotation = session.localPlayer.vec3Rotation
                mode = MovePlayerPacket.Mode.TELEPORT
                onGround = true
                tick = session.localPlayer.tickExists
            })
        }
    }

    private fun resetState() {
        originalPosition = null
        isReturning = false
        attackStartTime = 0L
    }
}