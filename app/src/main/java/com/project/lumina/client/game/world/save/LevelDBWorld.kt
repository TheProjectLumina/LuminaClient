package com.project.lumina.client.game.world.save

import com.project.lumina.client.game.world.chunk.Chunk
import com.project.lumina.client.game.world.chunk.ChunkSection
import io.netty.buffer.ByteBufAllocator
import org.iq80.leveldb.CompressionType
import org.iq80.leveldb.Options
import org.iq80.leveldb.impl.Iq80DBFactory
import java.io.File

class LevelDBWorld(val folder: File) {

    private val db = Iq80DBFactory.factory.open(folder, Options().apply {
        createIfMissing(true)
        compressionType(CompressionType.SNAPPY)
    })

    fun close() = db.close()

    fun saveChunkVersion(x: Int, z: Int, dimension: Int, version: Byte = CHUNK_VERSION) {
        val key = LevelDBChunkKey.Key.VERSION.getKey(x, z, dimension)
        db.put(key, byteArrayOf(version))
    }

    fun saveSubChunk(x: Int, z: Int, dimension: Int, y: Int, subChunk: ChunkSection, useRuntime: Boolean) {
        val key = LevelDBChunkKey.Key.SUB_CHUNK_DATA.getKey(x, z, dimension, y)
        val buf = ByteBufAllocator.DEFAULT.ioBuffer()

        try {
            subChunk.write(buf, useRuntime, false)
            val data = ByteArray(buf.readableBytes())
            buf.readBytes(data)
            db.put(key, data)
        } finally {
            buf.release()
        }
    }

    fun saveChunk(chunk: Chunk) {
        saveChunkVersion(chunk.x, chunk.z, chunk.dimension)
        val yOffset = if (chunk.is384World) -4 else 0
        chunk.sectionStorage.forEachIndexed { i, section ->
            saveSubChunk(chunk.x, chunk.z, chunk.dimension, i + yOffset, section, useRuntime = false)
        }
    }

    companion object {
        private const val CHUNK_VERSION = 0x28.toByte()
    }
}
