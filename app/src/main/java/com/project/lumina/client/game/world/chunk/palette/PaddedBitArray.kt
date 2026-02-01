package com.project.lumina.client.game.world.chunk.palette

class PaddedBitArray(
    private val version: BitArrayVersion,
    private val size: Int,
    private val words: IntArray
) : BitArray {

    override fun set(index: Int, value: Int) {
        require(index in 0 until size)
        require(value in 0..version.maxEntryValue)

        val wordIndex = index / version.entriesPerWord
        val offset = (index % version.entriesPerWord) * version.bits

        words[wordIndex] =
            (words[wordIndex] and (version.maxEntryValue shl offset).inv()) or
                    ((value and version.maxEntryValue) shl offset)
    }

    override fun get(index: Int): Int {
        require(index in 0 until size)

        val wordIndex = index / version.entriesPerWord
        val offset = (index % version.entriesPerWord) * version.bits

        return (words[wordIndex] ushr offset) and version.maxEntryValue
    }

    override fun size() = size
    override fun getWords(): IntArray = words
    override fun getVersion(): BitArrayVersion = version
    override fun copy(): BitArray = PaddedBitArray(version, size, words.copyOf())
}
