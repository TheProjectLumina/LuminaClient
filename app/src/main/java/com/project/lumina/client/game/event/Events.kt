package com.project.lumina.client.game.event

import com.project.lumina.client.constructors.NetBound
import com.project.lumina.client.game.entity.Entity
import com.project.lumina.client.game.inventory.AbstractInventory
import com.project.lumina.client.game.world.chunk.Chunk
import org.cloudburstmc.protocol.bedrock.packet.BedrockPacket

abstract class GameEvent(val session: NetBound, val friendlyName: String)

abstract class GameEventCancellable(session: NetBound, friendlyName: String) : GameEvent(session, friendlyName) {

    private var canceled = false

    open fun cancel() {
        canceled = true
    }

    open fun isCanceled() = canceled

}

class EventTick(session: NetBound) : GameEvent(session, "tick")

/**
 * @param reason BedrockDisconnectReasons
 */
class EventDisconnect(session: NetBound, val client: Boolean, val reason: String) : GameEvent(session, "disconnect")

class EventPacketInbound(session: NetBound, val packet: BedrockPacket) : GameEventCancellable(session, "packet_inbound")

class EventPacketOutbound(session: NetBound, val packet: BedrockPacket) : GameEventCancellable(session, "packet_outbound")

/**
 * the container just "initialized", the content not received
 */
class EventContainerOpen(session: NetBound, val container: AbstractInventory) : GameEvent(session, "container_open")

class EventContainerClose(session: NetBound, val container: AbstractInventory) : GameEvent(session, "container_close")

/**
 * triggered on LevelChunkPacket,
 * but be aware if the chunk have separate subchunks deliver, the subchunk will not be loaded on the event call
 */
class EventChunkLoad(session: NetBound, val chunk: Chunk) : GameEvent(session, "chunk_load")

class EventChunkUnload(session: NetBound, val chunk: Chunk) : GameEvent(session, "chunk_unload")

class EventDimensionChange(session: NetBound, val dimension: Int) : GameEvent(session, "dimension_change")

class EventEntitySpawn(session: NetBound, val entity: Entity) : GameEvent(session, "entity_spawn")

class EventEntityDespawn(session: NetBound, val entity: Entity) : GameEvent(session, "entity_despawn")
