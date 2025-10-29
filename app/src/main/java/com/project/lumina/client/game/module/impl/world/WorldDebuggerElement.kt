package com.project.lumina.client.game.module.impl.world

import com.project.lumina.client.R
import com.project.lumina.client.game.InterceptablePacket
import com.project.lumina.client.constructors.Element
import com.project.lumina.client.constructors.CheatCategory
import org.cloudburstmc.math.vector.Vector3i
import com.project.lumina.client.game.world.chunk.Chunk
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket

class WorldDebuggerElement : Element(
    name = "world_debugger",
    category = CheatCategory.World,
    displayNameResId = R.string.module_world_debugger_display_name
) {

    private var replaceBlock by boolValue("ReplaceBlock", false)
    private var dumpChunk by boolValue("DumpChunk", true)
    private var hasRun = false

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled || hasRun) return

        
        val packet = interceptablePacket.packet
        if (packet is PlayerAuthInputPacket) {
            val player = session.localPlayer
            val world = session.world

            val pos = Vector3i.from(
                player.posX.toInt(),
                player.posY.toInt() - 1,
                player.posZ.toInt()
            )

            val blockId = world.getBlockIdAt(pos)
            session.displayClientMessage("Block under player: $blockId")

            if (replaceBlock) {
                world.setBlockIdAt(pos, 1)
                session.displayClientMessage("Replaced block with STONE (ID 1)")
            }

            if (dumpChunk) {
                val chunk = getAccessibleChunk(world, pos.x, pos.z)
                if (chunk != null) {
                    session.displayClientMessage("Chunk at [${chunk.x}, ${chunk.z}], MaxY=${chunk.maximumHeight}")
                } else {
                    session.displayClientMessage("Chunk not found at player location")
                }
            }

            hasRun = true
            isEnabled = false
        }
    }

    private fun getAccessibleChunk(world: com.project.lumina.client.game.world.World, x: Int, z: Int): Chunk? {
        return try {
            val method = world::class.java.getDeclaredMethod("getChunkAt", Int::class.java, Int::class.java)
            method.isAccessible = true
            method.invoke(world, x, z) as? Chunk
        } catch (e: Exception) {
            null
        }
    }
}
