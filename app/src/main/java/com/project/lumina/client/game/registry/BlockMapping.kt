package com.project.lumina.client.game.registry

import android.content.Context
import org.cloudburstmc.nbt.NBTInputStream
import org.cloudburstmc.nbt.NbtList
import org.cloudburstmc.nbt.NbtMap
import org.cloudburstmc.protocol.common.DefinitionRegistry
import java.io.DataInputStream
import java.util.zip.GZIPInputStream

class BlockMapping(
    private val runtimeToGameMap: Map<Int, BlockDefinition>,
    val airId: Int
) : DefinitionRegistry<org.cloudburstmc.protocol.bedrock.data.definitions.BlockDefinition> {

    private val gameToRuntimeMap = mutableMapOf<BlockDefinition, Int>()

    init {
        runtimeToGameMap.forEach { (k, v) -> gameToRuntimeMap[v] = k }
    }

    override fun getDefinition(runtimeId: Int): BlockDefinition {
        return runtimeToGameMap[runtimeId] ?: UnknownBlockDefinition(runtimeId)
    }

    override fun isRegistered(definition: org.cloudburstmc.protocol.bedrock.data.definitions.BlockDefinition): Boolean {
        return definition is UnknownBlockDefinition || getDefinition(definition.runtimeId) == definition
    }

    fun getRuntimeByIdentifier(identifier: String): Int {
        return gameToRuntimeMap.keys.find { it.identifier == identifier }?.runtimeId ?: 0
    }

    fun getRuntimeByDefinition(definition: BlockDefinition): Int {
        return gameToRuntimeMap[definition] ?: 0.also { println("No block found for $definition") }
    }

    companion object {
        fun read(context: Context, version: Short): BlockMapping {
            val path = "mcpedata/blocks/runtime_block_states_$version.dat"
            context.assets.open(path).use { stream ->
                val gzipStream = GZIPInputStream(stream)
                val nbtInput = NBTInputStream(DataInputStream(gzipStream))

                @Suppress("unchecked_cast")
                val tag = nbtInput.readTag() as NbtList<NbtMap>
                val runtimeToBlock = mutableMapOf<Int, BlockDefinition>()
                var airId = 0

                tag.forEach { subtag ->
                    val runtime = subtag.getInt("runtimeId")
                    val name = subtag.getString("name")
                    if (name == "minecraft:air") {
                        airId = runtime
                    }
                    runtimeToBlock[runtime] = BlockDefinition(runtime, name, subtag.getCompound("states") ?: NbtMap.EMPTY)
                }

                return BlockMapping(runtimeToBlock, airId)
            }
        }
    }

    class Provider(context: Context) : MappingProvider<BlockMapping>(context) {

        override val assetPath: String
            get() = "mcpedata/blocks"

        override fun readMapping(version: Short): BlockMapping {
            if (!availableVersions.contains(version)) {
                error("BlockMapping: Version $version not available!")
            }
            return read(context, version)
        }
    }
}
