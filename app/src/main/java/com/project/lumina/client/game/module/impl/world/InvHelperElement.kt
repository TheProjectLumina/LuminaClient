package com.project.lumina.client.game.module.impl.world

import com.project.lumina.client.R
import com.project.lumina.client.constructors.CheatCategory
import com.project.lumina.client.constructors.Element
import com.project.lumina.client.game.InterceptablePacket
import com.project.lumina.client.game.inventory.PlayerInventory
import com.project.lumina.client.game.registry.itemDefinition
import com.project.lumina.client.game.registry.isBlock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.cloudburstmc.protocol.bedrock.data.inventory.ItemData
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket

class InvHelperElement : Element(
    name = "InvHelper",
    category = CheatCategory.World,
    displayNameResId = R.string.module_invhelper_display_name
) {

    private val delayMs by intValue("Delay", 100, 1..1000)
    private val organizeTools by boolValue("Organize Tools", true)
    private val organizeBlocks by boolValue("Organize Blocks", true)
    private val dropUseless by boolValue("Drop Useless Items", true)
    
    private var lastOrganizeTime = 0L
    private var isOrganizing = false
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    private val blacklistedItems = setOf(
        "minecraft:dirt",
        "minecraft:cobblestone_wall",
        "minecraft:diorite",
        "minecraft:andesite",
        "minecraft:granite",
        "minecraft:gravel",
        "minecraft:sand",
        "minecraft:rotten_flesh",
        "minecraft:poisonous_potato",
        "minecraft:spider_eye",
        "minecraft:bone",
        "minecraft:string",
        "minecraft:leather",
        "minecraft:feather",
        "minecraft:egg",
        "minecraft:snowball",
        "minecraft:glass_bottle",
        "minecraft:bowl"
    )

    private val toolPriority = listOf(
        "sword",
        "pickaxe",
        "axe",
        "shovel"
    )

    private val toolMaterials = listOf(
        "netherite",
        "diamond",
        "iron",
        "golden",
        "stone",
        "wooden"
    )

    private val blockPriority = listOf(
        "planks",
        "cobblestone",
        "stone",
        "wood",
        "log",
        "obsidian",
        "netherrack",
        "end_stone"
    )

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) return

        val packet = interceptablePacket.packet
        if (packet !is PlayerAuthInputPacket) return

        if (ChestStealerElement.isActivelyStealingFromChest) return

        val currentTime = System.currentTimeMillis()
        if (currentTime - lastOrganizeTime < 3000 || isOrganizing) return

        if (!isSessionCreated) return

        coroutineScope.launch {
            try {
                isOrganizing = true
                organizeInventory()
                lastOrganizeTime = System.currentTimeMillis()
            } finally {
                isOrganizing = false
            }
        }
    }

    private suspend fun organizeInventory() {
        val inventory = session.localPlayer.inventory

        if (dropUseless) {
            dropBlacklistedItems(inventory)
        }

        if (organizeTools) {
            organizeBestTools(inventory)
        }

        if (organizeBlocks) {
            organizeBestBlocks(inventory)
        }
    }

    private suspend fun dropBlacklistedItems(inventory: PlayerInventory) {
        for (i in 0 until 36) {
            val item = inventory.content[i]
            if (item == ItemData.AIR) continue

            val identifier = item.definition?.identifier ?: continue
            
            if (blacklistedItems.contains(identifier)) {
                try {
                    inventory.dropItem(i, session)
                    delay(delayMs.toLong())
                } catch (e: Exception) {
                    delay(delayMs.toLong())
                }
            }
        }
    }

    private suspend fun organizeBestTools(inventory: PlayerInventory) {
        val hotbarSlots = 0 until 9
        val inventorySlots = 9 until 36

        val toolsByType = mutableMapOf<String, MutableList<Pair<Int, ItemData>>>()
        
        for (i in inventorySlots) {
            val item = inventory.content[i]
            if (item == ItemData.AIR) continue

            val identifier = item.definition?.identifier?.lowercase() ?: continue

            for (toolType in toolPriority) {
                if (identifier.contains(toolType)) {
                    toolsByType.getOrPut(toolType) { mutableListOf() }.add(i to item)
                    break
                }
            }
        }

        var hotbarIndex = 0
        for (toolType in toolPriority) {
            val tools = toolsByType[toolType] ?: continue
            
            val bestTool = tools.maxByOrNull { (_, item) ->
                getToolTier(item)
            } ?: continue

            val (sourceSlot, _) = bestTool

            if (hotbarIndex < 9) {
                val targetSlot = hotbarIndex
                
                if (inventory.content[targetSlot] == ItemData.AIR || 
                    !isToolInHotbar(inventory.content[targetSlot])) {
                    
                    if (sourceSlot != targetSlot) {
                        try {
                            inventory.moveItem(sourceSlot, targetSlot, inventory, session)
                            delay(delayMs.toLong())
                            hotbarIndex++
                        } catch (e: Exception) {
                            delay(delayMs.toLong())
                        }
                    } else {
                        hotbarIndex++
                    }
                }
            }
        }
    }

    private suspend fun organizeBestBlocks(inventory: PlayerInventory) {
        val inventorySlots = 9 until 36
        val blocksByType = mutableMapOf<String, MutableList<Pair<Int, ItemData>>>()

        for (i in 0 until 36) {
            val item = inventory.content[i]
            if (item == ItemData.AIR) continue

            val itemDef = item.itemDefinition
            if (!item.isBlock()) continue

            val identifier = item.definition?.identifier ?: continue
            
            for (blockType in blockPriority) {
                if (identifier.contains(blockType)) {
                    blocksByType.getOrPut(blockType) { mutableListOf() }.add(i to item)
                    break
                }
            }
        }

        var targetSlot = 9
        for (blockType in blockPriority) {
            val blocks = blocksByType[blockType] ?: continue
            
            val bestBlock = blocks.maxByOrNull { (_, item) -> item.count } ?: continue
            val (sourceSlot, _) = bestBlock

            if (targetSlot < 36 && sourceSlot >= 9) {
                if (sourceSlot != targetSlot) {
                    val targetItem = inventory.content[targetSlot]
                    
                    if (targetItem == ItemData.AIR || 
                        !isBlockInPriority(targetItem)) {
                        try {
                            inventory.moveItem(sourceSlot, targetSlot, inventory, session)
                            delay(delayMs.toLong())
                            targetSlot++
                        } catch (e: Exception) {
                            delay(delayMs.toLong())
                        }
                    } else {
                        targetSlot++
                    }
                } else {
                    targetSlot++
                }
            }
        }
    }

    private fun getToolTier(item: ItemData): Int {
        val identifier = item.definition?.identifier?.lowercase() ?: return 0
        return when {
            identifier.contains("netherite") -> 6
            identifier.contains("diamond") -> 5
            identifier.contains("iron") -> 4
            identifier.contains("golden") || identifier.contains("gold") -> 3
            identifier.contains("stone") -> 2
            identifier.contains("wooden") || identifier.contains("wood") -> 1
            else -> 0
        }
    }

    private fun isToolInHotbar(item: ItemData): Boolean {
        if (item == ItemData.AIR) return false
        val identifier = item.definition?.identifier?.lowercase() ?: return false
        return toolPriority.any { identifier.contains(it) }
    }

    private fun isBlockInPriority(item: ItemData): Boolean {
        if (item == ItemData.AIR) return false
        if (!item.isBlock()) return false
        
        val identifier = item.definition?.identifier ?: return false
        return blockPriority.any { identifier.contains(it) }
    }

    override fun onDisabled() {
        super.onDisabled()
        isOrganizing = false
    }
}