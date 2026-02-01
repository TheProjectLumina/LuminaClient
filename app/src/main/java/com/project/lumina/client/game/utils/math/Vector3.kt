package com.project.lumina.client.game.utils.math

import org.cloudburstmc.math.vector.Vector3f
import org.cloudburstmc.math.vector.Vector3i
import kotlin.math.roundToInt

fun Vector3i.toVector3f(): Vector3f {
    return Vector3f.from(x.toFloat(), y.toFloat(), z.toFloat())
}

fun Vector3f.toVector3i(): Vector3i {
    return Vector3i.from(x.roundToInt(), y.roundToInt(), z.roundToInt())
}

fun Vector3f.toVector3iFloor(): Vector3i {
	return Vector3i.from(floorX, floorY, floorZ)
}

fun Vector3f.distance(x: Int, y: Int, z: Int): Float {
	return distance(x.toFloat(), y.toFloat(), z.toFloat())
}

fun Vector3f.distance(vector3i: Vector3i): Float {
	return distance(vector3i.x, vector3i.y, vector3i.z)
}
