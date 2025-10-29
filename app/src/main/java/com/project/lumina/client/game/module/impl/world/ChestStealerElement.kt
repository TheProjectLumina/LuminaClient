package com.project.lumina.client.game.module.impl.world
import com.project.lumina.client.R
import com.project.lumina.client.constructors.CheatCategory
import com.project.lumina.client.constructors.Element
import com.project.lumina.client.game.InterceptablePacket
import com.project.lumina.client.game.inventory.ContainerInventory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.cloudburstmc.protocol.bedrock.data.inventory.ContainerType
import org.cloudburstmc.protocol.bedrock.data.inventory.ItemData
import org.cloudburstmc.protocol.bedrock.packet.ContainerClosePacket
import org.cloudburstmc.protocol.bedrock.packet.ContainerOpenPacket
import org.cloudburstmc.protocol.bedrock.packet.InventoryContentPacket
class ChestStealerElement : Element(
    name = "ChestStealer",
    category = CheatCategory.World,
    displayNameResId = R.string.module_chest_stealer_display_name
) {
    private val autoClose by boolValue("Auto Close", true)
    private val delayMs by intValue("Delay", 50, 1..1000)
    private var currentContainer: ContainerInventory? = null
    private var isStealingInProgress = false
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    companion object {
        var isActivelyStealingFromChest = false
            private set
    }
    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) return
        val packet = interceptablePacket.packet
        when (packet) {
            is ContainerOpenPacket -> {
                if (isValidChestContainer(packet.type)) {
                    handleChestOpen(packet)
                }
            }
            is InventoryContentPacket -> {
                if (currentContainer != null && packet.containerId == currentContainer!!.containerId) {
                    currentContainer!!.onPacketBound(packet)
                    if (!isStealingInProgress) {
                        startStealing()
                    }
                }
            }
            is ContainerClosePacket -> {
                if (currentContainer != null && packet.id.toInt() == currentContainer!!.containerId) {
                    cleanup()
                }
            }
        }
    }
    private fun isValidChestContainer(type: ContainerType): Boolean {
        return when (type) {
            ContainerType.CONTAINER,
            ContainerType.MINECART_CHEST,
            ContainerType.CHEST_BOAT,
            ContainerType.HOPPER,
            ContainerType.DISPENSER,
            ContainerType.DROPPER -> true
            else -> false
        }
    }
    private fun handleChestOpen(packet: ContainerOpenPacket) {
        currentContainer = ContainerInventory(packet.id.toInt(), packet.type)
        isStealingInProgress = false
        isActivelyStealingFromChest = true
    }
    private fun startStealing() {
        if (isStealingInProgress || currentContainer == null) {
            return
        }
        isStealingInProgress = true
        coroutineScope.launch {
            try {
                stealAllItems()
                if (autoClose) {
                    closeChest()
                } else {
                    isStealingInProgress = false
                }
            } catch (e: Exception) {
                isStealingInProgress = false
                cleanup()
            }
        }
    }
    private suspend fun stealAllItems() {
        val container = currentContainer ?: return
        val playerInventory = session.localPlayer.inventory
        
        while (isEnabled && isStealingInProgress) {
            val itemSlot = findNextItemSlot(container) ?: break
            
            val emptySlot = playerInventory.findEmptySlot()
            if (emptySlot == null) {
                break
            }
            
            try {
                container.moveItem(itemSlot, emptySlot, playerInventory, session)
                delay(delayMs.toLong())
            } catch (e: Exception) {
                delay(delayMs.toLong())
                continue
            }
        }
    }
    
    private fun findNextItemSlot(container: ContainerInventory): Int? {
        return container.content.indices.firstOrNull { slot ->
            val item = container.content[slot]
            item != ItemData.AIR && item.count > 0 && item.isValid
        }
    }
    private suspend fun closeChest() {
        val container = currentContainer ?: return
        try {
            val closePacket = ContainerClosePacket().apply {
                id = container.containerId.toByte()
                isServerInitiated = false
                type = container.type
            }
            session.serverBound(closePacket)
        } finally {
            cleanup()
        }
    }
    private fun cleanup() {
        currentContainer = null
        isStealingInProgress = false
        isActivelyStealingFromChest = false
    }
    
    override fun onDisabled() {
        super.onDisabled()
        cleanup()
    }
}