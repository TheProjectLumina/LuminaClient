package com.project.lumina.client.game.world.chunk

import com.project.lumina.client.constructors.NetBound
import io.netty.buffer.ByteBuf

class Chunk(
    val x: Int,
    val z: Int,
    val dimension: Int,
    private val session: NetBound
) {
    val sectionStorage = Array(16) { ChunkSection() }
    val maximumHeight = 16 * sectionStorage.size

    val is384World: Boolean
        get() = sectionStorage.size > 16

    fun read(buffer: ByteBuf, subChunkCount: Int) {
        repeat(subChunkCount.coerceAtMost(sectionStorage.size)) {
            sectionStorage[it].read(buffer)
        }
    }

    fun readSubChunk(index: Int, buffer: ByteBuf) {
        if (index in sectionStorage.indices) {
            sectionStorage[index].read(buffer)
        }
    }

    fun getBlock(x: Int, y: Int, z: Int): Int {
        if (y !in 0 until maximumHeight) return 0
        return sectionStorage[y shr 4].getBlock(x, y and 15, z)
    }

    fun setBlock(x: Int, y: Int, z: Int, id: Int) {
        if (y !in 0 until maximumHeight) return
        sectionStorage[y shr 4].setBlock(x, y and 15, z, id)
    }

    val hash: Long
        get() = hash(x, z)

    companion object {
        fun hash(x: Int, z: Int): Long {
            return (x.toLong() shl 32) or (z.toLong() and 0xFFFFFFFFL)
        }
    }
}
