package com.project.lumina.client.game.module.impl.motion

import com.project.lumina.client.R
import com.project.lumina.client.game.InterceptablePacket
import com.project.lumina.client.constructors.Element
import com.project.lumina.client.constructors.CheatCategory
import com.project.lumina.client.game.utils.math.MathUtil
import com.project.lumina.client.util.AssetManager
import org.cloudburstmc.math.vector.Vector3f
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket
import org.cloudburstmc.protocol.bedrock.packet.SetEntityMotionPacket
import kotlin.math.cos
import kotlin.math.sin

class JetPackElement(iconResId: Int = AssetManager.getAsset("ic_ethereum_black_24dp")) : Element(
    name = "Jetpack",
    category = CheatCategory.Motion,
    iconResId,
    displayNameResId = AssetManager.getString("module_jet_pack_display_name")
) {
    private var speed by floatValue("Speed", 0.5f, 0.1f..1.5f)

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) {
            return
        }

        val packet = interceptablePacket.packet

        if (packet is PlayerAuthInputPacket) {
            val motionPacket = SetEntityMotionPacket().apply {
                runtimeEntityId = session.localPlayer.runtimeEntityId
                motion = MathUtil.getMovementDirectionRotDeg(packet.rotation, speed)
            }
            session.clientBound(motionPacket)
        }
    }
}