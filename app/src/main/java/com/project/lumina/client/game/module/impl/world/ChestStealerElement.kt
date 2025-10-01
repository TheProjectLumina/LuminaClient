package com.project.lumina.client.game.module.impl.world
import android.util.Log
import com.project.lumina.client.R
import com.project.lumina.client.constructors.CheatCategory
import com.project.lumina.client.constructors.Element
import com.project.lumina.client.game.InterceptablePacket
import com.project.lumina.client.game.inventory.ContainerInventory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.cloudburstmc.protocol.bedrock.data.inventory.ContainerType
import org.cloudburstmc.protocol.bedrock.data.inventory.ItemData
import org.cloudburstmc.protocol.bedrock.packet.ContainerClosePacket
import org.cloudburstmc.protocol.bedrock.packet.ContainerOpenPacket
import org.cloudburstmc.protocol.bedrock.packet.InventoryContentPacket
import org.cloudburstmc.protocol.bedrock.packet.NetworkStackLatencyPacket
import java.util.ArrayDeque
class ChestStealerElement : Element(
    name = "ChestStealer",
    category = CheatCategory.World,
    displayNameResId = R.string.module_chest_stealer_display_name
) {
    companion object {
        private const val TAG = "ChestStealer"
    }
    private val autoClose by boolValue("Auto Close", true)
    private val useDelay by boolValue("Use Delay", false)
    private var currentContainer: ContainerInventory? = null
    private var isStealingInProgress = false
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    private var currentLatency = 100L
    private val latencyHistory = ArrayDeque<Long>(10)
    private var lastLatencyUpdate = 0L
    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) return
        val packet = interceptablePacket.packet
        when (packet) {
            is NetworkStackLatencyPacket -> {
                if (packet.fromServer) {
                    handleLatencyPacket(packet)
                }
            }
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
    private fun handleLatencyPacket(packet: NetworkStackLatencyPacket) {
        if (!useDelay) return
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastLatencyUpdate > 1000) {
            val latency = (currentTime - (packet.timestamp / 1_000_000)).coerceAtLeast(1L)
            latencyHistory.offer(latency)
            if (latencyHistory.size > 10) {
                latencyHistory.poll()
            }
            currentLatency = if (latencyHistory.isEmpty()) {
                100L
            } else {
                latencyHistory.average().toLong()
            }
            lastLatencyUpdate = currentTime
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
        var itemsStolen = 0
        var itemsSkipped = 0
        val validSlots = container.content.indices.filter { slot ->
            val item = container.content[slot]
            item != ItemData.AIR && item.count > 0 && item.isValid
        }
        for (slot in validSlots) {
            if (!isEnabled || (!isStealingInProgress && autoClose)) {
                break
            }
            val item = container.content[slot]
            if (item != ItemData.AIR && item.count > 0 && item.isValid) {
                val emptySlot = playerInventory.findEmptySlot()
                if (emptySlot != null) {
                    try {
                        container.moveItem(slot, emptySlot, playerInventory, session)
                        val containerSlotAfterMove = container.content[slot]
                        val playerSlotAfterMove = playerInventory.content[emptySlot]
                        val moveSuccessful = (containerSlotAfterMove == ItemData.AIR || containerSlotAfterMove.count < container.content[slot].count) &&
                                (playerSlotAfterMove.definition?.identifier == item.definition?.identifier)
                        if (moveSuccessful) {
                            itemsStolen++
                        } else {
                            itemsSkipped++
                        }
                    } catch (e: Exception) {
                        itemsSkipped++
                        continue
                    }
                } else {
                    break
                }
            } else {
                itemsSkipped++
            }
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
    }
    private fun resetLatencyTracking() {
        currentLatency = 100L
        latencyHistory.clear()
        lastLatencyUpdate = 0L
    }
    override fun onDisabled() {
        super.onDisabled()
        cleanup()
        resetLatencyTracking()
    }
}