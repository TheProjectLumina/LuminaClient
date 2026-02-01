package com.project.lumina.client.game.utils.constants

import com.google.gson.JsonParser
object ItemTags {

    const val TAG_IS_TOOL = "minecraft:is_tool"
    const val TAG_IS_PICKAXE = "minecraft:is_pickaxe"
    const val TAG_NETHERITE_TIER = "minecraft:netherite_tier"
    const val TAG_IS_SHOVEL = "minecraft:is_shovel"
    const val TAG_IS_HOE = "minecraft:is_hoe"
    const val TAG_DIAMOND_TIER = "minecraft:diamond_tier"
    const val TAG_IS_SWORD = "minecraft:is_sword"
    const val TAG_IRON_TIER = "minecraft:iron_tier"
    const val TAG_IS_AXE = "minecraft:is_axe"
    const val TAG_WOODEN_TIER = "minecraft:wooden_tier"
    const val TAG_GOLDEN_TIER = "minecraft:golden_tier"
    const val TAG_STONE_TIER = "minecraft:stone_tier"
    const val TAG_LEATHER_TIER = "minecraft:leather_tier"
    const val TAG_IS_ARMOR = "minecraft:is_armor"
    const val TAG_IS_HELMET = "minecraft:is_helmet"
    const val TAG_IS_CHESTPLATE = "minecraft:is_chestplate"
    const val TAG_IS_LEGGINGS = "minecraft:is_leggings"
    const val TAG_IS_BOOTS = "minecraft:is_boots"
    const val TAG_CHAINMAIL_TIER = "minecraft:chainmail_tier"
    const val TAG_IS_FOOD = "minecraft:is_food"

    private val itemTags = mutableMapOf<String, List<String>>()

    init {
        
        val json = JsonParser.parseReader(ItemTags::class.java.getResourceAsStream("/mcpedata/item_tags.json").reader(Charsets.UTF_8)).asJsonObject
        json.entrySet().forEach { (item, tags) ->
            itemTags[item] = tags.asJsonArray.map { it.asString }
        }
    }

    fun tags(item: String): List<String> {
        return itemTags[item] ?: emptyList()
    }
}
