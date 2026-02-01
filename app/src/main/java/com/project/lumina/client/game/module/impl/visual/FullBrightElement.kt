package com.project.lumina.client.game.module.impl.visual

import com.project.lumina.client.R
import com.project.lumina.client.game.InterceptablePacket
import com.project.lumina.client.constructors.Element
import com.project.lumina.client.constructors.CheatCategory
import org.cloudburstmc.protocol.bedrock.packet.MobEffectPacket
import com.project.lumina.client.util.AssetManager

class FullBrightElement(iconResId: Int = AssetManager.getAsset("video_camera_alt_18")) : Element(
    name = "FullBright",
    category = CheatCategory.Visual,
    iconResId,
    displayNameResId = AssetManager.getString("module_fullbright_display_name")
) {

    private fun sendApplyPacket() {
        val packet = MobEffectPacket().apply {
            runtimeEntityId = session.localPlayer.runtimeEntityId
            event = MobEffectPacket.Event.ADD
            effectId = 16
            amplifier = 0
            isParticles = false
            duration = 1000000
        }
        session.clientBound(packet)
    }

    private fun sendRemovePacket() {
        val packet = MobEffectPacket().apply {
            runtimeEntityId = session.localPlayer.runtimeEntityId
            event = MobEffectPacket.Event.REMOVE
            effectId = 16
        }
        session.clientBound(packet)
    }

    override fun onEnabled() {
        super.onEnabled()
        try {
            if (session.localPlayer != null) {
                sendApplyPacket()
            } else {
                println("Local player not initialized, cannot enable FullBright.")
            }
        } catch (e: Exception) {
            println("Error enabling FullBright: ${e.message}")
        }
    }

    override fun onDisabled() {
        super.onDisabled()
        if(isSessionCreated) {
            if (session.localPlayer != null) {
                sendRemovePacket()
            }
        }
    }

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) return

        val packet = interceptablePacket.packet
        if (packet is MobEffectPacket &&
            packet.runtimeEntityId == session.localPlayer.runtimeEntityId &&
            packet.event == MobEffectPacket.Event.REMOVE &&
            packet.effectId == 16
        ) {
            interceptablePacket.intercept()
        }
    }
}