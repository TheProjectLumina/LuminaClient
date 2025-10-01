package com.project.lumina.client.game.module.impl.effect

import com.project.lumina.client.constructors.CheatCategory
import com.project.lumina.client.constructors.Element
import com.project.lumina.client.game.InterceptablePacket
import com.project.lumina.client.game.module.api.setting.EffectSetting
import org.cloudburstmc.protocol.bedrock.data.entity.EntityDataTypes
import org.cloudburstmc.protocol.bedrock.packet.MobEffectPacket
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket

abstract class BaseEffectElement(
    name: String,
    displayNameResId: Int,
    private val effectId: Int? = null,
    private val effectSetting: Number? = null
) : Element(
    name = name,
    category = CheatCategory.World,
    displayNameResId = displayNameResId
) {
    protected val amplifierValue by floatValue("amplifier", 1f, 1f..5f)
    protected val effect by EffectSetting(this, EntityDataTypes.VISIBLE_MOB_EFFECTS, effectSetting!!.toInt())

    override fun onDisabled() {
        super.onDisabled()
        if (isSessionCreated && effectId != null) {
            removeEffect()
        }
    }

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) return

        val packet = interceptablePacket.packet
        if (packet is PlayerAuthInputPacket && session.localPlayer.tickExists % 20 == 0L && effectId != null) {
            addEffect()
        }
    }

    protected fun addEffect(customEffectId: Int? = null) {
        session.clientBound(MobEffectPacket().apply {
            runtimeEntityId = session.localPlayer.runtimeEntityId
            event = MobEffectPacket.Event.ADD
            this.effectId = customEffectId ?: effectId
            amplifier = amplifierValue.toInt() - 1
            isParticles = false
            duration = 360000
        })
    }

    protected fun removeEffect(customEffectId: Int? = null) {
        session.clientBound(MobEffectPacket().apply {
            runtimeEntityId = session.localPlayer.runtimeEntityId
            event = MobEffectPacket.Event.REMOVE
            this.effectId = customEffectId ?: effectId
        })
    }
} 