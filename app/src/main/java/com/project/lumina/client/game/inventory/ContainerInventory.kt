package com.project.lumina.client.game.inventory

import org.cloudburstmc.protocol.bedrock.data.inventory.ContainerType
import org.cloudburstmc.protocol.bedrock.data.inventory.ItemData
import org.cloudburstmc.protocol.bedrock.data.inventory.transaction.InventoryActionData
import org.cloudburstmc.protocol.bedrock.data.inventory.transaction.InventorySource
import org.cloudburstmc.protocol.bedrock.packet.BedrockPacket
import org.cloudburstmc.protocol.bedrock.packet.InventoryContentPacket
import org.cloudburstmc.protocol.bedrock.packet.InventorySlotPacket
import org.cloudburstmc.protocol.bedrock.packet.InventoryTransactionPacket

class ContainerInventory(containerId: Int, val type: ContainerType) :
    AbstractInventory(containerId) {

    override var content = emptyArray<ItemData>()

    fun onPacketBound(packet: BedrockPacket) {
        if (packet is InventoryTransactionPacket) {
            android.util.Log.d("ContainerInventory", "Received InventoryTransactionPacket for container $containerId")
            packet.actions.filter {
                it is InventoryActionData &&
                        it.source.type == InventorySource.Type.CONTAINER &&
                        it.source.containerId == containerId
            }.forEach {
                android.util.Log.d("ContainerInventory", "Transaction action: slot ${it.slot} ${it.fromItem.definition?.identifier ?: "AIR"} -> ${it.toItem.definition?.identifier ?: "AIR"}")
                content[it.slot] = it.toItem
            }
        }
        if (packet is InventoryContentPacket && packet.containerId == containerId) {
            android.util.Log.d("ContainerInventory", "Received InventoryContentPacket for container $containerId with ${packet.contents.size} items")
            content = packet.contents.toTypedArray()
        } else if (packet is InventorySlotPacket && packet.containerId == containerId) {
            android.util.Log.d("ContainerInventory", "Received InventorySlotPacket for container $containerId slot ${packet.slot}: ${packet.item.definition?.identifier ?: "AIR"} x${packet.item.count}")
            content[packet.slot] = packet.item
        }
    }

}