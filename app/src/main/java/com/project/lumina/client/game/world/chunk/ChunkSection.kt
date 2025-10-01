package com.project.lumina.client.game.world.chunk

import android.util.Log
import io.netty.buffer.ByteBuf
import com.project.lumina.client.game.world.chunk.palette.BitArrayVersion
import com.project.lumina.client.game.world.chunk.palette.Pow2BitArray
import org.cloudburstmc.protocol.common.util.VarInts

class ChunkSection {

    var storage = BlockStorage(BitArrayVersion.V2, 0)

    fun read(buf: ByteBuf) {
        val version = buf.readByte().toInt()
        if (version in 1..10) {
            if (version >= 9) buf.readByte()
            val layers = if (version == 1) 1 else buf.readByte().toInt()
            if (layers > 0) {
                try {
                    storage = BlockStorage(buf, true)
                } catch (e: Exception) {
                    Log.e("ChunkSection", "Failed to read primary block storage layer", e)
                    storage = BlockStorage(BitArrayVersion.V2, 0)
                }
            }
            repeat(layers - 1) {
                try {
                    BlockStorage(buf, true)
                } catch (e: Exception) {
                    Log.e("ChunkSection", "Failed to read additional block storage layer", e)
                }
            }
        } else {
            error("Unsupported chunk version: $version")
        }
    }

    fun write(buffer: ByteBuf, useRuntime: Boolean, legacy: Boolean) {
        val subChunkVersion: Byte = 9
        val blockLayerCount: Byte = 1

        buffer.writeByte(subChunkVersion.toInt())
        buffer.writeByte(blockLayerCount.toInt())

        writeStorageLayer(buffer, storage)
    }

    private fun writeStorageLayer(buffer: ByteBuf, storage: BlockStorage) {
        val bitArray = storage.bitArray
        val words = bitArray.getWords()

        
        buffer.writeByte((bitArray.getVersion().bits shl 1) or 1)
        buffer.writeShortLE(words.size)

        for (word in words) {
            buffer.writeIntLE(word)
        }

        
        VarInts.writeInt(buffer, storage.palette.size)
        for (runtimeId in storage.palette) {
            VarInts.writeInt(buffer, runtimeId)
        }
    }


    fun getBlock(x: Int, y: Int, z: Int): Int {
        return storage.getBlock(x, y, z)
    }

    fun setBlock(x: Int, y: Int, z: Int, id: Int) {
        storage.setBlock(x, y, z, id)
    }
}
