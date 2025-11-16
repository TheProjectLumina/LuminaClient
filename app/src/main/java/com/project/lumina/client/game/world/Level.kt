package com.project.lumina.client.game.world

import android.util.Log
import com.project.lumina.client.constructors.NetBound
import com.project.lumina.client.game.entity.Entity
import com.project.lumina.client.game.entity.EntityUnknown
import com.project.lumina.client.game.entity.Item
import com.project.lumina.client.game.entity.Player
import com.project.lumina.client.game.event.EventEntityDespawn
import com.project.lumina.client.game.event.EventEntitySpawn
import com.project.lumina.client.game.event.GameEvent
import com.project.lumina.client.game.event.Listenable
import com.project.lumina.client.constructors.MobAlertManager
import com.project.lumina.client.game.entity.MobList
import org.cloudburstmc.protocol.bedrock.packet.AddEntityPacket
import org.cloudburstmc.protocol.bedrock.packet.AddItemEntityPacket
import org.cloudburstmc.protocol.bedrock.packet.AddPlayerPacket
import org.cloudburstmc.protocol.bedrock.packet.BedrockPacket
import org.cloudburstmc.protocol.bedrock.packet.PlayerListPacket
import org.cloudburstmc.protocol.bedrock.packet.RemoveEntityPacket
import org.cloudburstmc.protocol.bedrock.packet.StartGamePacket
import org.cloudburstmc.protocol.bedrock.packet.TakeItemEntityPacket
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap


@Suppress("MemberVisibilityCanBePrivate")
class Level(val session: NetBound) : Listenable {

    override val eventManager = session.eventManager
    private val pendingEvents = mutableListOf<GameEvent>()
    val entityMap = ConcurrentHashMap<Long, Entity>()
    val playerMap = ConcurrentHashMap<UUID, PlayerListPacket.Entry>()

    private fun safeEmit(event: GameEvent) {
        if (eventManager != null) {
            if (pendingEvents.isNotEmpty()) {
                pendingEvents.forEach { eventManager.emit(it) }
                pendingEvents.clear()
            }
            eventManager.emit(event)
        } else {
            pendingEvents.add(event)
        }
    }

    fun initFromStartGame(packet: StartGamePacket) {
        entityMap.clear()
        playerMap.clear()
        Log.i("Level", "Initialized Level from StartGamePacket")
    }


    fun onDisconnect() {
        entityMap.clear()
        playerMap.clear()
    }

    fun onPacketBound(packet: BedrockPacket) {
        when (packet) {
            is AddEntityPacket -> {
                val entity = EntityUnknown(
                    packet.runtimeEntityId,
                    packet.uniqueEntityId,
                    packet.identifier
                ).apply {
                    move(packet.position)
                    rotate(packet.rotation)
                    handleSetData(packet.metadata)
                    handleSetAttribute(packet.attributes)
                }
                entityMap[packet.runtimeEntityId] = entity
                safeEmit(EventEntitySpawn(session, entity))
                
                if (packet.identifier in MobList.mobTypes) {
                    val mobName = packet.identifier.split(":").lastOrNull()?.replaceFirstChar {
                        if (it.isLowerCase()) it.titlecase() else it.toString()
                    } ?: "Unknown"
                    MobAlertManager.checkMobDetection(mobName)
                }
            }

            is AddItemEntityPacket -> {
                val entity = Item(packet.runtimeEntityId, packet.uniqueEntityId).apply {
                    move(packet.position)
                    handleSetData(packet.metadata)
                }
                entityMap[packet.runtimeEntityId] = entity
                safeEmit(EventEntitySpawn(session, entity))
            }

            is AddPlayerPacket -> {
                val entity = Player(
                    packet.runtimeEntityId,
                    packet.uniqueEntityId,
                    packet.uuid,
                    packet.username
                ).apply {
                    move(packet.position)
                    rotate(packet.rotation)
                    handleSetData(packet.metadata)
                }
                entityMap[packet.runtimeEntityId] = entity
                safeEmit(EventEntitySpawn(session, entity))
            }

            is RemoveEntityPacket -> {
                val entityToRemove =
                    entityMap.values.find { it.uniqueEntityId == packet.uniqueEntityId } ?: return
                entityMap.remove(entityToRemove.runtimeEntityId)
                safeEmit(EventEntityDespawn(session, entityToRemove))
            }

            is TakeItemEntityPacket -> {
                entityMap.remove(packet.itemRuntimeEntityId)
            }

            is PlayerListPacket -> {
                val add = packet.action == PlayerListPacket.Action.ADD
                packet.entries.forEach {
                    if (add) {
                        playerMap[it.uuid] = it
                    } else {
                        playerMap.remove(it.uuid)
                    }
                }
            }
            is StartGamePacket -> {
                entityMap.clear()
                playerMap.clear()
            }

            else -> {
                entityMap.values.forEach { entity ->
                    entity.onPacketBound(packet)
                }
            }
        }
    }

}