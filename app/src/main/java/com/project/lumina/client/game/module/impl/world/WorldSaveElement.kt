package com.project.lumina.client.game.module.impl.world

import com.project.lumina.client.R
import com.project.lumina.client.application.AppContext
import com.project.lumina.client.constructors.Element
import com.project.lumina.client.constructors.CheatCategory
import com.project.lumina.client.game.world.save.LevelDBWorld
import com.project.lumina.client.game.world.World
import com.project.lumina.client.game.InterceptablePacket
import com.project.lumina.client.game.world.chunk.Chunk
import org.cloudburstmc.math.vector.Vector3i
import java.io.File

class WorldSaveElement : Element(
    name = "world_save",
    category = CheatCategory.World,
    displayNameResId = R.string.module_world_save_display_name
) {

    private var saveOnce = false

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled || saveOnce) return

        val player = session.localPlayer
        val world = session.world
        val pos = Vector3i.from(
            player.posX.toInt(),
            player.posY.toInt(),
            player.posZ.toInt()
        )

        val chunk = getAccessibleChunk(world, pos.x, pos.z)
        if (chunk == null) {
            session.displayClientMessage("⛔ Chunk not found at player position!")
            return
        }

        val saveFolder = File(AppContext.instance.filesDir, "world_saves")
        if (!saveFolder.exists()) saveFolder.mkdirs()

        val dbWorld = LevelDBWorld(saveFolder)

        dbWorld.saveChunk(chunk as Chunk)
        dbWorld.close()

        session.displayClientMessage("✅ Saved chunk at (${chunk.x}, ${chunk.z}) to LevelDB.")

        saveOnce = true
        isEnabled = false
    }

    private fun getAccessibleChunk(world: World, x: Int, z: Int): Chunk? {
        return try {
            val method = world::class.java.getDeclaredMethod("getChunkAt", Int::class.java, Int::class.java)
            method.isAccessible = true
            method.invoke(world, x, z) as? Chunk
        } catch (e: Exception) {
            null
        }
    }
}
