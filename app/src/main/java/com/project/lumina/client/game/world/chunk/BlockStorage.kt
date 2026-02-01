package com.project.lumina.client.game.world.chunk

import android.util.Log
import com.project.lumina.client.game.world.chunk.palette.BitArray
import com.project.lumina.client.game.world.chunk.palette.BitArrayVersion
import com.project.lumina.client.game.world.chunk.palette.Pow2BitArray
import io.netty.buffer.ByteBuf
import org.cloudburstmc.protocol.common.util.VarInts

class BlockStorage {
    var bitArray: BitArray
    val palette = mutableListOf<Int>()

    constructor(version: BitArrayVersion, airId: Int) {
        bitArray = version.createPalette(MAX_BLOCKS)
        palette.add(airId)
    }

    constructor(buf: ByteBuf, network: Boolean) {
        val paletteHeader = buf.readByte().toInt()
        val paletteBits = paletteHeader shr 1


        if (paletteBits == 0) {
            Log.d("BlockStorage", "Handling single-valued palette (0 bits)")
            bitArray = BitArrayVersion.V1.createPalette(MAX_BLOCKS)

            if (network) {
                val wordCount = VarInts.readInt(buf)
                repeat(wordCount) {
                    buf.readIntLE()
                }
            } else {
                val wordCount = buf.readShortLE().toInt()
                repeat(wordCount) {
                    buf.readIntLE()
                }
            }
        } else {
            val bitArrayVersion = BitArrayVersion.get(paletteBits, true)
            bitArray = bitArrayVersion.createPalette(MAX_BLOCKS)

            if (network) {
                val wordCount = VarInts.readInt(buf)
                val words = IntArray(wordCount)
                repeat(wordCount) { i ->
                    words[i] = buf.readIntLE()
                }
                System.arraycopy(words, 0, bitArray.getWords(), 0, minOf(words.size, bitArray.getWords().size))
            } else {
                val wordCount = buf.readShortLE().toInt()
                repeat(wordCount.coerceAtMost(bitArray.getWords().size)) { i ->
                    bitArray.getWords()[i] = buf.readIntLE()
                }
            }
        }

        fun readInt(): Int {
            return if (network) VarInts.readInt(buf) else buf.readIntLE()
        }

        val paletteSize = readInt()
        repeat(paletteSize) {
            palette.add(readInt())
        }
    }

    fun getBlock(x: Int, y: Int, z: Int): Int {
        val index = (x shl 8) or (z shl 4) or y
        return palette[bitArray.get(index)]
    }

    fun setBlock(x: Int, y: Int, z: Int, id: Int) {
        val index = (x shl 8) or (z shl 4) or y
        val paletteIndex = palette.indexOf(id).takeIf { it != -1 } ?: run {
            palette.add(id)
            palette.lastIndex
        }
        bitArray.set(index, paletteIndex)
    }

    companion object {
        const val MAX_BLOCKS = 4096
    }
}
