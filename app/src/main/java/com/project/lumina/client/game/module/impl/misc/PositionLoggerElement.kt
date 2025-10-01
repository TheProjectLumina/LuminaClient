package com.project.lumina.client.game.module.impl.misc

import com.project.lumina.client.game.InterceptablePacket
import com.project.lumina.client.constructors.Element
import com.project.lumina.client.constructors.CheatCategory
import com.project.lumina.client.util.AssetManager
import net.kyori.adventure.text.Component
import org.cloudburstmc.math.vector.Vector3f
import org.cloudburstmc.protocol.bedrock.packet.MoveEntityAbsolutePacket
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket
import org.cloudburstmc.protocol.bedrock.packet.TextPacket
import kotlin.math.atan2
import kotlin.math.ceil
import kotlin.math.sqrt

class PositionLoggerElement : Element(
    name = "PosLogger",
    category = CheatCategory.Misc,
    displayNameResId = AssetManager.getString("module_position_logger_display_name")
) {

    private var playerPosition = Vector3f.from(0f, 0f, 0f)

    private val entityPositions = mutableMapOf<Long, Vector3f>()

    private val logInterval by intValue("Interval", 5000, 1000..60000)

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) {
            return
        }

        val packet = interceptablePacket.packet
        if (packet is PlayerAuthInputPacket) {
            playerPosition = packet.position

            
            var closestEntityId: Long? = null
            var closestDistance = Float.MAX_VALUE
            var closestEntityPosition: Vector3f? = null

            entityPositions.forEach { (entityId, entityPos) ->
                val distance = calculateDistance(playerPosition, entityPos)
                if (distance < closestDistance) {
                    closestDistance = distance
                    closestEntityId = entityId
                    closestEntityPosition = entityPos
                }
            }

            
            if (closestEntityId != null && closestEntityPosition != null) {
                val roundedPosition = closestEntityPosition!!.roundUpCoordinates()
                val roundedDistance = ceil(closestDistance)
                val direction = getCompassDirection(playerPosition, closestEntityPosition!!)
                sendMessage("§l§b[PositionLogger]§r §eClosest entity at §a$roundedPosition §e| Distance: §c$roundedDistance §e| Direction: §d$direction")
            }
        }

        if (packet is MoveEntityAbsolutePacket) {
            val entityId = packet.runtimeEntityId
            val entityPosition = packet.position
            entityPositions[entityId] = entityPosition
        }
    }

    
    private fun calculateDistance(from: Vector3f, to: Vector3f): Float {
        val dx = from.x - to.x
        val dy = from.y - to.y
        val dz = from.z - to.z
        return sqrt((dx * dx + dy * dy + dz * dz).toDouble()).toFloat()
    }

    
    private fun Vector3f.roundUpCoordinates(): String {
        val roundedX = ceil(this.x).toInt()
        val roundedY = ceil(this.y).toInt()
        val roundedZ = ceil(this.z).toInt()
        return "($roundedX, $roundedY, $roundedZ)"
    }

    
    private fun getCompassDirection(from: Vector3f, to: Vector3f): String {
        val dx = to.x - from.x
        val dz = to.z - from.z
        val angle = Math.toDegrees(atan2(dx, dz).toDouble()).let {
            ((it + 360) % 360)
        }

        return when {
            angle >= 337.5 || angle < 22.5 -> "N"
            angle >= 22.5 && angle < 67.5 -> "NE"
            angle >= 67.5 && angle < 112.5 -> "E"
            angle >= 112.5 && angle < 157.5 -> "SE"
            angle >= 157.5 && angle < 202.5 -> "S"
            angle >= 202.5 && angle < 247.5 -> "SW"
            angle >= 247.5 && angle < 292.5 -> "W"
            else -> "NW"
        }
    }

    
    private fun sendMessage(msg: String) {
        val textPacket = TextPacket().apply {
            type = TextPacket.Type.RAW
           
            message = msg
            xuid = ""
            sourceName = ""
        }
        session.clientBound(textPacket)
    }
}
