package com.project.lumina.client.game.module.impl.combat

import com.project.lumina.client.R
import com.project.lumina.client.game.InterceptablePacket
import com.project.lumina.client.constructors.Element
import com.project.lumina.client.constructors.CheatCategory
import com.project.lumina.client.game.entity.Entity
import com.project.lumina.client.game.entity.Player
import org.cloudburstmc.math.vector.Vector3f
import org.cloudburstmc.protocol.bedrock.data.entity.EntityDataTypes
import org.cloudburstmc.protocol.bedrock.packet.MovePlayerPacket
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random
import com.project.lumina.client.util.AssetManager

class OpFightBotElement(iconResId: Int = AssetManager.getAsset("ic_eye")) : Element(
    name = "Auto Fight",
    category = CheatCategory.Combat,
    iconResId,
    displayNameResId = AssetManager.getString("module_opfightbot_display_name")
) {
    
    private var playersOnly by boolValue("Players Only", false)
    private var filterInvisible by boolValue("Filter Invisible", true)
    
    
    private var mode by intValue("Mode", 1, 0..2) 
    private var range by floatValue("Range", 1.5f, 1.5f..4.0f)
    private var passive by boolValue("Passive", false)
    
    
    private var horizontalSpeed by floatValue("Horizontal Speed", 5.0f, 1.0f..7.0f)
    private var verticalSpeed by floatValue("Vertical Speed", 4.0f, 1.0f..7.0f)
    private var strafeSpeed by intValue("Strafe Speed", 20, 10..90)
    
    
    private var attackEnabled by boolValue("Attack", true)
    private var attackInterval by intValue("Interval", 5, 1..20)
    private var cpsValue by intValue("CPS", 5, 1..20)
    private var packets by intValue("Packets", 1, 1..10)

    private var lastAttackTime = 0L

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) return
        if (interceptablePacket.packet !is PlayerAuthInputPacket) return

        val inputPacket = interceptablePacket.packet as PlayerAuthInputPacket
        val currentTime = System.currentTimeMillis()
        val minAttackDelay = 1000L / cpsValue

        
        val playerPos = session.localPlayer.vec3Position
        val target = session.level.entityMap.values
            .filter { it != session.localPlayer }
            .filter { !playersOnly || it is Player } 
            .filter { !isEntityInvisible(it) }
            .minByOrNull {
                val dx = it.vec3Position.x - playerPos.x
                val dy = it.vec3Position.y - playerPos.y
                val dz = it.vec3Position.z - playerPos.z
                dx * dx + dy * dy + dz * dz
            } ?: return

        val distance = target.vec3Position.distance(playerPos)
        val targetPos = target.vec3Position

        if (distance < range) {
            
            val direction = Math.toRadians(when (mode) {
                0 -> Random.Default.nextDouble() * 360.0 
                1 -> ((session.localPlayer.tickExists * strafeSpeed) % 360).toDouble() 
                2 -> target.vec3Rotation.y + 180.0 
                else -> 0.0
            }).toFloat()

            val newPos = Vector3f.from(
                targetPos.x - sin(direction) * range,
                targetPos.y + 0.5f,
                targetPos.z + cos(direction) * range
            )

            
            val yaw = atan2(targetPos.z - playerPos.z, targetPos.x - playerPos.x).toFloat() + Math.toRadians(90.0).toFloat()
            val pitch = -atan2(
                targetPos.y - playerPos.y,
                Vector3f.from(targetPos.x, playerPos.y, targetPos.z).distance(playerPos)
            )

            session.clientBound(MovePlayerPacket().apply {
                runtimeEntityId = session.localPlayer.runtimeEntityId
                position = newPos
                rotation = Vector3f.from(pitch, yaw, yaw)
                mode = MovePlayerPacket.Mode.NORMAL
                onGround = true
                tick = session.localPlayer.tickExists
            })

            
            if (attackEnabled && (currentTime - lastAttackTime) >= minAttackDelay) {
                repeat(packets) {
                    session.localPlayer.attack(target)
                }
                lastAttackTime = currentTime
            }
        } else if (!passive) {
            
            val direction = atan2(targetPos.z - playerPos.z, targetPos.x - playerPos.x) - Math.toRadians(90.0).toFloat()
            val newPos = Vector3f.from(
                playerPos.x - sin(direction) * horizontalSpeed,
                targetPos.y.coerceIn(playerPos.y - verticalSpeed, playerPos.y + verticalSpeed),
                playerPos.z + cos(direction) * horizontalSpeed
            )

            session.clientBound(MovePlayerPacket().apply {
                runtimeEntityId = session.localPlayer.runtimeEntityId
                position = newPos
                rotation = session.localPlayer.vec3Rotation
                mode = MovePlayerPacket.Mode.NORMAL
                onGround = true
                tick = session.localPlayer.tickExists
            })
        }
    }

    private fun isEntityInvisible(entity: Entity): Boolean {
        if (!filterInvisible) return false
        
        if (entity.vec3Position.y < -30) return true
        
        val invisibilityFlag = entity.metadata[EntityDataTypes.FLAGS]?.let { flags ->
            if (flags is Long) {
                (flags and (1L shl 5)) != 0L
            } else false
        } ?: false
        
        if (invisibilityFlag) return true
        
        val name = entity.metadata[EntityDataTypes.NAME] as? String ?: ""
        if (name.contains("invisible", ignoreCase = true) || name.isEmpty()) return true
        
        return false
    }

}