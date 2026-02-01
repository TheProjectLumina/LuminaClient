package com.project.lumina.client.game.module.impl.motion


import com.project.lumina.client.R
import com.project.lumina.client.game.InterceptablePacket
import com.project.lumina.client.constructors.Element
import com.project.lumina.client.constructors.CheatCategory
import com.project.lumina.client.util.AssetManager
import org.cloudburstmc.math.vector.Vector3f
import org.cloudburstmc.protocol.bedrock.data.PlayerAuthInputData
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket
import org.cloudburstmc.protocol.bedrock.packet.SetEntityMotionPacket



class SpeedElement(iconResId: Int = AssetManager.getAsset("ic_run_black_24dp")) : Element(
    name = "Speed",
    category = CheatCategory.Motion,
    iconResId,
    displayNameResId = AssetManager.getString("module_speed_display_name")
) {

    private var speedMultiplier by floatValue("Speed", 1.5f, 1.1f..3.0f)


    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) return

        val packet = interceptablePacket.packet

        if (packet is PlayerAuthInputPacket) {
            if (packet.motion.length() > 0.0 && packet.inputData.contains(PlayerAuthInputData.VERTICAL_COLLISION)) {
                val motionPacket = SetEntityMotionPacket().apply {
                    runtimeEntityId = session.localPlayer.runtimeEntityId
                    motion = Vector3f.from(
                        session.localPlayer.motionX.toDouble() * speedMultiplier,
                        session.localPlayer.motionY.toDouble(),
                        session.localPlayer.motionZ.toDouble() * speedMultiplier
                    )
                }

                session.clientBound(motionPacket)
            }
        }
    }
}