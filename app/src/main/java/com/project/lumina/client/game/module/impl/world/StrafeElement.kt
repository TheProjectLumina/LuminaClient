package com.project.lumina.client.game.module.impl.world

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
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import com.project.lumina.client.util.AssetManager

class StrafeElement(iconResId: Int = AssetManager.getAsset("ic_run_fast_black_24dp")) : Element(
    name = "Strafe",
    category = CheatCategory.World,
    iconResId,
    displayNameResId = AssetManager.getString("module_strafe_display_name")
) {
    private var targetRange by intValue("Range", 3, 2..20)
    private val movementSpeed by intValue("Speed", 1, 1..20)
    private val circleRadius by intValue("Circle Radius", 1, 1..5)
    private val offsetX by intValue("X Level", 0, -8..8)
    private val offsetY by intValue("Y Level", 0, -8..8)
    private val offsetZ by intValue("Z Level", 0, -8..8)
    private var preserveY by boolValue("Preserve Y", true)
    private var faceTarget by boolValue("Face Target", true)
    private var playersOnly by boolValue("Players", true)
    private var mobsOnly by boolValue("Mobs", false)

    private var strafeAngle = 0.0f
    private var lastTarget: Entity? = null

    fun onDisable() {
        lastTarget = null
    }

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled || interceptablePacket.packet !is PlayerAuthInputPacket) return

        val targets = findTargetsInRange()

        if (targets.isNotEmpty()) {
            val target = lastTarget?.let {
                if (it.distance(session.localPlayer) < targetRange && it.isValidTarget()) it else targets.first()
            } ?: targets.first()

            lastTarget = target
            strafeAroundTarget(target)
        } else {
            lastTarget = null
        }
    }

    private fun strafeAroundTarget(target: Entity) {
        val targetPos = target.vec3Position

        strafeAngle = (strafeAngle + movementSpeed) % 360f

        val angleRadians = Math.toRadians(strafeAngle.toDouble())
        val offsetXValue = circleRadius * cos(angleRadians).toFloat()
        val offsetZValue = circleRadius * sin(angleRadians).toFloat()

        val yPos = if (preserveY) {
            session.localPlayer.vec3Position.y
        } else {
            targetPos.y
        }

        val newPosition = Vector3f.from(
            targetPos.x + offsetXValue + offsetX,
            yPos + offsetY,
            targetPos.z + offsetZValue + offsetZ
        )

        val rotation = if (faceTarget) {
            calculateRotationToTarget(newPosition, targetPos)
        } else {
            session.localPlayer.vec3Rotation
        }

        val movePlayerPacket = MovePlayerPacket().apply {
            runtimeEntityId = session.localPlayer.runtimeEntityId
            position = newPosition
            this.rotation = rotation
            mode = MovePlayerPacket.Mode.NORMAL
            onGround = true  
            ridingRuntimeEntityId = 0
            tick = session.localPlayer.tickExists
        }

        session.clientBound(movePlayerPacket)  
    }

    private fun calculateRotationToTarget(playerPos: Vector3f, targetPos: Vector3f): Vector3f {
        val deltaX = targetPos.x - playerPos.x
        val deltaZ = targetPos.z - playerPos.z

        val yaw = (Math.toDegrees(atan2(deltaZ, deltaX).toDouble()).toFloat() - 90f + 360f) % 360f

        return Vector3f.from(
            session.localPlayer.vec3Rotation.x,
            yaw,
            session.localPlayer.vec3Rotation.z
        )
    }

    private fun findTargetsInRange(): List<Entity> {
        return session.level.entityMap.values
            .filter { it.runtimeEntityId != session.localPlayer.runtimeEntityId }
            .filter { it.distance(session.localPlayer) < targetRange && it.isValidTarget() }
            .sortedBy { it.distance(session.localPlayer) }
    }

    private fun Entity.isValidTarget(): Boolean {
        return when (this) {
            is Player -> (playersOnly || (playersOnly && mobsOnly)) && !isBot()
            is EntityUnknown -> (mobsOnly || (playersOnly && mobsOnly)) && true  
            else -> false
        }
    }

    private fun Player.isBot(): Boolean {
        if (this is LocalPlayer) return false
        val playerList = session.level.playerMap[this.uuid] ?: return true
        return playerList.name.toString().isBlank()
    }
}