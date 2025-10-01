package com.project.lumina.client.game.module.impl.misc

import com.project.lumina.client.constructors.CheatCategory
import com.project.lumina.client.constructors.Element
import com.project.lumina.client.game.InterceptablePacket
import com.project.lumina.client.overlay.mods.KeystrokesOverlay
import com.project.lumina.client.util.AssetManager
import org.cloudburstmc.protocol.bedrock.data.PlayerAuthInputData.*
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket
import kotlinx.coroutines.*
import kotlin.math.*

class KeyStrokes : Element("KeyStrokes", CheatCategory.Misc, AssetManager.getString("module_keystrokes")) {
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var isJumping = false
    private var lastJump = 0L
    private var currentYaw = 0f
    private var lastPosition = Triple(0.0, 0.0, 0.0)
    private var lastTime = 0L
    private val motionThreshold = 0.02



    private var dragX by intValue("Position X", 90, -100..100)
    private var dragY by intValue("Position Y", 90, -100..100)


    private var lastDragX = dragX
    private var lastDragY = dragY

    override fun onEnabled() {
        if(isSessionCreated) {
            KeystrokesOverlay.setOverlayEnabled(true)
        }
    }

    override fun onDisabled() {
        if(isSessionCreated) {
            KeystrokesOverlay.setOverlayEnabled(false)
            scope.launch {
                arrayOf("W", "A", "S", "D", "Space").forEach {
                    session.keyPress(
                        it,
                        false
                    )
                }
            }
            isJumping = false
        }
    }



    override fun beforePacketBound(p: InterceptablePacket) {
        if (!isEnabled) return

        when (val packet = p.packet) {
            is PlayerAuthInputPacket -> {

                val d = packet.inputData
                val t = System.currentTimeMillis()
                val currentPosition = packet.position

                currentYaw = packet.rotation.y

                if (d.contains(START_JUMPING)) {
                    isJumping = true
                    lastJump = t
                } else if (isJumping && t - lastJump > 500) {
                    isJumping = false
                }

                if (lastTime > 0) {
                    val deltaTime = (t - lastTime) / 1000.0

                    val motionX = (currentPosition.x - lastPosition.first) / deltaTime
                    val motionZ = (currentPosition.z - lastPosition.third) / deltaTime

                    val motionMagnitude = sqrt(motionX * motionX + motionZ * motionZ)
                    val keys = if (motionMagnitude > motionThreshold && deltaTime > 0) {
                        calculateMovementKeys(motionX, motionZ, currentYaw)
                    } else {
                        MovementKeys()
                    }

                    scope.launch {
                        session.keyPress("W", keys.w)
                        session.keyPress("A", keys.a)
                        session.keyPress("S", keys.s)
                        session.keyPress("D", keys.d)
                        session.keyPress("Space", isJumping)
                    }
                }

                lastPosition = Triple(currentPosition.x.toDouble(), currentPosition.y.toDouble(), currentPosition.z.toDouble())
                lastTime = t
            }
        }
    }

    private fun calculateMovementKeys(motionX: Double, motionZ: Double, yaw: Float): MovementKeys {
        val yawRad = Math.toRadians(yaw.toDouble())
        val motionAngle = atan2(-motionX, motionZ)
        var relativeAngle = motionAngle - yawRad
        while (relativeAngle > PI) relativeAngle -= 2 * PI
        while (relativeAngle < -PI) relativeAngle += 2 * PI
        val relativeDegrees = Math.toDegrees(relativeAngle)
        return when {
            relativeDegrees >= -45 && relativeDegrees <= 45 -> MovementKeys(w = true)
            relativeDegrees > 45 && relativeDegrees < 135 -> MovementKeys(d = true)
            abs(relativeDegrees) >= 135 -> MovementKeys(s = true)
            relativeDegrees > -135 && relativeDegrees < -45 -> MovementKeys(a = true)
            else -> MovementKeys()
        }
    }

    private data class MovementKeys(
        val w: Boolean = false,
        val a: Boolean = false,
        val s: Boolean = false,
        val d: Boolean = false
    )
}