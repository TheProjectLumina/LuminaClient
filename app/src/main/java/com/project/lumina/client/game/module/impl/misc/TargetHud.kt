package com.project.lumina.client.game.module.impl.misc

import com.project.lumina.client.constructors.CheatCategory
import com.project.lumina.client.constructors.Element
import com.project.lumina.client.game.InterceptablePacket
import com.project.lumina.client.game.entity.Entity
import com.project.lumina.client.game.entity.EntityUnknown
import com.project.lumina.client.game.entity.LocalPlayer
import com.project.lumina.client.game.entity.MobList
import com.project.lumina.client.game.entity.Player
import com.project.lumina.client.overlay.mods.TargetHudOverlay
import java.util.Locale
import com.project.lumina.client.util.AssetManager

class TargetHud(iconResId: Int = AssetManager.getAsset("ic_target")) : Element(
    name = "TargetHud",
    category = CheatCategory.Visual,
    displayNameResId = AssetManager.getString("module_targethud"),
    iconResId = iconResId
) {
    private val playerOnly = true
    private val mobsOnly = false
    private val rangeValue = 7f
    private val maxDistance by floatValue("MaxDistance", 7f, 1f..10f )

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) {
            try {
                TargetHudOverlay.dismissTargetHud()
            } catch (_: Exception) { }
            return
        }

        val closestEntities = searchForClosestEntities()
        val closestEntity = closestEntities.firstOrNull()

        if (closestEntity != null) {
            val username = getEntityName(closestEntity)
            val distance = closestEntity.distance(session.localPlayer)

            try {
               session.targetHud(username, distance, maxDistance, 0f)
            } catch (_: Exception) { }
        } else {
            try {
                TargetHudOverlay.dismissTargetHud()
            } catch (_: Exception) { }
        }
    }

    private fun Entity.isTarget(): Boolean {
        val isTarget = when (this) {
            is LocalPlayer -> false
            is Player -> {
                if (playerOnly || (playerOnly && mobsOnly)) {
                    !this.isBot()
                } else {
                    false
                }
            }
            is EntityUnknown -> {
                if (mobsOnly || (playerOnly && mobsOnly)) {
                    isMob()
                } else {
                    false
                }
            }
            else -> false
        }
         return isTarget
    }

    private fun EntityUnknown.isMob(): Boolean {
        val isMob = this.identifier in MobList.mobTypes
         return isMob
    }

    private fun Player.isBot(): Boolean {
        if (this is LocalPlayer) {

            return false
        }
        val playerList = session.level.playerMap[this.uuid]
        val isBot = (session.level.playerMap[uuid]?.name?.toString() ?: "").isBlank()

        return isBot
    }

    private fun searchForClosestEntities(): List<Entity> {
        val entities = session.level.entityMap.values
            .filter { entity ->
                val inRange = entity.distance(session.localPlayer) < rangeValue
                val isTarget = entity.isTarget()
                 inRange && isTarget
            }
            .sortedBy { it.distance(session.localPlayer) } 
            .take(1) 
          return entities
    }

    private fun getEntityName(entity: Entity): String {
        val name = when (entity) {
            is Player -> session.level.playerMap[entity.uuid]?.name?.toString()?.takeIf { it.isNotBlank() } ?: "Player"
            is EntityUnknown -> entity.identifier.split(":").lastOrNull()?.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
            } ?: "Unknown"
            else -> entity.javaClass.simpleName
        }
         return name
    }
}