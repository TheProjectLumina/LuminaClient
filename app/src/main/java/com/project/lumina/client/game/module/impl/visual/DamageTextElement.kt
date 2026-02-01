package com.project.lumina.client.game.module.impl.visual

import com.project.lumina.client.R
import com.project.lumina.client.constructors.CheatCategory
import com.project.lumina.client.constructors.Element
import com.project.lumina.client.game.InterceptablePacket
import com.project.lumina.client.game.entity.Player
import com.project.lumina.client.util.AssetManager
import org.cloudburstmc.protocol.bedrock.data.entity.EntityEventType
import org.cloudburstmc.protocol.bedrock.packet.EntityEventPacket

class DamageTextElement(
    iconResId: Int = ir.alirezaivaz.tablericons.R.drawable.ic_text_plus
) : Element(
    name = "DamageText",
    category = CheatCategory.Visual,
    iconResId,
    displayNameResId = AssetManager.getString("module_damage_text_display_name")
) {

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) return

        val packet = interceptablePacket.packet
        if (packet is EntityEventPacket && packet.type == EntityEventType.HURT) {
            val entityId = packet.runtimeEntityId

            if (entityId == session.localPlayer.runtimeEntityId) return

            val entity = session.level.entityMap[entityId]
            if (entity is Player) {
                val name = entity.username
                val status = "§f$name§r §cEnemy Damaged"
                session.displayClientMessage(" $status")
            }
        }
    }
}