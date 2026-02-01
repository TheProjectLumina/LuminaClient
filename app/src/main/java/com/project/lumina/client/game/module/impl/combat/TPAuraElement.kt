package com.project.lumina.client.game.module.impl.combat

import com.project.lumina.client.R
import com.project.lumina.client.game.InterceptablePacket
import com.project.lumina.client.constructors.Element
import com.project.lumina.client.constructors.CheatCategory
import com.project.lumina.client.game.entity.Entity
import com.project.lumina.client.game.entity.EntityUnknown
import com.project.lumina.client.game.entity.LocalPlayer
import com.project.lumina.client.game.entity.Player
import org.cloudburstmc.math.vector.Vector3f
import org.cloudburstmc.protocol.bedrock.packet.MovePlayerPacket
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.atan2
import com.project.lumina.client.util.AssetManager

class TPAuraElement(iconResId: Int = AssetManager.getAsset("ic_dice_3_outline_black_24dp")) : Element(
    name = "TpAura",
    category = CheatCategory.Combat,
    iconResId,
    displayNameResId = AssetManager.getString("module_tpaura_display_name")
) {
    
    private var teleportBehind by boolValue("Teleport Behind", false)
    private var tpDelay by intValue("TP Delay", 500, 100..2000)
    private var targetDistance by floatValue("Distance", 2.0f, 1f..5f)
    private var attackRange by floatValue("Range", 3.7f, 2f..20f)
    private var preserveY by boolValue("Preserve Y", true)
    private var packets by intValue("Packets", 1, 1..10)  

    
    private var lastTeleportTime = 0L

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) return

        val packet = interceptablePacket.packet
        if (packet is PlayerAuthInputPacket) {
            val currentTime = System.currentTimeMillis()
            val closestEntities = findTargetsInRange()

            if (closestEntities.isNotEmpty() && currentTime - lastTeleportTime >= tpDelay) {
                val target = closestEntities.first()

                
                teleportToTarget(target)

                
                repeat(packets) {
                    session.localPlayer.attack(target)
                }

                lastTeleportTime = currentTime
            }
        }
    }

    private fun teleportToTarget(target: Entity) {
        val newPosition = calculatePosition(target)

        val movePlayerPacket = MovePlayerPacket().apply {
            runtimeEntityId = session.localPlayer.runtimeEntityId
            position = newPosition
            rotation = Vector3f.from(
                session.localPlayer.vec3Rotation.x,
                calculateYawToTarget(target),
                session.localPlayer.vec3Rotation.z
            )
            mode = MovePlayerPacket.Mode.NORMAL
            onGround = true  
            ridingRuntimeEntityId = 0
            tick = session.localPlayer.tickExists
        }

        session.clientBound(movePlayerPacket)
    }

    private fun calculatePosition(target: Entity): Vector3f {
        return if (teleportBehind) {
            getBehindPosition(target)
        } else {
            getFrontPosition(target)
        }
    }

    private fun getBehindPosition(target: Entity): Vector3f {
        val targetPos = target.vec3Position
        val yaw = Math.toRadians(target.vec3Rotation.y.toDouble())
        val dirX = sin(yaw.toFloat())
        val dirZ = -cos(yaw.toFloat())

        val yPos = if (preserveY) {
            session.localPlayer.vec3Position.y
        } else {
            targetPos.y
        }

        return Vector3f.from(
            targetPos.x + dirX * targetDistance,
            yPos,
            targetPos.z + dirZ * targetDistance
        )
    }

    private fun getFrontPosition(target: Entity): Vector3f {
        val targetPos = target.vec3Position
        val playerPos = session.localPlayer.vec3Position

        val dirX = targetPos.x - playerPos.x
        val dirZ = targetPos.z - playerPos.z
        val length = sqrt((dirX * dirX + dirZ * dirZ).toDouble()).toFloat()

        val yPos = if (preserveY) {
            session.localPlayer.vec3Position.y
        } else {
            targetPos.y
        }

        return if (length > 0f) {
            Vector3f.from(
                targetPos.x - (dirX / length) * targetDistance,
                yPos,
                targetPos.z - (dirZ / length) * targetDistance
            )
        } else {
            Vector3f.from(
                targetPos.x,
                yPos,
                targetPos.z - targetDistance
            )
        }
    }

    private fun calculateYawToTarget(target: Entity): Float {
        val targetPos = target.vec3Position
        val playerPos = session.localPlayer.vec3Position

        val deltaX = targetPos.x - playerPos.x
        val deltaZ = targetPos.z - playerPos.z

        val yaw = Math.toDegrees(atan2(deltaZ, deltaX).toDouble()).toFloat() - 90f
        return (yaw + 360) % 360
    }

    private fun findTargetsInRange(): List<Entity> {
        return session.level.entityMap.values
            .filter { entity -> entity.distance(session.localPlayer) < attackRange && entity.isTarget() }
            .sortedBy { it.distance(session.localPlayer) }
    }

    private fun Entity.isTarget(): Boolean {
        return when (this) {
            is LocalPlayer -> false
            is Player -> !this.isBot()
            is EntityUnknown -> true
            else -> false
        }
    }

    private fun Player.isBot(): Boolean {
        if (this is LocalPlayer) return false
        val playerList = session.level.playerMap[this.uuid] ?: return true
        return playerList.name.toString().isBlank()
    }
}