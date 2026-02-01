package com.project.lumina.client.game.module.impl.world

import com.project.lumina.client.R
import com.project.lumina.client.game.InterceptablePacket
import com.project.lumina.client.constructors.Element
import com.project.lumina.client.constructors.CheatCategory
import org.cloudburstmc.math.vector.Vector3f
import org.cloudburstmc.protocol.bedrock.packet.MovePlayerPacket
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class FollowBotElement : Element(
    name = "FollowBot",
    category = CheatCategory.World,
    displayNameResId = R.string.module_followbot_display_name
) {
    private var followDistance by floatValue("Distance", 2.0f, 1.0f..5.0f)
    private var followMode by intValue("Mode", 0, 0..1) 
    private var speed by floatValue("Speed", 3.0f, 1.0f..7.0f)
    private var passive by boolValue("Passive", false)

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) return
        if (interceptablePacket.packet !is PlayerAuthInputPacket) return

        val inputPacket = interceptablePacket.packet as PlayerAuthInputPacket

        
        val playerPos = session.localPlayer.vec3Position
        val target = session.level.entityMap.values
            .filter { it != session.localPlayer }
            .minByOrNull {
                val dx = it.vec3Position.x - playerPos.x
                val dy = it.vec3Position.y - playerPos.y
                val dz = it.vec3Position.z - playerPos.z
                dx * dx + dy * dy + dz * dz
            } ?: return

        val distance = target.vec3Position.distance(playerPos)
        val targetPos = target.vec3Position

        if (distance > followDistance && !passive) {
            
            val direction = when (followMode) {
                0 -> atan2(targetPos.z - playerPos.z, targetPos.x - playerPos.x) - Math.toRadians(180.0).toFloat() 
                1 -> atan2(targetPos.z - playerPos.z, targetPos.x - playerPos.x) - Math.toRadians(90.0).toFloat() 
                else -> atan2(targetPos.z - playerPos.z, targetPos.x - playerPos.x) - Math.toRadians(180.0).toFloat()
            }

            val newPos = Vector3f.from(
                playerPos.x - sin(direction) * speed,
                targetPos.y.coerceIn(playerPos.y - 0.5f, playerPos.y + 0.5f), 
                playerPos.z + cos(direction) * speed
            )

            
            val yaw = atan2(targetPos.z - playerPos.z, targetPos.x - playerPos.x).toFloat() + Math.toRadians(90.0).toFloat()
            val pitch = -atan2(
                targetPos.y - playerPos.y,
                Vector3f.from(targetPos.x, playerPos.y, targetPos.z).distance(playerPos)
            ).toFloat()

            session.clientBound(MovePlayerPacket().apply {
                runtimeEntityId = session.localPlayer.runtimeEntityId
                position = newPos
                rotation = Vector3f.from(pitch, yaw, yaw)
                mode = MovePlayerPacket.Mode.NORMAL
                onGround = true
                tick = session.localPlayer.tickExists
            })
        }
    }

    private fun Vector3f.distance(other: Vector3f): Float {
        val dx = x - other.x
        val dy = y - other.y
        val dz = z - other.z
        return sqrt(dx * dx + dy * dy + dz * dz)
    }
}