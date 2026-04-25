package com.project.lumina.client.game.utils.math

import org.cloudburstmc.math.vector.Vector3f
import org.cloudburstmc.math.vector.Vector3i

/**
 * Represents an axis-aligned bounding box in 3D space.
 * @property minX the minimum X coordinate
 * @property minY the minimum Y coordinate
 * @property minZ the minimum Z coordinate
 * @property maxX the maximum X coordinate
 * @property maxY the maximum Y coordinate
 * @property maxZ the maximum Z coordinate
 */
data class AxisAlignedBB(
    val minX: Float,
    val minY: Float,
    val minZ: Float,
    val maxX: Float,
    val maxY: Float,
    val maxZ: Float
) {
    constructor(
        minX: Int, minY: Int, minZ: Int,
        maxX: Int, maxY: Int, maxZ: Int
    ) : this(minX.toFloat(), minY.toFloat(), minZ.toFloat(), maxX.toFloat(), maxY.toFloat(), maxZ.toFloat())

    constructor(min: Vector3f, max: Vector3f) : this(min.x, min.y, min.z, max.x, max.y, max.z)
    constructor(min: Vector3i, max: Vector3i) : this(min.x, min.y, min.z, max.x, max.y, max.z)

    /** Returns a new AABB contracted by the given amounts. */
    fun contracted(x: Float, y: Float, z: Float) =
        AxisAlignedBB(minX + x, minY + y, minZ + z, maxX - x, maxY - y, maxZ - z)

    /** Returns a new AABB expanded by the given amounts. */
    fun expanded(x: Float, y: Float, z: Float) =
        AxisAlignedBB(minX - x, minY - y, minZ - z, maxX + x, maxY + y, maxZ + z)

    /** Returns a new AABB offset by the given amounts. */
    fun offset(x: Float, y: Float, z: Float) =
        AxisAlignedBB(minX + x, minY + y, minZ + z, maxX + x, maxY + y, maxZ + z)

    /** Returns true if this box intersects with the specified coordinates. */
    fun intersects(
        minX: Float, minY: Float, minZ: Float,
        maxX: Float, maxY: Float, maxZ: Float
    ): Boolean =
        this.minX < maxX && this.maxX > minX &&
        this.minY < maxY && this.maxY > minY &&
        this.minZ < maxZ && this.maxZ > minZ

    /** Returns true if this box intersects with another box. */
    fun intersects(other: AxisAlignedBB): Boolean =
        intersects(other.minX, other.minY, other.minZ, other.maxX, other.maxY, other.maxZ)

    /** Returns true if this box intersects with another box in the XZ plane. */
    fun intersectsXZ(other: AxisAlignedBB): Boolean =
        this.minX < other.maxX && this.maxX > other.minX &&
        this.minZ < other.maxZ && this.maxZ > other.minZ

    /** Returns true if this box intersects with another box in the Y axis. */
    fun intersectsY(other: AxisAlignedBB): Boolean =
        this.minY < other.maxY && this.maxY > other.minY

    // Optional: Add some useful properties
    val width get() = maxX - minX
    val height get() = maxY - minY
    val depth get() = maxZ - minZ
    val center get() = Vector3f((minX + maxX) / 2f, (minY + maxY) / 2f, (minZ + maxZ) / 2f)
}
