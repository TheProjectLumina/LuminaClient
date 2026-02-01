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

class FullStopElement(iconResId: Int = AssetManager.getAsset("ic_arrow_collapse_vertical_black_24dp")) : Element(
    name = "Faststop",
    category = CheatCategory.Motion,
    iconResId,
    displayNameResId = AssetManager.getString("module_faststop_display_name")
) {
    private var lastKeyState: Boolean = false
    private var lastYVelocity: Float = 0.0f

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) return

        val packet = interceptablePacket.packet

        if (packet is PlayerAuthInputPacket) {
            val keyState = packet.inputData.any {
                it in setOf(
                    PlayerAuthInputData.UP,
                    PlayerAuthInputData.DOWN,
                    PlayerAuthInputData.LEFT,
                    PlayerAuthInputData.RIGHT
                )
            }

            if (!keyState && lastKeyState) {
                val stopPacket = SetEntityMotionPacket().apply {
                    runtimeEntityId = session.localPlayer.runtimeEntityId
                    motion = Vector3f.from(
                        0.0f,
                        lastYVelocity,
                        0.0f
                    )
                }

                session.clientBound(stopPacket)
            }

            lastKeyState = keyState
        }
        else if (packet is SetEntityMotionPacket && packet.runtimeEntityId == session.localPlayer.runtimeEntityId) {
            lastYVelocity = packet.motion.y
        }
    }
}