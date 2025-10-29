package com.project.lumina.client.game.module.impl.misc

import com.project.lumina.client.game.InterceptablePacket
import com.project.lumina.client.constructors.Element
import com.project.lumina.client.constructors.CheatCategory
import com.project.lumina.client.util.AssetManager
import net.kyori.adventure.text.Component
import org.cloudburstmc.math.vector.Vector3f
import org.cloudburstmc.protocol.bedrock.data.skin.SerializedSkin
import org.cloudburstmc.protocol.bedrock.packet.MoveEntityAbsolutePacket
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket
import org.cloudburstmc.protocol.bedrock.packet.PlayerListPacket
import org.cloudburstmc.protocol.bedrock.packet.TextPacket
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.ceil
import kotlin.math.sqrt


class PlayerTracerElement : Element(
    name = "PlayerTracer",
    category = CheatCategory.Misc,
    displayNameResId = AssetManager.getString("module_player_tracer_display_name")
) {
    
    private val playersInfo = mutableMapOf<Long, PlayerInfo>()
    private var playerPosition = Vector3f.from(0f, 0f, 0f)

    
    private val previousPositions = mutableMapOf<Long, Vector3f>()
    private val previousTimestamps = mutableMapOf<Long, Long>()

    
    private val scanRadius = intValue("Scan Radius", 500, 100..100000)

    private var showBots by boolValue("Show Bots", true)

    
    data class PlayerInfo(
        val entityId: Long,
        val name: String,
        val xuid: String,
        val platformChatId: String,
        val buildPlatform: Int,
        val skin: SerializedSkin
    )

    
    private fun calculateVelocity(
        entityId: Long,
        currentPosition: Vector3f,
        currentTime: Long
    ): Vector3f? {
        val previousPosition = previousPositions[entityId]
        val previousTimestamp = previousTimestamps[entityId]

        return if (previousPosition != null && previousTimestamp != null) {
            val timeDelta = currentTime - previousTimestamp
            if (timeDelta > 0) {
                
                val velocity = Vector3f.from(
                    (currentPosition.x - previousPosition.x) / timeDelta,
                    (currentPosition.y - previousPosition.y) / timeDelta,
                    (currentPosition.z - previousPosition.z) / timeDelta
                )
                velocity
            } else {
                null
            }
        } else {
            null
        }
    }

    
    private fun updatePositionAndTimestamp(entityId: Long, currentPosition: Vector3f) {
        val currentTime = System.currentTimeMillis()
        previousPositions[entityId] = currentPosition
        previousTimestamps[entityId] = currentTime
    }

    
    private fun sendMessage(
        playerInfo: PlayerInfo,
        entityPosition: Vector3f,
        distance: Float,
        direction: String,
        velocity: Vector3f?,
        actionState: String?
    ) {
        val timestamp = System.currentTimeMillis()
        val lastKnownPosition =
            previousPositions[playerInfo.entityId]?.roundUpCoordinates() ?: "N/A"

        val textPacket = TextPacket().apply {
            type = TextPacket.Type.RAW
           
            message ="""
        §l§b[CutieAI]§r §ePlayer Gamertag: §a${playerInfo.name} §e| §eEntity ID: §c${playerInfo.entityId} §e| §ePosition: §a${entityPosition.roundUpCoordinates()} §e| §eDistance: §c${
                ceil(
                    distance
                )
            } §e| §eDirection: §d$direction
        §l§b[CutieAI]§r §7Additional Info: §fXbox UID: §7${playerInfo.xuid} §e| §7Height Difference: §f${
                ceil(
                    entityPosition.y - playerPosition.y
                )
            } Blocks §e| §7Last Known Position: §f$lastKnownPosition
    """.trimIndent()
            xuid = ""
            sourceName = ""
        }

        session.clientBound(textPacket) 
    }

    
    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) {
            return
        }

        
        val packet = interceptablePacket.packet
        if (packet is PlayerListPacket) {
            packet.entries.forEach { entry ->
                playersInfo[entry.entityId] = PlayerInfo(
                    entityId = entry.entityId,
                    name = entry.name.toString(),
                    xuid = entry.xuid,
                    platformChatId = entry.platformChatId,
                    buildPlatform = entry.buildPlatform,
                    skin = entry.skin
                )
            }
        }

        
        if (packet is PlayerAuthInputPacket) {
            playerPosition = packet.position
        }

        
        if (packet is MoveEntityAbsolutePacket) {
            val entityId = packet.runtimeEntityId
            val entityPosition = packet.position
            val currentTime = System.currentTimeMillis()

            
            updatePositionAndTimestamp(entityId, entityPosition)

            
            val velocity = calculateVelocity(entityId, entityPosition, currentTime)

            
            val storedPlayerInfo = playersInfo[entityId]
            if (storedPlayerInfo != null) {
                
                val distance = calculateDistance(playerPosition, entityPosition)

                
                if (distance <= scanRadius.value.toFloat()) {
                    val direction = getCompassDirection(playerPosition, entityPosition)

                    
                    sendMessage(
                        storedPlayerInfo,
                        entityPosition,
                        distance,
                        direction,
                        velocity,
                        null
                    )
                }
            }
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
        return "$roundedX, $roundedY, $roundedZ"
    }

    
    private fun getCompassDirection(from: Vector3f, to: Vector3f): String {
        val dx = to.x - from.x
        val dz = to.z - from.z
        val angle = (atan2(dz, dx) * (180 / PI) + 360) % 360
        return when {
            angle >= 348.75 || angle < 11.25 -> "N"
            angle >= 11.25 && angle < 33.75 -> "NNE"
            angle >= 33.75 && angle < 56.25 -> "NE"
            angle >= 56.25 && angle < 78.75 -> "ENE"
            angle >= 78.75 && angle < 101.25 -> "E"
            angle >= 101.25 && angle < 123.75 -> "ESE"
            angle >= 123.75 && angle < 146.25 -> "SE"
            angle >= 146.25 && angle < 168.75 -> "SSE"
            angle >= 168.75 && angle < 191.25 -> "S"
            angle >= 191.25 && angle < 213.75 -> "SSW"
            angle >= 213.75 && angle < 236.25 -> "SW"
            angle >= 236.25 && angle < 258.75 -> "WSW"
            angle >= 258.75 && angle < 281.25 -> "W"
            angle >= 281.25 && angle < 303.75 -> "WNW"
            angle >= 303.75 && angle < 326.25 -> "NW"
            else -> "NNW"
        }
    }
}
