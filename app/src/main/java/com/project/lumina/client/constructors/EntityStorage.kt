package com.project.lumina.client.constructors

import android.util.Log
import com.project.lumina.client.application.AppContext
import com.project.lumina.client.game.entity.Entity
import com.project.lumina.client.game.entity.EntityUnknown
import com.project.lumina.client.game.entity.LocalPlayer
import com.project.lumina.client.game.entity.MobList
import com.project.lumina.client.constructors.NetBound
import com.project.lumina.client.constructors.MobAlertManager
import com.project.lumina.client.constructors.SelectedMobsManager
import net.kyori.adventure.text.Component
import org.cloudburstmc.math.vector.Vector3f
import org.json.JSONArray
import org.json.JSONObject
import com.project.lumina.client.R
import java.io.BufferedReader
import java.util.Locale
import kotlin.math.atan2
import kotlin.math.ceil
import kotlin.math.sqrt

class EntityStorage(
    private val session: NetBound,
    private val range: Float = 50f
) {

    private val entities = mutableMapOf<Long, EntityInfo>()
    private val textureJson: JSONObject by lazy {
        loadTextureJson()
    }

    data class EntityInfo(
        val id: Long,
        val name: String,
        val coords: Vector3f,
        val relativeHeight: String,
        val direction: String,
        val imagePath: String?,
        val firstSeen: Long = System.currentTimeMillis(),
        val lastSeen: Long = System.currentTimeMillis(),
        val velocity: Float = 0f,
        val minDistance: Float = Float.MAX_VALUE,
        val maxDistance: Float = 0f
    )

    private fun loadTextureJson(): JSONObject {
        return try {
            val inputStream = AppContext.instance.resources.openRawResource(R.raw.texture_meta)
            val jsonString = inputStream.bufferedReader().use(BufferedReader::readText)
            JSONObject(jsonString)
        } catch (e: Exception) {
            Log.e("EntityStorage", "Failed to load texture_meta.json", e)
            JSONObject()
        }
    }

    private fun getTexturePath(entityName: String): String? {
        return try {
            val entityKey = entityName.lowercase(Locale.getDefault())
            val entityObj = textureJson.getJSONObject(entityKey)
            val types = entityObj.getJSONArray("types") as JSONArray
            var selectedType: JSONObject? = null
            for (i in 0 until types.length()) {
                val type = types.getJSONObject(i)
                if (type.has("default") && type.getBoolean("default")) {
                    selectedType = type
                    break
                }
            }
            
            if (selectedType == null) {
                return null
            }
            val textures = selectedType.getJSONObject("textures")
            textures.optString("16", null).ifBlank { null }
        } catch (e: Exception) {
            Log.w("EntityStorage", "Failed to get texture path for $entityName", e)
            null
        }
    }

    fun updateEntities() {
        val playerPos = session.localPlayer.vec3Position
        val playerY = playerPos.y

        
        entities.entries.removeAll { (_, info) ->
            val dist = calculateDistance(playerPos, info.coords)
            dist > range
        }

        
        for ((id, entity) in session.level.entityMap) {
            if (entities.containsKey(id)) continue
            if (!isValidMob(entity)) continue
            val pos = entity.vec3Position
            val dist = calculateDistance(playerPos, pos)
            if (dist > range) continue

            val name = getEntityName(entity)
            val imagePath = getTexturePath(name)
            val dy = pos.y - playerY
            val relativeHeight = calculateRelativeHeight(dy)
            val currentTime = System.currentTimeMillis()
            val info = EntityInfo(
                id = id,
                name = name,
                coords = pos,
                relativeHeight = relativeHeight,
                direction = "IDLE",
                imagePath = imagePath,
                firstSeen = currentTime,
                lastSeen = currentTime,
                velocity = 0f,
                minDistance = dist,
                maxDistance = dist
            )
            entities[id] = info

            MobAlertManager.checkMobDetection(name)
            SelectedMobsManager.checkMobDetection(info)
        }

        val selectedMobsInRange = mutableSetOf<Long>()
        val selectedMobTypesInRange = mutableSetOf<String>()

        entities.forEach { (id, info) ->
            val entity = session.level.entityMap[id] ?: run {
                entities.remove(id)
                return@forEach
            }
            if (!isValidMob(entity)) {
                entities.remove(id)
                return@forEach
            }

            if (SelectedMobsManager.isSelected(id)) {
                selectedMobsInRange.add(id)
            }

            
            val mobType = info.name.lowercase().trim()
            val isSelectedType = SelectedMobsManager.getSelectedMobs().any {
                it.mobName.lowercase().trim() == mobType
            }
            if (isSelectedType) {
                selectedMobTypesInRange.add(mobType)
            }

            val pos = entity.vec3Position
            val dy = pos.y - playerY
            val newHeight = calculateRelativeHeight(dy)

            val dx = pos.x - info.coords.x
            val dz = pos.z - info.coords.z
            val calculatedVelocity = sqrt(dx * dx + dz * dz)

            val dist = calculateDistance(playerPos, pos)
            val newMinDistance = minOf(info.minDistance, dist)
            val newMaxDistance = maxOf(info.maxDistance, dist)

            val direction = if (calculatedVelocity < 0.1f) {
                "IDLE"
            } else {
                getCompassDirection(dx, dz)
            }

            val updatedInfo = info.copy(
                coords = pos,
                relativeHeight = newHeight,
                direction = direction,
                lastSeen = System.currentTimeMillis(),
                velocity = calculatedVelocity,
                minDistance = newMinDistance,
                maxDistance = newMaxDistance
            )
            entities[id] = updatedInfo

            if (SelectedMobsManager.isSelected(id)) {
                SelectedMobsManager.updateMobPosition(updatedInfo)
            }
        }

        
        SelectedMobsManager.getSelectedMobs().forEach { selectedMob ->
            val mobType = selectedMob.mobName.lowercase().trim()
            if (!selectedMobTypesInRange.contains(mobType)) {
                SelectedMobsManager.markMobTypeAsOutOfRange(mobType)
            }
        }


        //debug
        Log.d("EntityStorage", "=== FULL UPDATE (${entities.size} entities) ===")
        entities.values.forEach { info ->
            Log.d("EntityStorage", formatEntityInfo(info))
        }
        Log.d("EntityStorage", "========================")
    }

    fun onEntityMove(entityId: Long, newPos: Vector3f) {
        val oldInfo = entities[entityId] ?: return
        val oldPos = oldInfo.coords
        val deltaX = newPos.x - oldPos.x
        val deltaZ = newPos.z - oldPos.z
        val movementSpeed = sqrt((deltaX * deltaX + deltaZ * deltaZ).toDouble()).toFloat()
        val direction = if (movementSpeed < 0.1f) {
            "IDLE"
        } else {
            getCompassDirection(deltaX, deltaZ)
        }
        val updatedInfo = oldInfo.copy(coords = newPos, direction = direction)
        entities[entityId] = updatedInfo
    }

    fun getEntities(): Map<Long, EntityInfo> = entities.toMap()

    fun clear() {
        entities.clear()
    }

    private fun calculateRelativeHeight(dy: Float): String {
        return if (Math.abs(dy) < 1f) {
            "same height"
        } else {
            val blocks = ceil(Math.abs(dy)).toInt()
            if (dy > 0) {
                "$blocks blocks above"
            } else {
                "$blocks blocks below"
            }
        }
    }

    private fun getCompassDirection(dx: Float, dz: Float): String {
        val angle = Math.toDegrees(atan2(dx.toDouble(), (-dz).toDouble())).let {
            ((it + 360) % 360)
        }
        return when {
            angle >= 337.5 || angle < 22.5 -> "N"
            angle >= 22.5 && angle < 67.5 -> "NE"
            angle >= 67.5 && angle < 112.5 -> "E"
            angle >= 112.5 && angle < 157.5 -> "SE"
            angle >= 157.5 && angle < 202.5 -> "S"
            angle >= 202.5 && angle < 247.5 -> "SW"
            angle >= 247.5 && angle < 292.5 -> "W"
            else -> "NW"
        }
    }

    private fun calculateDistance(from: Vector3f, to: Vector3f): Float {
        val dx = from.x - to.x
        val dy = from.y - to.y
        val dz = from.z - to.z
        return sqrt((dx * dx + dy * dy + dz * dz).toDouble()).toFloat()
    }

    private fun isValidMob(entity: Entity): Boolean {
        return when (entity) {
            is EntityUnknown -> entity.identifier in MobList.mobTypes
            else -> false
        }
    }

    private fun getEntityName(entity: Entity): String {
        return when (entity) {
            is EntityUnknown -> entity.identifier.split(":").lastOrNull()?.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
            } ?: "Unknown"
            else -> "Unknown"
        }
    }


    private fun formatEntityInfo(info: EntityInfo): String {
        val roundedCoords = "(${ceil(info.coords.x).toInt()}, ${ceil(info.coords.y).toInt()}, ${ceil(info.coords.z).toInt()})"
        val texture = if (info.imagePath != null) " | Texture: ${info.imagePath}" else ""
        return "Entity(ID: ${info.id}, Name: ${info.name}, Coords: $roundedCoords, Height: ${info.relativeHeight}, Dir/Moving: ${info.direction}$texture"
    }
}