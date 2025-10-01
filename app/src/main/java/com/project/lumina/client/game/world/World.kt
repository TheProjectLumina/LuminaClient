package com.project.lumina.client.game.world

import android.util.Log
import com.project.lumina.client.constructors.NetBound
import com.project.lumina.client.game.event.EventChunkLoad
import com.project.lumina.client.game.event.GameEvent
import com.project.lumina.client.game.event.Listenable
import com.project.lumina.client.game.world.chunk.Chunk
import org.cloudburstmc.math.vector.Vector3i
import org.cloudburstmc.protocol.bedrock.data.SubChunkRequestResult
import org.cloudburstmc.protocol.bedrock.packet.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.floor

class World(val session: NetBound) : Listenable {

    override val eventManager = session.eventManager
    private val pendingEvents = mutableListOf<GameEvent>()
    private val chunks = ConcurrentHashMap<Long, Chunk>()
    private var viewDistance = -1

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
        chunks.clear()
        viewDistance = packet.serverChunkTickRange
        Log.i("World", "🌍 Initialized World from StartGamePacket with viewDistance $viewDistance")
    }

    fun onPacket(packet: BedrockPacket) {
        when (packet) {
            is LevelChunkPacket -> {
                val chunk = Chunk(packet.chunkX, packet.chunkZ, packet.dimension, session)
                chunk.read(packet.data, packet.subChunksLength)
                chunks[chunk.hash] = chunk

                safeEmit(EventChunkLoad(session, chunk))
            }
            is ChunkRadiusUpdatedPacket -> {
                viewDistance = packet.radius
                cleanupChunks()
            }
            is SubChunkPacket -> handleSubChunk(packet)
            is ChangeDimensionPacket -> chunks.clear()
            is UpdateBlockPacket -> {
                if (packet.dataLayer == 0) {
                    val runtimeId = packet.definition?.runtimeId ?: 0
                    setBlockId(packet.blockPosition.x, packet.blockPosition.y, packet.blockPosition.z, runtimeId)

                    if (packet.definition == null) {
                        Log.w("World", "Received UpdateBlockPacket with null definition at position ${packet.blockPosition}")
                    }
                }
            }
        }
    }


    private fun handleLevelChunk(packet: LevelChunkPacket) {
        val chunk = Chunk(
            packet.chunkX,
            packet.chunkZ,
            packet.dimension,
            session
        )
        chunk.read(packet.data, packet.subChunksLength)
        chunks[chunk.hash] = chunk

        eventManager.emit(EventChunkLoad(session, chunk))
    }

    private fun handleSubChunk(packet: SubChunkPacket) {
        val center = packet.centerPosition
        for (subChunk in packet.subChunks) {
            if (subChunk.result != SubChunkRequestResult.SUCCESS) continue
            val pos = subChunk.position.add(center).add(0, 4, 0)
            getChunk(pos.x, pos.z)?.readSubChunk(pos.y, subChunk.data)
        }
    }

    private fun cleanupChunks() {
        if (viewDistance < 0) return
        val px = floor(session.localPlayer.posX).toInt() shr 4
        val pz = floor(session.localPlayer.posZ).toInt() shr 4
        val limit = viewDistance + 1

        chunks.entries.removeIf { (_, chunk) ->
            val dx = chunk.x - px
            val dz = chunk.z - pz
            dx * dx + dz * dz > limit * limit
        }
    }

    fun getBlockId(x: Int, y: Int, z: Int): Int {
        return getChunkAt(x, z)?.getBlock(x and 15, y, z and 15) ?: 0
    }

    fun setBlockId(x: Int, y: Int, z: Int, id: Int) {
        getChunkAt(x, z)?.setBlock(x and 15, y, z and 15, id)
    }

    private fun getChunkAt(x: Int, z: Int): Chunk? {
        return getChunk(x shr 4, z shr 4)
    }

    private fun getChunk(x: Int, z: Int): Chunk? {
        return chunks[Chunk.hash(x, z)]
    }

    fun getBlockIdAt(vec: Vector3i): Int = getBlockId(vec.x, vec.y, vec.z)

    fun setBlockIdAt(vec: Vector3i, id: Int) = setBlockId(vec.x, vec.y, vec.z, id)
}
