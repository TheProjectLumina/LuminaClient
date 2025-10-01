package com.project.lumina.client.game.module.impl.motion

import com.project.lumina.client.R
import com.project.lumina.client.constructors.CheatCategory
import com.project.lumina.client.constructors.Element
import com.project.lumina.client.game.InterceptablePacket
import com.project.lumina.client.util.AssetManager
import org.cloudburstmc.math.vector.Vector3f
import org.cloudburstmc.protocol.bedrock.data.PlayerAuthInputData
import org.cloudburstmc.protocol.bedrock.data.PlayerPermission
import org.cloudburstmc.protocol.bedrock.data.command.CommandPermission
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket
import org.cloudburstmc.protocol.bedrock.packet.SetEntityMotionPacket
import org.cloudburstmc.protocol.bedrock.packet.UpdateAbilitiesPacket
import kotlin.math.cos
import kotlin.math.sin

class JitterFlyElement(iconResId: Int = AssetManager.getAsset("ic_menu_arrow_up_black_24dp")) : Element(
    name = "Jitterfly",
    category = CheatCategory.Motion,
    iconResId,
    displayNameResId = AssetManager.getString("module_jitterfly_display_name")
) {

    private val horizontalSpeed by floatValue("Horizontal Speed", 3.5f, 0.5f..10.0f)
    private val verticalSpeed by floatValue("Vertical Speed", 1.5f, 0.5f..5.0f)
    private val jitterPower by floatValue("Jitter", 0.05f, 0.01f..0.3f)
    private val motionInterval by floatValue("Delay", 50f, 10f..100f)

    private var lastMotionTime = 0L
    private var jitterState = false
    private var flyEnabled = false

    private val flyPacket = UpdateAbilitiesPacket().apply {
        playerPermission = PlayerPermission.OPERATOR
        commandPermission = CommandPermission.OWNER
        uniqueEntityId = -1
    }

    private val resetPacket = UpdateAbilitiesPacket().apply {
        playerPermission = PlayerPermission.VISITOR
        commandPermission = CommandPermission.ANY
        uniqueEntityId = -1
    }

    private fun applyFlyAbilities(enabled: Boolean) {
        if (flyEnabled != enabled) {
            val id = session.localPlayer.uniqueEntityId
            flyPacket.uniqueEntityId = id
            resetPacket.uniqueEntityId = id
            session.clientBound(if (enabled) flyPacket else resetPacket)
            flyEnabled = enabled
        }
    }

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        val packet = interceptablePacket.packet
        if (packet is PlayerAuthInputPacket && isEnabled) {
            applyFlyAbilities(true)

            val now = System.currentTimeMillis()
            if (now - lastMotionTime < motionInterval) return

            val vertical = when {
                packet.inputData.contains(PlayerAuthInputData.WANT_UP) -> verticalSpeed
                packet.inputData.contains(PlayerAuthInputData.WANT_DOWN) -> -verticalSpeed
                else -> if (jitterState) jitterPower else -jitterPower
            }

            val inputX = packet.motion.x
            val inputZ = packet.motion.y

            val yaw = Math.toRadians(packet.rotation.y.toDouble()).toFloat()
            val sinYaw = sin(yaw)
            val cosYaw = cos(yaw)

            val strafe = inputX * horizontalSpeed
            val forward = inputZ * horizontalSpeed

            val motionX = strafe * cosYaw - forward * sinYaw
            val motionZ = forward * cosYaw + strafe * sinYaw

            val motionPacket = SetEntityMotionPacket().apply {
                runtimeEntityId = session.localPlayer.runtimeEntityId
                motion = Vector3f.from(motionX, vertical, motionZ)
            }

            session.clientBound(motionPacket)
            jitterState = !jitterState
            lastMotionTime = now
        }
    }

    override fun onDisabled() {
        applyFlyAbilities(false)
        super.onDisabled()
    }
}