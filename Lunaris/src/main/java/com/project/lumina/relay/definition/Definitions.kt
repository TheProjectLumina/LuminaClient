package com.project.lumina.relay.definition

import org.cloudburstmc.nbt.NbtMap
import org.cloudburstmc.nbt.NbtType
import org.cloudburstmc.nbt.NbtUtils
import org.cloudburstmc.protocol.bedrock.data.definitions.BlockDefinition
import org.cloudburstmc.protocol.bedrock.data.definitions.ItemDefinition
import org.cloudburstmc.protocol.common.DefinitionRegistry
import org.cloudburstmc.protocol.common.NamedDefinition
import org.cloudburstmc.protocol.common.SimpleDefinitionRegistry
import java.io.InputStream


@Suppress("MemberVisibilityCanBePrivate")
object Definitions {

    var itemDefinitions: DefinitionRegistry<ItemDefinition> = SimpleDefinitionRegistry.builder<ItemDefinition>()
        .build()

    var blockDefinitions: DefinitionRegistry<BlockDefinition> =
        SimpleDefinitionRegistry.builder<BlockDefinition>()
            .build()

    var cameraPresetDefinitions: DefinitionRegistry<NamedDefinition> = SimpleDefinitionRegistry.builder<NamedDefinition>()
        .build()

    var blockDefinitionsHashed: DefinitionRegistry<BlockDefinition> =
        SimpleDefinitionRegistry.builder<BlockDefinition>()
            .build()

    fun loadBlockPalette() {
        Definitions::class.java.classLoader.getResourceAsStream("nbt/block_palette.nbt")?.let {
            val tag = loadGzipNBT(it)
            if (tag is NbtMap) {
                blockDefinitions = NbtBlockDefinitionRegistry(tag.getList("blocks", NbtType.COMPOUND), false)
                blockDefinitionsHashed = NbtBlockDefinitionRegistry(tag.getList("blocks", NbtType.COMPOUND), true)
            }
        }
    }

    private fun loadGzipNBT(stream: InputStream): Any {
        NbtUtils.createGZIPReader(stream).use { nbtInputStream ->
            return nbtInputStream.readTag()
        }
    }

}