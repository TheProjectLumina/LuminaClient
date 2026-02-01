package com.project.lumina.client.game.world.chunk.palette

enum class BitArrayVersion(val bits: Int, val entriesPerWord: Int, val next: BitArrayVersion?) {

    V16(16, 2, null),
    V8(8, 4, V16),
    V6(6, 5, V8),
    V5(5, 6, V6),
    V4(4, 8, V5),
    V3(3, 10, V4),
    V2(2, 16, V3),
    V1(1, 32, V2);

    val maxEntryValue: Int = (1 shl bits) - 1

    fun getWordsForSize(size: Int): Int {
        return (size + entriesPerWord - 1) / entriesPerWord
    }

    fun createPalette(size: Int): BitArray {
        val words = IntArray(getWordsForSize(size))
        return createPalette(size, words)
    }

    fun createPalette(size: Int, words: IntArray): BitArray {
        return if (this == V3 || this == V5 || this == V6) {
            PaddedBitArray(this, size, words)
        } else {
            Pow2BitArray(this, size, words)
        }
    }

    companion object {
        fun get(bits: Int, read: Boolean): BitArrayVersion {
            return values().firstOrNull {
                (!read && it.entriesPerWord <= bits) || (read && it.bits == bits)
            } ?: throw IllegalArgumentException("Invalid palette version: $bits")
        }
    }
}
