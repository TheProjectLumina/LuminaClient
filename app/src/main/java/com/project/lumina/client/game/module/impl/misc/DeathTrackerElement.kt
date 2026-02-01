package com.project.lumina.client.game.module.impl.misc

import android.util.Log
import com.project.lumina.client.constructors.CheatCategory
import com.project.lumina.client.constructors.Element
import com.project.lumina.client.game.InterceptablePacket
import com.project.lumina.client.game.entity.Player
import com.project.lumina.client.util.AssetManager
import org.cloudburstmc.math.vector.Vector3f
import org.cloudburstmc.protocol.bedrock.packet.AddPlayerPacket
import org.cloudburstmc.protocol.bedrock.packet.RemoveEntityPacket
import java.util.UUID
import kotlin.math.floor
import kotlin.math.sqrt

class DeathTrackerElement : Element(
    name = "DeathTracker",
    category = CheatCategory.Misc,
    displayNameResId = AssetManager.getString("module_death_tracker_display_name")
) {
    
    private val enableDeathNotifications by boolValue("Death Notifications", true)
    private val enableRespawnNotifications by boolValue("Respawn Notifications", true)
    private val showDistance by boolValue("Show Distance", true)
    private val maxTrackingDistance by intValue("Max Distance", 1000, 100..5000)
    private val autoCleanupMinutes by intValue("Auto Cleanup (min)", 30, 5..120)
    
    private val deathLocations = mutableMapOf<UUID, Triple<Vector3f, String, Long>>()
    private val bedLocations = mutableMapOf<UUID, Triple<Vector3f, String, Long>>()
    
    override fun onEnabled() {
        super.onEnabled()
        if (isSessionCreated) {
            session.displayClientMessage("§a§lEnabled §eDeathTracker§a. Tracking player deaths and bed locations...")
        }
    }
    
    override fun onDisabled() {
        super.onDisabled()
        clearAllData()
        if (isSessionCreated) {
            session.displayClientMessage("§c§lDisabled §eDeathTracker§c.")
        }
    }
    
    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) return

        if (!isSessionCreated) {
            Log.d("DeathTracker", "Packet received before session initialization, skipping")
            return
        }

        try {
            val packet = interceptablePacket.packet
            when (packet) {
                is RemoveEntityPacket -> handlePlayerDeath(packet)
                is AddPlayerPacket -> handlePlayerRespawn(packet)
            }

            cleanupOldData()
        } catch (e: Exception) {
            Log.e("DeathTracker", "Error processing packet ${interceptablePacket.packet.javaClass.simpleName}: ${e.javaClass.simpleName} - ${e.message}")
        }
    }
    
    private fun handlePlayerDeath(packet: RemoveEntityPacket) {
        try {
            if (session.level.entityMap.isEmpty()) {
                Log.d("DeathTracker", "Entity map is empty, cannot process death packet")
                return
            }

            val entity = session.level.entityMap.values.find { it.uniqueEntityId == packet.uniqueEntityId }
            if (entity == null) {
                Log.d("DeathTracker", "Entity with uniqueId ${packet.uniqueEntityId} not found in entity map")
                return
            }

            if (entity !is Player) {
                Log.d("DeathTracker", "Entity ${packet.uniqueEntityId} is not a player, ignoring death")
                return
            }

            val position = entity.vec3Position
            val playerName = entity.username.takeIf { it.isNotEmpty() } ?: "Unknown Player"
            val currentTime = System.currentTimeMillis()

            Log.d("DeathTracker", "Processing death for player: $playerName at position: $position")

            if (isWithinTrackingDistance(position)) {
                deathLocations[entity.uuid] = Triple(position, playerName, currentTime)

                if (enableDeathNotifications) {
                    val distance = if (showDistance) {
                        val dist = calculateDistance(position)
                        " §7(${dist}m away)"
                    } else ""

                    val x = floor(position.x).toInt()
                    val y = floor(position.y).toInt()
                    val z = floor(position.z).toInt()

                    session.displayClientMessage("§c[DeathTracker] §e$playerName §cdied at §f$x $y $z$distance")
                }

                Log.i("DeathTracker", "Successfully tracked death: $playerName at $position")
            } else {
                Log.d("DeathTracker", "Player $playerName death at $position is outside tracking distance")
            }
        } catch (e: Exception) {
            Log.e("DeathTracker", "Error handling player death packet: ${e.javaClass.simpleName} - ${e.message}")
        }
    }
    
    private fun handlePlayerRespawn(packet: AddPlayerPacket) {
        try {
            val uuid = packet.uuid
            val playerName = packet.username.takeIf { it.isNotEmpty() } ?: "Unknown Player"
            val respawnPosition = packet.position
            val currentTime = System.currentTimeMillis()

            Log.d("DeathTracker", "Processing respawn for player: $playerName (UUID: $uuid) at position: $respawnPosition")

            if (!deathLocations.containsKey(uuid)) {
                Log.d("DeathTracker", "No previous death recorded for player $playerName, ignoring respawn")
                return
            }

            if (!isWithinTrackingDistance(respawnPosition)) {
                Log.d("DeathTracker", "Player $playerName respawn at $respawnPosition is outside tracking distance")
                return
            }

            bedLocations[uuid] = Triple(respawnPosition, playerName, currentTime)

            if (enableRespawnNotifications) {
                val distance = if (showDistance) {
                    val dist = calculateDistance(respawnPosition)
                    " §7(${dist}m away)"
                } else ""

                val x = floor(respawnPosition.x).toInt()
                val y = floor(respawnPosition.y).toInt()
                val z = floor(respawnPosition.z).toInt()

                session.displayClientMessage("§a[DeathTracker] §e$playerName §arespawned at §f$x $y $z §7(bed location)$distance")
            }

            Log.i("DeathTracker", "Successfully tracked respawn: $playerName at $respawnPosition")
        } catch (e: Exception) {
            Log.e("DeathTracker", "Error handling player respawn packet: ${e.javaClass.simpleName} - ${e.message}")
        }
    }
    
    private fun isWithinTrackingDistance(position: Vector3f): Boolean {
        val distance = calculateDistance(position)
        return distance <= maxTrackingDistance
    }
    
    private fun calculateDistance(position: Vector3f): Int {
        return try {
            val playerPos = session.localPlayer.vec3Position
            val dx = position.x - playerPos.x
            val dy = position.y - playerPos.y
            val dz = position.z - playerPos.z
            sqrt(dx * dx + dy * dy + dz * dz).toInt()
        } catch (e: Exception) {
            Log.e("DeathTracker", "Error calculating distance: ${e.javaClass.simpleName} - ${e.message}")
            0
        }
    }
    
    private fun cleanupOldData() {
        val currentTime = System.currentTimeMillis()
        val cleanupThreshold = autoCleanupMinutes * 60 * 1000L
        
        deathLocations.entries.removeIf { (_, data) ->
            currentTime - data.third > cleanupThreshold
        }
        
        bedLocations.entries.removeIf { (_, data) ->
            currentTime - data.third > cleanupThreshold
        }
    }
    
    fun getTrackedDeaths(): Map<UUID, Triple<Vector3f, String, Long>> = deathLocations.toMap()
    
    fun getTrackedBeds(): Map<UUID, Triple<Vector3f, String, Long>> = bedLocations.toMap()
    
    fun clearAllData() {
        deathLocations.clear()
        bedLocations.clear()
        if (isSessionCreated) {
            session.displayClientMessage("§b[DeathTracker] §7Cleared all tracked data")
        }
    }
    
    fun getTrackedPlayersCount(): Int = (deathLocations.keys + bedLocations.keys).distinct().size
}
