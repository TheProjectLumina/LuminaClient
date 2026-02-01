package com.project.lumina.client.game.inventory

import com.project.lumina.client.game.entity.Entity
import org.cloudburstmc.protocol.bedrock.data.inventory.ContainerId
import org.cloudburstmc.protocol.bedrock.data.inventory.ItemData
import org.cloudburstmc.protocol.bedrock.packet.BedrockPacket
import org.cloudburstmc.protocol.bedrock.packet.MobArmorEquipmentPacket
import org.cloudburstmc.protocol.bedrock.packet.MobEquipmentPacket

open class EntityInventory(val entity: Entity) : AbstractInventory(0) {

    override val content = Array(6) { ItemData.AIR }

    open var hand: ItemData
        get() = content[0]
        set(value) {
            content[0] = value
        }
    open var offhand: ItemData
        get() = content[1]
        set(value) {
            content[1] = value
        }

    open var helmet: ItemData
        get() = content[2]
        set(value) {
            content[2] = value
        }
    open var chestplate: ItemData
        get() = content[3]
        set(value) {
            content[3] = value
        }
    open var leggings: ItemData
        get() = content[4]
        set(value) {
            content[4] = value
        }
    open var boots: ItemData
        get() = content[5]
        set(value) {
            content[5] = value
        }

    open fun onPacketBound(packet: BedrockPacket) {
        if (packet is MobEquipmentPacket && packet.runtimeEntityId == entity.runtimeEntityId) {
            if (packet.containerId == 0) {
                hand = packet.item
            } else if (packet.containerId == ContainerId.OFFHAND) {
                offhand = packet.item
            }
        } else if (packet is MobArmorEquipmentPacket && packet.runtimeEntityId == entity.runtimeEntityId) {
            helmet = packet.helmet
            chestplate = packet.chestplate
            leggings = packet.leggings
            boots = packet.boots
        }
    }
}