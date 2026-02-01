package com.project.lumina.client.game.world.chunk.palette

interface BitArray {
    fun set(index: Int, value: Int)
    fun get(index: Int): Int
    fun size(): Int
    fun getWords(): IntArray
    fun getVersion(): BitArrayVersion
    fun copy(): BitArray
}
