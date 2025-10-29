package com.project.lumina.client.game.registry

import org.cloudburstmc.nbt.NbtMap

open class BlockDefinition(private val runtimeId: Int, val identifier: String, val states: NbtMap):
    org.cloudburstmc.protocol.bedrock.data.definitions.BlockDefinition {

    override fun getRuntimeId() = runtimeId

	override fun toString(): String {
		return identifier
	}
}

class UnknownBlockDefinition(runtimeId: Int): BlockDefinition(runtimeId, "minecraft:unknown", NbtMap.EMPTY)