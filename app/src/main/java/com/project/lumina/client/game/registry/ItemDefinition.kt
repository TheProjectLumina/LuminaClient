package com.project.lumina.client.game.registry

import com.project.lumina.client.game.inventory.AbstractInventory
import com.project.lumina.client.game.utils.constants.ItemTags
import org.cloudburstmc.protocol.bedrock.data.inventory.ItemData

open class ItemDefinition(private val runtimeId: Int, private val identifier: String, val tags: Array<String>) :
    org.cloudburstmc.protocol.bedrock.data.definitions.ItemDefinition {

    override fun getRuntimeId() = runtimeId

    override fun getIdentifier() = identifier

    override fun isComponentBased() = false

    fun isNecessaryItem(item: ItemData): Boolean {
        if (item == ItemData.AIR) return false
        if (item.isBlock()) return true

        val itemTags = tags
        return itemTags.contains(ItemTags.TAG_IS_HELMET)
                || itemTags.contains(ItemTags.TAG_IS_CHESTPLATE)
                || itemTags.contains(ItemTags.TAG_IS_LEGGINGS)
                || itemTags.contains(ItemTags.TAG_IS_BOOTS)
                || itemTags.contains(ItemTags.TAG_IS_SWORD)
                || itemTags.contains(ItemTags.TAG_IS_PICKAXE)
                || itemTags.contains(ItemTags.TAG_IS_AXE)
                || itemTags.contains(ItemTags.TAG_IS_HOE)
                || itemTags.contains(ItemTags.TAG_IS_SHOVEL)
                || itemTags.contains(ItemTags.TAG_IS_FOOD)
                || identifier == "minecraft:shield"
				|| identifier == "minecraft:flint_and_steel"
				|| identifier == "minecraft:totem_of_undying"
				|| identifier == "minecraft:end_crystal"
    }

    fun hasBetterItem(container: AbstractInventory, excludeSlot: Int = -1, strictMode: Boolean = true): Boolean {
        if (this is UnknownItemDefinition || identifier == "minecraft:air") return false

        var categoryTag: String
        if (tags.contains(ItemTags.TAG_IS_HELMET).also { categoryTag = ItemTags.TAG_IS_HELMET }
            || tags.contains(ItemTags.TAG_IS_CHESTPLATE).also { categoryTag = ItemTags.TAG_IS_CHESTPLATE }
            || tags.contains(ItemTags.TAG_IS_LEGGINGS).also { categoryTag = ItemTags.TAG_IS_LEGGINGS }
            || tags.contains(ItemTags.TAG_IS_BOOTS).also { categoryTag = ItemTags.TAG_IS_BOOTS }
            || tags.contains(ItemTags.TAG_IS_SWORD).also { categoryTag = ItemTags.TAG_IS_SWORD }
            || tags.contains(ItemTags.TAG_IS_PICKAXE).also { categoryTag = ItemTags.TAG_IS_PICKAXE }
            || tags.contains(ItemTags.TAG_IS_AXE).also { categoryTag = ItemTags.TAG_IS_AXE }
            || tags.contains(ItemTags.TAG_IS_HOE).also { categoryTag = ItemTags.TAG_IS_HOE }
            || tags.contains(ItemTags.TAG_IS_SHOVEL).also { categoryTag = ItemTags.TAG_IS_SHOVEL }) {
            
            val itemTier = getTier()
            container.searchForItemIndexed { i, item ->
                if (i == excludeSlot) return@searchForItemIndexed false

                val alterDefinition = item.itemDefinition
                return@searchForItemIndexed alterDefinition.tags.contains(categoryTag) &&
                        (if (strictMode) alterDefinition.getTier() >= itemTier else alterDefinition.getTier() > itemTier)
            }?.also { return true }
            
        }
        return false
    }

    fun getTier(): Int {
        return when {
            tags.contains(ItemTags.TAG_LEATHER_TIER) -> 1
            tags.contains(ItemTags.TAG_WOODEN_TIER) -> 1
            tags.contains(ItemTags.TAG_GOLDEN_TIER) -> 2
            tags.contains(ItemTags.TAG_STONE_TIER) -> 3
            tags.contains(ItemTags.TAG_CHAINMAIL_TIER) -> 3
            tags.contains(ItemTags.TAG_IRON_TIER) -> 4
            tags.contains(ItemTags.TAG_DIAMOND_TIER) -> 5
            tags.contains(ItemTags.TAG_NETHERITE_TIER) -> 6
            else -> 0
        }
    }

	override fun toString(): String {
		return identifier
	}
}

class UnknownItemDefinition(runtimeId: Int): ItemDefinition(runtimeId, "minecraft:unknown", emptyArray())

fun ItemData.isBlock(): Boolean {
    return (blockDefinition?.runtimeId ?: 0) != 0
}

private val airDefinition = ItemDefinition(0, "minecraft:air", emptyArray())

val ItemData.itemDefinition: ItemDefinition
    get() = this.definition as? ItemDefinition ?: airDefinition