package com.project.lumina.client.game.inventory

import com.project.lumina.client.constructors.NetBound
import org.cloudburstmc.protocol.bedrock.data.inventory.ContainerId
import org.cloudburstmc.protocol.bedrock.data.inventory.ContainerSlotType
import org.cloudburstmc.protocol.bedrock.data.inventory.FullContainerName
import org.cloudburstmc.protocol.bedrock.data.inventory.ItemData
import org.cloudburstmc.protocol.bedrock.data.inventory.itemstack.request.ItemStackRequest
import org.cloudburstmc.protocol.bedrock.data.inventory.itemstack.request.ItemStackRequestSlotData
import org.cloudburstmc.protocol.bedrock.data.inventory.itemstack.request.action.DropAction
import org.cloudburstmc.protocol.bedrock.data.inventory.itemstack.request.action.PlaceAction
import org.cloudburstmc.protocol.bedrock.data.inventory.itemstack.request.action.SwapAction
import org.cloudburstmc.protocol.bedrock.data.inventory.transaction.InventoryActionData
import org.cloudburstmc.protocol.bedrock.data.inventory.transaction.InventorySource
import org.cloudburstmc.protocol.bedrock.data.inventory.transaction.InventoryTransactionType
import org.cloudburstmc.protocol.bedrock.packet.BedrockPacket
import org.cloudburstmc.protocol.bedrock.packet.InventoryContentPacket
import org.cloudburstmc.protocol.bedrock.packet.InventorySlotPacket
import org.cloudburstmc.protocol.bedrock.packet.InventoryTransactionPacket
import org.cloudburstmc.protocol.bedrock.packet.ItemStackRequestPacket
import com.project.lumina.client.game.event.EventInventorySlotUpdate


abstract class AbstractInventory(val containerId: Int) {

    abstract val content: Array<ItemData>
    open val capacity: Int
        get() = content.size

    /**
     * @return containerId, slotId
     */
    open fun getNetworkSlotInfo(slot: Int): Pair<Int, Int> {
        return containerId to slot
    }

    protected fun notifySlotUpdate(session: NetBound, slot: Int) {
        session.eventManager.emit(EventInventorySlotUpdate(session, this, slot))
    }

    private fun getSlotTypeFromInventoryId(id: Int, slot: Int): ContainerSlotType {
        return when (id) {
            ContainerId.INVENTORY -> {
                if (slot < 9) {
                    ContainerSlotType.HOTBAR
                } else {
                    ContainerSlotType.INVENTORY
                }
            }

            ContainerId.ARMOR -> ContainerSlotType.ARMOR
            ContainerId.OFFHAND -> ContainerSlotType.OFFHAND
            else -> ContainerSlotType.LEVEL_ENTITY
        }
    }

    open fun moveItem(
        sourceSlot: Int,
        destinationSlot: Int,
        destinationInventory: AbstractInventory,
        serverAuthoritative: Int
    ): BedrockPacket {
        val sourceInfo = getNetworkSlotInfo(sourceSlot)
        val destinationInfo = destinationInventory.getNetworkSlotInfo(destinationSlot)
        return if (serverAuthoritative != Int.MAX_VALUE) {
            ItemStackRequestPacket().also {
                val src = content[sourceSlot]
                val dst = destinationInventory.content[destinationSlot]
                if (dst == ItemData.AIR) {
                    it.requests.add(
                        ItemStackRequest(
                            serverAuthoritative,
                            arrayOf(
                                PlaceAction(
                                    src.count,
                                    ItemStackRequestSlotData(
                                        getSlotTypeFromInventoryId(
                                            sourceInfo.first,
                                            sourceSlot
                                        ),
                                        sourceInfo.second,
                                        src.netId,
                                        FullContainerName(
                                            getSlotTypeFromInventoryId(
                                                sourceInfo.first,
                                                sourceSlot
                                            ),
                                            sourceInfo.first
                                        )
                                    ),
                                    ItemStackRequestSlotData(
                                        getSlotTypeFromInventoryId(
                                            destinationInfo.first,
                                            destinationSlot
                                        ),
                                        destinationInfo.second,
                                        dst.netId,
                                        FullContainerName(
                                            getSlotTypeFromInventoryId(
                                                destinationInfo.first,
                                                destinationSlot
                                            ),
                                            destinationInfo.first
                                        )
                                    )
                                )
                            ),
                            arrayOf()
                        )
                    )
                } else {
                    it.requests.add(
                        ItemStackRequest(
                            serverAuthoritative,
                            arrayOf(
                                SwapAction(
                                    ItemStackRequestSlotData(
                                        getSlotTypeFromInventoryId(
                                            sourceInfo.first,
                                            sourceSlot
                                        ),
                                        sourceInfo.second,
                                        src.netId,
                                        FullContainerName(
                                            getSlotTypeFromInventoryId(
                                                sourceInfo.first,
                                                sourceSlot
                                            ),
                                            sourceInfo.first
                                        )
                                    ),
                                    ItemStackRequestSlotData(
                                        getSlotTypeFromInventoryId(
                                            destinationInfo.first,
                                            destinationSlot
                                        ),
                                        destinationInfo.second,
                                        dst.netId,
                                        FullContainerName(
                                            getSlotTypeFromInventoryId(
                                                destinationInfo.first,
                                                destinationSlot
                                            ),
                                            destinationInfo.first
                                        )
                                    )
                                )
                            ),
                            arrayOf()
                        )
                    )
                }
            }
        } else {
            InventoryTransactionPacket().apply {
                transactionType = InventoryTransactionType.NORMAL
                val src = content[sourceSlot]
                val dst = destinationInventory.content[destinationSlot]
                actions.add(
                    InventoryActionData(
                        InventorySource.fromContainerWindowId(sourceInfo.first), sourceInfo.second,
                        src, dst
                    )
                )
                actions.add(
                    InventoryActionData(
                        InventorySource.fromContainerWindowId(destinationInfo.first),
                        destinationInfo.second,
                        dst,
                        src
                    )
                )
            }
        }
    }

