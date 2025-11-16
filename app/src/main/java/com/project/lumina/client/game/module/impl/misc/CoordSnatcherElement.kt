package com.project.lumina.client.game.module.impl.misc

import android.util.Log
import com.project.lumina.client.constructors.CheatCategory
import com.project.lumina.client.constructors.Element
import com.project.lumina.client.game.InterceptablePacket
import com.project.lumina.client.game.entity.Player
import com.project.lumina.client.util.AssetManager
import org.cloudburstmc.protocol.bedrock.packet.MovePlayerPacket
import kotlin.math.floor

class CoordSnatcherElement : Element(
    name = "CoordSnatcher",
    category = CheatCategory.Misc,
    displayNameResId = AssetManager.getString("module_coord_snatcher_display_name")
) {
    
    private val enableNotifications by boolValue("Notifications", true)
    private val logToConsole by boolValue("Log to Console", false)
    private val trackAllPlayers by boolValue("Track All Players", true)
    
    private val trackedPlayers = mutableSetOf<Long>()
    private val playerCoords = mutableMapOf<Long, Triple<Int, Int, Int>>()
    private var sessionStartTime = 0L
    
    override fun onEnabled() {
        super.onEnabled()
        sessionStartTime = System.currentTimeMillis()
        if (isSessionCreated) {
            session.displayClientMessage("§a§lEnabled §eCoord Snatcher§a. Tracking player teleports...")
        }
    }
    
    override fun onDisabled() {
        super.onDisabled()
        trackedPlayers.clear()
        playerCoords.clear()
        if (isSessionCreated) {
            session.displayClientMessage("§c§lDisabled §eCoord Snatcher§c.")
        }
    }
    
    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) return

        if (!isSessionCreated) {
            Log.d("CoordSnatcher", "Packet received before session initialization, skipping")
            return
        }

        try {
            val packet = interceptablePacket.packet
            if (packet is MovePlayerPacket && packet.mode == MovePlayerPacket.Mode.TELEPORT) {
                handleTeleportPacket(packet)
            }
        } catch (e: Exception) {
            Log.e("CoordSnatcher", "Error processing packet ${interceptablePacket.packet.javaClass.simpleName}: ${e.javaClass.simpleName} - ${e.message}")
        }
    }
    
    private fun handleTeleportPacket(packet: MovePlayerPacket) {
        try {
            val runtimeId = packet.runtimeEntityId
            val position = packet.position

            val x = floor(position.x).toInt()
            val y = floor(position.y).toInt()
            val z = floor(position.z).toInt()

            Log.d("CoordSnatcher", "Processing teleport packet for runtimeId: $runtimeId to position: ($x, $y, $z)")

            if (System.currentTimeMillis() - sessionStartTime < 2000) {
                Log.d("CoordSnatcher", "Ignoring early teleport during initialization period")
                return
            }

            if (session.level.entityMap.isEmpty()) {
                Log.d("CoordSnatcher", "Entity map is empty, cannot process teleport packet")
                return
            }

            val player = findPlayerByRuntimeId(runtimeId)
            if (player == null) {
                Log.d("CoordSnatcher", "Player with runtimeId $runtimeId not found in entity map")
                return
            }

            val playerName = player.username.takeIf { it.isNotEmpty() } ?: "Unknown Player"
            Log.d("CoordSnatcher", "Found player: $playerName (runtimeId: $runtimeId)")

            if (trackAllPlayers || trackedPlayers.contains(runtimeId)) {
                val previousCoords = playerCoords[runtimeId]
                val newCoords = Triple(x, y, z)

                if (previousCoords != newCoords) {
                    playerCoords[runtimeId] = newCoords

                    if (!trackedPlayers.contains(runtimeId)) {
                        trackedPlayers.add(runtimeId)
                        if (enableNotifications) {
                            session.displayClientMessage("§b§lCoord Snatcher §r§7Now tracking §e$playerName")
                        }
                        Log.i("CoordSnatcher", "Started tracking player: $playerName")
                    }

                    val message = "§aSnatched §e$playerName's §aCoordinates. X: §e$x §aY: §e$y §aZ: §e$z"

                    if (enableNotifications) {
                        session.displayClientMessage(message)
                    }

                    if (logToConsole) {
                        Log.i("CoordSnatcher", "Snatched $playerName's Coordinates: X=$x Y=$y Z=$z")
                    }

                    Log.d("CoordSnatcher", "Successfully updated coordinates for $playerName: $previousCoords -> $newCoords")
                } else {
                    Log.d("CoordSnatcher", "Player $playerName coordinates unchanged: $newCoords")
                }
            } else {
                Log.d("CoordSnatcher", "Player $playerName not being tracked (trackAllPlayers: $trackAllPlayers)")
            }
        } catch (e: Exception) {
            Log.e("CoordSnatcher", "Error handling teleport packet: ${e.javaClass.simpleName} - ${e.message}")
        }
    }
    
    private fun findPlayerByRuntimeId(runtimeId: Long): Player? {
        return try {
            val entity = session.level.entityMap[runtimeId]
            if (entity is Player) {
                entity
            } else {
                Log.d("CoordSnatcher", "Entity with runtimeId $runtimeId is not a Player: ${entity?.javaClass?.simpleName ?: "null"}")
                null
            }
        } catch (e: Exception) {
            Log.e("CoordSnatcher", "Error finding player by runtimeId $runtimeId: ${e.javaClass.simpleName} - ${e.message}")
            null
        }
    }
    
    fun getTrackedPlayersCount(): Int = trackedPlayers.size
    
    fun getPlayerCoordinates(runtimeId: Long): Triple<Int, Int, Int>? = playerCoords[runtimeId]
    
    fun clearTrackedPlayer(runtimeId: Long) {
        trackedPlayers.remove(runtimeId)
        playerCoords.remove(runtimeId)
    }
    
    fun clearAllTracked() {
        trackedPlayers.clear()
        playerCoords.clear()
        if (isSessionCreated) {
            session.displayClientMessage("§b§lCoord Snatcher §r§7Cleared all tracked players")
        }
    }
}
