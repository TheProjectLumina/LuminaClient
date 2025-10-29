package com.project.lumina.client.game.utils.math

import org.cloudburstmc.math.vector.Vector3f
import org.cloudburstmc.math.vector.Vector3i

data class AxisAlignedBB(var minX: Float, var minY: Float, var minZ: Float,
                         var maxX: Float, var maxY: Float, var maxZ: Float) {

    constructor(minX: Int, minY: Int, minZ: Int, maxX: Int, maxY: Int, maxZ: Int) : this(minX.toFloat(), minY.toFloat(), minZ.toFloat(), maxX.toFloat(), maxY.toFloat(), maxZ.toFloat())

    constructor(vec: Vector3f, vec1: Vector3f) : this(vec.x, vec.y, vec.z, vec1.x, vec1.y, vec1.z)

    constructor(vec: Vector3i, vec1: Vector3i) : this(vec.x, vec.y, vec.z, vec1.x, vec1.y, vec1.z)

    fun contract(x: Float, y: Float, z: Float) {
        this.minX += x
        this.minY += y
        this.minZ += z
        this.maxX -= x
        this.maxY -= y
        this.maxZ -= z
    }

    fun expand(x: Float, y: Float, z: Float) {
        this.minX -= x
        this.minY -= y
        this.minZ -= z
        this.maxX += x
        this.maxY += y
        this.maxZ += z
    }

    fun offset(x: Float, y: Float, z: Float) {
        this.minX += x
        this.minY += y
        this.minZ += z
        this.maxX += x
        this.maxY += y
        this.maxZ += z
    }

    fun intersects(minX: Float, minY: Float, minZ: Float, maxX: Float, maxY: Float, maxZ: Float): Boolean {
        return this.minX < maxX && this.maxX > minX && this.minY < maxY &&
                this.maxY > minY && this.minZ < maxZ && this.maxZ > minZ
    }

    fun intersects(other: AxisAlignedBB): Boolean {
        return this.minX < other.maxX && this.maxX > other.minX && this.minY < other.maxY &&
                this.maxY > other.minY && this.minZ < other.maxZ && this.maxZ > other.minZ
    }

    fun intersectsXZ(other: AxisAlignedBB): Boolean {
        return this.minX < other.maxX && this.maxX > other.minX && this.minZ < other.maxZ && this.maxZ > other.minZ
    }

    fun intersectsY(other: AxisAlignedBB): Boolean {
        return this.minY < other.maxY && this.maxY > other.minY
    }
}
