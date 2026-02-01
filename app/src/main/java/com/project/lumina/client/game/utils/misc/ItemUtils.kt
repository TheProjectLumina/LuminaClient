package com.project.lumina.client.game.utils.misc

import org.cloudburstmc.nbt.NbtType
import org.cloudburstmc.protocol.bedrock.data.inventory.ItemData

fun ItemData.removeNetInfo(): ItemData {
	return toBuilder()
		.usingNetId(false)
		.netId(0)
		.build()
}

fun ItemData.getEnchant(id: Short): Short? {
	val enchantments = tag?.getList("ench", NbtType.COMPOUND) ?: return null

	enchantments.forEach { enchant ->
		if (enchant.getShort("id") == id) {
			return enchant.getShort("lvl")
		}
	}

	return null
}

fun ItemData.hasEnchant(id: Short): Boolean {
	val enchantments = tag?.getList("ench", NbtType.COMPOUND) ?: return false

	enchantments.forEach { enchant ->
		if (enchant.getShort("id") == id) {
			return true
		}
	}

	return false
}