    open fun moveItem(
        sourceSlot: Int,
        destinationSlot: Int,
        destinationInventory: AbstractInventory,
        session: NetBound
    ) {
        val sourceItem = content[sourceSlot]
        val destItem = destinationInventory.content[destinationSlot]
        val isServerAuth = session.localPlayer.inventoriesServerAuthoritative
        val requestId = if (isServerAuth) session.localPlayer.inventory.getRequestId() else Int.MAX_VALUE






        val pk = moveItem(sourceSlot, destinationSlot, destinationInventory, requestId)


        if (pk is InventoryTransactionPacket) {


            pk.actions.forEachIndexed { index, action ->
  
            }
        } else if (pk is ItemStackRequestPacket) {
        
            pk.requests.forEachIndexed { index, request ->
  
            }
        }

        sendInventoryPacket(pk, destinationInventory, session)
     

        this.updateItem(session, sourceSlot)
        destinationInventory.updateItem(session, destinationSlot)

        this.notifySlotUpdate(session, sourceSlot)
        destinationInventory.notifySlotUpdate(session, destinationSlot)

  
    }

    protected fun updateItem(session: NetBound, slot: Int) {
        val info = getNetworkSlotInfo(slot)
        if (info.first == ContainerId.OFFHAND) {
            session.clientBound(InventoryContentPacket().also {
                it.containerId = info.first
                it.contents = arrayListOf(content[slot])
            })
        } else {
            session.clientBound(InventorySlotPacket().also {
                it.containerId = info.first
                it.slot = info.second
                it.item = content[slot]
            })
        }
    }

    open fun dropItem(slot: Int, serverAuthoritative: Int): BedrockPacket {
        val info = getNetworkSlotInfo(slot)
        return if (serverAuthoritative != Int.MAX_VALUE) {
            ItemStackRequestPacket().also {
                val item = content[slot]
                it.requests.add(
                    ItemStackRequest(
                        serverAuthoritative,
                        arrayOf(
                            DropAction(
                                item.count,
                                ItemStackRequestSlotData(
                                    getSlotTypeFromInventoryId(info.first, slot),
                                    info.second,
                                    item.netId,
                                    FullContainerName(
                                        getSlotTypeFromInventoryId(info.first, slot),
                                        info.first
                                    )
                                ),
                                false
                            )
                        ),
                        arrayOf(), null
                    )
                )
            }
        } else {
            InventoryTransactionPacket().apply {
                transactionType = InventoryTransactionType.NORMAL
                val item = content[slot]
                actions.add(
                    InventoryActionData(
                        InventorySource.fromWorldInteraction(InventorySource.Flag.DROP_ITEM), 0,
                        ItemData.AIR, item
                    )
                )
                actions.add(
                    InventoryActionData(
                        InventorySource.fromContainerWindowId(info.first), info.second,
                        item, ItemData.AIR
                    )
                )
            }
        }
    }

    open fun dropItem(slot: Int, session: NetBound) {
        
        val pk = dropItem(
            slot,
            if (session.localPlayer.inventoriesServerAuthoritative) session.localPlayer.inventory.getRequestId() else Int.MAX_VALUE
        )
        sendInventoryPacket(pk, null, session)

        
        this.updateItem(session, slot)

        this.notifySlotUpdate(session, slot)
    }

    private fun sendInventoryPacket(
        pk: BedrockPacket,
        destinationInventory: AbstractInventory?,
        session: NetBound
    ) {
        if (pk is ItemStackRequestPacket) {
            if (destinationInventory is PlayerInventory) {
                pk.requests.forEach { request ->
                    destinationInventory.itemStackRequest(request, session)
                }
            } else if (this is PlayerInventory) {
                pk.requests.forEach { request ->
                    this.itemStackRequest(request, session)
                }
            } else {
                session.serverBound(pk)
            }
        } else {
            session.serverBound(pk)
        }
    }

    open fun searchForItem(range: IntRange, condition: (ItemData) -> Boolean): Int? {
        for (i in range) {
            if (condition(content[i])) {
                return i
            }
        }
        return null
    }

    open fun searchForItem(condition: (ItemData) -> Boolean): Int? {
        content.forEachIndexed { i, item ->
            if (condition(item)) {
                return i
            }
        }
        return null
    }

    open fun searchForItemIndexed(condition: (Int, ItemData) -> Boolean): Int? {
        content.forEachIndexed { i, item ->
            if (condition(i, item)) {
                return i
            }
        }
        return null
    }

    open fun findEmptySlot(): Int? {
        content.forEachIndexed { i, item ->
            if (item == ItemData.AIR) {
                return i
            }
        }
        return null
    }

    open fun findBestItem(currentSlot: Int, judge: (ItemData) -> Float): Int? {
        var slot = currentSlot
        var credit = judge(content[currentSlot])
        content.forEachIndexed { i, item ->
            val score = judge(item)
            if (score > credit) {
                credit = score
                slot = i
            }
        }
        return slot
    }
}