package com.project.lumina.client.game.module.impl.motion

import com.project.lumina.client.R
import com.project.lumina.client.constructors.CheatCategory
import com.project.lumina.client.constructors.Element
import com.project.lumina.client.game.InterceptablePacket
import com.project.lumina.client.util.AssetManager
import org.cloudburstmc.math.vector.Vector3f
import org.cloudburstmc.protocol.bedrock.data.PlayerAuthInputData
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket
import org.cloudburstmc.protocol.bedrock.packet.SetEntityMotionPacket
import kotlin.math.cos
import kotlin.math.sin

class AntiACFly(iconResId: Int = AssetManager.getAsset("ic_menu_arrow_up_black_24dp")) : Element(
    name = "AntiACFly",
    category = CheatCategory.Motion,
    iconResId,
    displayNameResId = AssetManager.getString("module_Anti_Ac_fly_display_name")
) {
    private val glideSpeed by floatValue("Glide Speed", 0.0f, -2.0f..1.0f)
    private val verticalSpeedUp by floatValue("Speed Up", 0.2f, 0.1f..1.0f)
    private val verticalSpeedDown by floatValue(
        "Speed Down",
        0.2f,
        0.1f..1.0f
    )
    private val horizontalSpeed by floatValue(
       "HorizontalSpeed",
        0.5f,
        0.1f..2.0f
    )
    private val motionInterval by floatValue(
        "Interval",
        100.0f,
        100.0f..600.0f
    )
    private var lastMotionTime = 0L
    private var jitterState = false
    private var effectiveGlideSpeed: Float = 0.0f

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) return

        val packet = interceptablePacket.packet

        if (packet is PlayerAuthInputPacket && System.currentTimeMillis() - lastMotionTime >= motionInterval) {

            effectiveGlideSpeed = glideSpeed


            if (packet.inputData.contains(PlayerAuthInputData.WANT_UP)) {
                effectiveGlideSpeed += verticalSpeedUp
            }
            if (packet.inputData.contains(PlayerAuthInputData.WANT_DOWN)) {
                effectiveGlideSpeed -= verticalSpeedDown
            }


            effectiveGlideSpeed += if (jitterState) 0.1f else -0.1f


            val yawRadians = Math.toRadians(packet.rotation.y.toDouble())


            val inputX = packet.motion.x
            val inputZ = packet.motion.y
            val motionX =
                ((-sin(yawRadians) * inputZ + cos(yawRadians) * inputX) * horizontalSpeed).toFloat()
            val motionZ =
                ((cos(yawRadians) * inputZ + sin(yawRadians) * inputX) * horizontalSpeed).toFloat()
            val glidePacket = SetEntityMotionPacket().apply {
                runtimeEntityId = session.localPlayer.runtimeEntityId
                motion = Vector3f.from(motionX, effectiveGlideSpeed, motionZ)
            }
            session.clientBound(glidePacket)
            jitterState = !jitterState
            lastMotionTime = System.currentTimeMillis()
        }
    }
}






