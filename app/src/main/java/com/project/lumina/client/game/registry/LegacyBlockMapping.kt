package com.project.lumina.client.game.registry

import android.content.Context
import org.cloudburstmc.nbt.NBTInputStream
import org.cloudburstmc.nbt.NbtList
import org.cloudburstmc.nbt.NbtMap
import java.io.DataInputStream
import java.util.zip.GZIPInputStream

class LegacyBlockMapping(private val stateToRuntimeMap: Map<Int, Int>) {

    fun toRuntime(id: Int, meta: Int): Int =
        toRuntime((id shl 6) or meta)

    fun toRuntime(state: Int): Int {
        return stateToRuntimeMap[state] ?: 0
    }

    companion object {
        fun read(context: Context, version: Short): LegacyBlockMapping {
            val path = "mcpedata/blocks/runtime_block_states_$version.dat"
            context.assets.open(path).use { inputStream ->
                val gzipStream = GZIPInputStream(inputStream)
                val dataInputStream = DataInputStream(gzipStream)
                val nbtStream = NBTInputStream(dataInputStream)

                val tag = nbtStream.readTag() as NbtList<NbtMap>
                val stateToRuntime = mutableMapOf<Int, Int>()

                tag.forEach { subtag ->
                    val state = (subtag.getInt("id") shl 6) or subtag.getShort("data").toInt()
                    val runtime = subtag.getInt("runtimeId")
                    stateToRuntime[state] = runtime
                }

                return LegacyBlockMapping(stateToRuntime)
            }
        }
    }

    class Provider(context: Context) : MappingProvider<LegacyBlockMapping>(context) {
        override val assetPath: String
            get() = "mcpedata/blocks"

        override fun readMapping(version: Short): LegacyBlockMapping {
            return read(context, version)
        }
    }
}
