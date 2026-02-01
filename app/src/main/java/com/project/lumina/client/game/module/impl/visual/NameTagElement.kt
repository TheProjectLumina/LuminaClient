/*
 * © Project Lumina 2026 — Licensed under GNU GPLv3
 * You are free to use, modify, and redistribute this code under the terms
 * of the GNU General Public License v3. See the LICENSE file for details.
 *
 * ─────────────────────────────────────────────────────────────────────────────
 * This is open source — not open credit.
 *
 * If you're here to build, welcome. If you're here to repaint and reupload
 * with your tag slapped on it… you're not fooling anyone.
 *
 * Changing colors and class names doesn't make you a developer.
 * Copy-pasting isn't contribution.
 *
 * You have legal permission to fork. But ask yourself — are you improving,
 * or are you just recycling someone else's work to feed your ego?
 *
 * Open source isn't about low-effort clones or chasing clout.
 * It's about making things better. Sharper. Cleaner. Smarter.
 *
 * So go ahead, fork it — but bring something new to the table,
 * or don't bother pretending.
 *
 * This message is philosophical. It does not override your legal rights under GPLv3.
 * ─────────────────────────────────────────────────────────────────────────────
 *
 * GPLv3 Summary:
 * - You have the freedom to run, study, share, and modify this software.
 * - If you distribute modified versions, you must also share the source code.
 * - You must keep this license and copyright intact.
 * - You cannot apply further restrictions — the freedom stays with everyone.
 * - This license is irrevocable, and applies to all future redistributions.
 *
 * Full text: https://www.gnu.org/licenses/gpl-3.0.html
 */

package com.project.lumina.client.game.module.impl.visual

import com.project.lumina.client.R
import com.project.lumina.client.game.InterceptablePacket
import com.project.lumina.client.constructors.Element
import com.project.lumina.client.constructors.CheatCategory
import com.project.lumina.client.game.entity.Entity
import com.project.lumina.client.game.entity.EntityUnknown
import com.project.lumina.client.game.entity.LocalPlayer
import com.project.lumina.client.game.entity.MobList
import com.project.lumina.client.game.entity.Player
import org.cloudburstmc.protocol.bedrock.data.entity.EntityDataMap
import org.cloudburstmc.protocol.bedrock.data.entity.EntityDataTypes
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket
import org.cloudburstmc.protocol.bedrock.packet.SetEntityDataPacket
import java.util.Locale
import com.project.lumina.client.util.AssetManager

class NameTagElement(iconResId: Int = AssetManager.getAsset("ic_guy_fawkes_mask_black_24dp")) : Element(
    name = "NameTag",
    category = CheatCategory.Visual,
    iconResId,
    displayNameResId = AssetManager.getString("module_name_tag_display_name")
) {
    
    private val showDistance by boolValue("Show Distance", true)
    private val colorPlayers by boolValue("Color Players", true)
    private val range by floatValue("Range", 20f, 5f..50f)

    
    private val originalNames = mutableMapOf<Long, String>()

    override fun onEnabled() {
        super.onEnabled()
        
        originalNames.clear()
    }

    override fun onDisabled() {
        super.onDisabled()
        
        if (isSessionCreated) {
            session.level.entityMap.values.forEach { entity ->
                val originalName = originalNames[entity.runtimeEntityId] ?: return@forEach
                val metadata = EntityDataMap()
                metadata.put(EntityDataTypes.NAME, originalName)
                session.clientBound(SetEntityDataPacket().apply {
                    runtimeEntityId = entity.runtimeEntityId
                    this.metadata = metadata
                })
            }
            originalNames.clear()
        }
    }

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled || interceptablePacket.packet !is PlayerAuthInputPacket) return

        
        if (session.localPlayer.tickExists % 20 != 0L) return

        session.level.entityMap.values
            .filter { it.distance(session.localPlayer) < range && it.isTarget() }
            .forEach { entity ->
                
                if (!originalNames.containsKey(entity.runtimeEntityId)) {
                    val currentName = entity.metadata[EntityDataTypes.NAME] as? String ?: ""
                    originalNames[entity.runtimeEntityId] = currentName
                }

                
                val customName = formatName(entity)
                val metadata = EntityDataMap()
                metadata.put(EntityDataTypes.NAME, customName)

                
                session.clientBound(SetEntityDataPacket().apply {
                    runtimeEntityId = entity.runtimeEntityId
                    this.metadata = metadata
                })
            }
    }

    /**
     * Formats the name tag for an entity based on settings.
     */
    private fun formatName(entity: Entity): String {
        val baseName = when (entity) {
            is Player -> session.level.playerMap[entity.uuid]?.name?.toString()?.takeIf { it.isNotBlank() } ?: "Player"
            is EntityUnknown -> entity.identifier.split(":").lastOrNull()?.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
            } ?: "Unknown"
            else -> entity.javaClass.simpleName
        }

        val color = if (colorPlayers && entity is Player && !entity.isBot()) "§a" else "§7"
        val distance = if (showDistance) {
            " [${"%.1f".format(entity.distance(session.localPlayer))}m]"
        } else {
            ""
        }

        return "$color$baseName$distance"
    }

    /**
     * Determines if an entity is a valid target for name tags.
     */
    private fun Entity.isTarget(): Boolean {
        return when (this) {
            is LocalPlayer -> false
            is Player -> !this.isBot()
            is EntityUnknown -> this.isMob()
            else -> false
        }
    }

    /**
     * Checks if an EntityUnknown is a mob.
     */
    private fun EntityUnknown.isMob(): Boolean {
        return this.identifier in MobList.mobTypes
    }

    /**
     * Checks if a Player is a bot.
     */
    private fun Player.isBot(): Boolean {
        if (this is LocalPlayer) return false
        val playerList = session.level.playerMap[this.uuid] ?: return true
        return playerList.name.toString().isBlank()
    }
}