/*
 * © Project Lumina 2026 — Licensed under GNU GPLv3
 */

package com.project.lumina.client.game.module.impl.world

import com.project.lumina.client.R
import com.project.lumina.client.game.InterceptablePacket
import com.project.lumina.client.constructors.Element
import com.project.lumina.client.constructors.CheatCategory
import com.project.lumina.client.game.entity.Player
import com.project.lumina.client.game.entity.LocalPlayer
import com.project.lumina.client.overlay.mods.MiniMapOverlay
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket
import org.cloudburstmc.protocol.bedrock.packet.RemoveEntityPacket
import org.cloudburstmc.protocol.bedrock.packet.PlayerListPacket
import kotlin.math.PI
import kotlin.math.sqrt

class MinimapElement : Element(
    name = "Minimap",
    category = CheatCategory.World,
    displayNameResId = R.string.module_minimap_display_name
) {

    private val playerOnly by boolValue("Players", false)
    private val mobsOnly by boolValue("Mobs", true)
    private val rangeValue = 1000f
    private val sizeOption by intValue("Size", 100, 60..300)
    private val zoomOption by floatValue("Zoom", 1.0f, 0.5f..2.0f)
    private val dotSizeOption by intValue("DotSize", 5, 1..10)

    // Track players separately for cleanup
    private val trackedPlayers = mutableSetOf<Long>()
    private var lastCleanupTime = 0L
    private val cleanupInterval = 3000L // Cleanup every 3 seconds

    override fun onEnabled() {
        super.onEnabled()
        try {
            if (isSessionCreated) {
                session.enableMinimap(true)
                updateMinimapSettings()
                clearAllTracking()
            }
        } catch (e: Exception) {
            println("Error enabling Minimap: ${e.message}")
        }
    }

    override fun onDisabled() {
        super.onDisabled()
        if (isSessionCreated) {
            session.enableMinimap(false)
            clearAllTracking()
        }
    }

    override fun onDisconnect(reason: String) {
        if (isSessionCreated) {
            clearAllTracking()
        }
    }

    private fun updateMinimapSettings() {
        MiniMapOverlay.setMinimapSize(sizeOption.toFloat())
        MiniMapOverlay.overlayInstance.minimapZoom = zoomOption
        MiniMapOverlay.overlayInstance.minimapDotSize = dotSizeOption
    }

    private fun clearAllTracking() {
        trackedPlayers.clear()
        MiniMapOverlay.clearAllEntities()
        lastCleanupTime = 0L
    }

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled || !isSessionCreated) return

        val packet = interceptablePacket.packet

        when (packet) {
            is PlayerAuthInputPacket -> {
                // Update player position and rotation
                val position = packet.position
                session.updatePlayerPosition(position.x, position.z)

                val yawRadians = (packet.rotation.y * PI / 180).toFloat()
                session.updatePlayerRotation(yawRadians)

                // Update entities from EntityStorage (mobs)
                if (mobsOnly || (playerOnly && mobsOnly)) {
                    val entities = session.getStoredEntities()
                    entities.values.forEach { entityInfo ->
                        MiniMapOverlay.updateEntity(
                            id = entityInfo.id,
                            x = entityInfo.coords.x,
                            y = entityInfo.coords.z,
                            name = entityInfo.name,
                            imagePath = entityInfo.imagePath,
                            isPlayer = false
                        )
                    }
                }

                // Update players
                if (playerOnly || (playerOnly && mobsOnly)) {
                    updatePlayers()
                }

                // Periodic cleanup
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastCleanupTime > cleanupInterval) {
                    performCleanup()
                    lastCleanupTime = currentTime
                }

                updateMinimapSettings()
            }

            is RemoveEntityPacket -> {
                // Remove entity from minimap - need to find by uniqueEntityId
                val entity = session.level.entityMap.values.find {
                    it.uniqueEntityId == packet.uniqueEntityId
                }
                if (entity != null) {
                    MiniMapOverlay.removeEntity(entity.runtimeEntityId)
                    trackedPlayers.remove(entity.runtimeEntityId)
                }
            }

            is PlayerListPacket -> {
                // Handle player disconnections
                if (packet.action == PlayerListPacket.Action.REMOVE) {
                    packet.entries.forEach { entry ->
                        // Find player entity by UUID
                        val playerEntity = session.level.entityMap.values.find { entity ->
                            entity is Player && entity.uuid == entry.uuid
                        }
                        if (playerEntity != null) {
                            MiniMapOverlay.removeEntity(playerEntity.runtimeEntityId)
                            trackedPlayers.remove(playerEntity.runtimeEntityId)
                        }
                    }
                }
            }
        }
    }

    private fun updatePlayers() {
        val currentPlayers = mutableSetOf<Long>()
        val localPlayerPos = session.localPlayer.vec3Position

        // Iterate through entity map to find Player entities
        session.level.entityMap.values.forEach { entity ->
            if (entity !is Player) return@forEach
            if (entity is LocalPlayer) return@forEach

            // Check if bot by looking up in playerMap
            val playerListEntry = session.level.playerMap[entity.uuid]
            if (playerListEntry == null || playerListEntry.name.toString().isBlank()) {
                return@forEach
            }

            // Calculate distance
            val entityPos = entity.vec3Position
            val dx = entityPos.x - localPlayerPos.x
            val dy = entityPos.y - localPlayerPos.y
            val dz = entityPos.z - localPlayerPos.z
            val distance = sqrt((dx * dx + dy * dy + dz * dz).toDouble()).toFloat()

            if (distance > rangeValue) return@forEach

            val entityId = entity.runtimeEntityId
            currentPlayers.add(entityId)
            trackedPlayers.add(entityId)

            MiniMapOverlay.updateEntity(
                id = entityId,
                x = entityPos.x,
                y = entityPos.z,
                name = entity.username,
                imagePath = null, // Players don't have image paths
                isPlayer = true
            )
        }

        // Remove players no longer in range
        val removedPlayers = trackedPlayers - currentPlayers
        removedPlayers.forEach { playerId ->
            MiniMapOverlay.removeEntity(playerId)
            trackedPlayers.remove(playerId)
        }
    }

    private fun performCleanup() {
        // Verify all tracked players still exist in entity map
        val validPlayers = mutableSetOf<Long>()

        trackedPlayers.forEach { playerId ->
            val playerExists = session.level.entityMap.containsKey(playerId)
            if (playerExists) {
                validPlayers.add(playerId)
            } else {
                // Entity no longer exists, remove from minimap
                MiniMapOverlay.removeEntity(playerId)
            }
        }

        // Update tracked players to only include valid ones
        trackedPlayers.clear()
        trackedPlayers.addAll(validPlayers)
    }
}