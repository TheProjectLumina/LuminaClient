package com.project.lumina.client.game.utils.math

import org.cloudburstmc.math.vector.Vector3f
import kotlin.math.cos
import kotlin.math.sin

object MathUtil {
    fun <T: Number> getMovementDirection(yaw: T, pitch: T, speed: T): Vector3f {
        val castedYaw = yaw.toFloat()
        val castedPitch = pitch.toFloat()
        val castedSpeed = speed.toFloat()

        val motionX = -sin(castedYaw) * cos(castedPitch) * castedSpeed
        val motionY = -sin(castedPitch) * castedSpeed
        val motionZ = cos(castedYaw) * cos(castedPitch) * castedSpeed

        return Vector3f.from(motionX, motionY, motionZ)
    }

    fun <T: Number> getMovementDirectionRotDeg(rot: Vector3f, speed: T): Vector3f {
        val yaw = Math.toRadians(rot.y.toDouble())
        val pitch = Math.toRadians(rot.x.toDouble())

        return getMovementDirection(yaw, pitch, speed.toFloat())
    }

    const val JITTER_VAL = 0.7071f
}