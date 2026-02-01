package com.project.lumina.client.game.utils.math

import org.cloudburstmc.math.vector.Vector3f
import kotlin.math.atan2
import kotlin.math.hypot
import kotlin.math.sqrt

data class Rotation(var yaw: Float, var pitch: Float)

fun toRotation(from: Vector3f, to: Vector3f): Rotation {
    val diffX = (to.x - from.x).toDouble()
    val diffY = (to.y - from.y).toDouble()
    val diffZ = (to.z - from.z).toDouble()
    return Rotation(
        (Math.toDegrees(atan2(diffZ, diffX)).toFloat() - 90f),
        ((-Math.toDegrees(atan2(diffY, sqrt(diffX * diffX + diffZ * diffZ)))).toFloat())
    )
}

fun getRotationDifference(a: Rotation, b: Rotation) =
    hypot(getAngleDifference(a.yaw, b.yaw), a.pitch - b.pitch)

fun getAngleDifference(a: Float, b: Float) = ((a - b) % 360f + 540f) % 360f - 180f