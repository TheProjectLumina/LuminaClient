package com.project.lumina.client.constructors

import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import com.project.lumina.client.application.AppContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.cloudburstmc.math.vector.Vector3f
import org.json.JSONArray
import org.json.JSONObject

object SelectedMobsManager {
    private const val PREFS_NAME = "selected_mobs"
    private const val SELECTED_KEY = "selected_list"

    data class SelectedMobInfo(
        val mobId: Long,
        val mobName: String,
        val selectedAt: Long,
        val lastKnownX: Float,
        val lastKnownY: Float,
        val lastKnownZ: Float
    ) {
        fun getPosition(): Vector3f = Vector3f.from(lastKnownX, lastKnownY, lastKnownZ)

        fun toJson(): JSONObject {
            return JSONObject().apply {
                put("mobId", mobId)
                put("mobName", mobName)
                put("selectedAt", selectedAt)
                put("x", lastKnownX)
                put("y", lastKnownY)
                put("z", lastKnownZ)
            }
        }

        companion object {
            fun fromJson(json: JSONObject): SelectedMobInfo {
                return SelectedMobInfo(
                    mobId = json.getLong("mobId"),
                    mobName = json.getString("mobName"),
                    selectedAt = json.getLong("selectedAt"),
                    lastKnownX = json.getDouble("x").toFloat(),
                    lastKnownY = json.getDouble("y").toFloat(),
                    lastKnownZ = json.getDouble("z").toFloat()
                )
            }
        }
    }

    private val selectedMobs = mutableStateListOf<SelectedMobInfo>()
    private val scope = CoroutineScope(Dispatchers.Main)
    private val detectedMobTypes = mutableSetOf<String>()

    var onSelectedMobDetected: ((EntityStorage.EntityInfo) -> Unit)? = null

    init {
        loadSelected()
    }

    fun selectMob(entity: EntityStorage.EntityInfo) {
        val existing = selectedMobs.find { it.mobId == entity.id }
        if (existing == null) {
            val mobInfo = SelectedMobInfo(
                mobId = entity.id,
                mobName = entity.name,
                selectedAt = System.currentTimeMillis(),
                lastKnownX = entity.coords.x,
                lastKnownY = entity.coords.y,
                lastKnownZ = entity.coords.z
            )
            selectedMobs.add(mobInfo)
            saveSelected()
            Log.d("SelectedMobsManager", "Selected mob: ${entity.name} (ID: ${entity.id})")
        }
    }

    fun unselectMob(mobId: Long) {
        selectedMobs.removeAll { it.mobId == mobId }
        saveSelected()
        Log.d("SelectedMobsManager", "Unselected mob ID: $mobId")
    }

    fun updateMobPosition(entity: EntityStorage.EntityInfo) {
        val index = selectedMobs.indexOfFirst { it.mobId == entity.id }
        if (index != -1) {
            val updated = selectedMobs[index].copy(
                lastKnownX = entity.coords.x,
                lastKnownY = entity.coords.y,
                lastKnownZ = entity.coords.z
            )
            selectedMobs[index] = updated
            saveSelected()
        }
    }

    fun checkMobDetection(entity: EntityStorage.EntityInfo) {
        val mobType = entity.name.lowercase().trim()
        val isSelectedType = selectedMobs.any { it.mobName.lowercase().trim() == mobType }

        if (isSelectedType && !detectedMobTypes.contains(mobType)) {
            detectedMobTypes.add(mobType)
            onSelectedMobDetected?.invoke(entity)
            Log.d("SelectedMobsManager", "Selected mob type detected: ${entity.name} (ID: ${entity.id})")
        }
    }

    fun markMobTypeAsOutOfRange(mobType: String) {
        val normalizedType = mobType.lowercase().trim()
        if (detectedMobTypes.contains(normalizedType)) {
            detectedMobTypes.remove(normalizedType)
            Log.d("SelectedMobsManager", "Mob type marked as out of range: $mobType")
        }
    }

    fun resetDetections() {
        detectedMobTypes.clear()
        Log.d("SelectedMobsManager", "Reset detections")
    }

    fun isSelected(mobId: Long): Boolean {
        return selectedMobs.any { it.mobId == mobId }
    }

    fun getSelectedMobs(): List<SelectedMobInfo> = selectedMobs.toList()

    fun getSelectedCount(): Int = selectedMobs.size

    fun clearAll() {
        selectedMobs.clear()
        saveSelected()
        Log.d("SelectedMobsManager", "Cleared all selected mobs")
    }

    fun resetOnDisconnect() {
        selectedMobs.clear()
        detectedMobTypes.clear()
        saveSelected()
    }

    private fun saveSelected() {
        scope.launch(Dispatchers.IO) {
            try {
                val prefs = AppContext.instance.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                val jsonArray = JSONArray()
                selectedMobs.forEach { mobInfo ->
                    jsonArray.put(mobInfo.toJson())
                }
                prefs.edit().putString(SELECTED_KEY, jsonArray.toString()).apply()
                Log.d("SelectedMobsManager", "Saved ${selectedMobs.size} selected mobs")
            } catch (e: Exception) {
                Log.e("SelectedMobsManager", "Failed to save selected mobs", e)
            }
        }
    }

    private fun loadSelected() {
        try {
            val prefs = AppContext.instance.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val jsonString = prefs.getString(SELECTED_KEY, null)

            if (jsonString != null) {
                val jsonArray = JSONArray(jsonString)
                selectedMobs.clear()
                for (i in 0 until jsonArray.length()) {
                    val mobInfo = SelectedMobInfo.fromJson(jsonArray.getJSONObject(i))
                    selectedMobs.add(mobInfo)
                }
                Log.d("SelectedMobsManager", "Loaded ${selectedMobs.size} selected mobs")
            }
        } catch (e: Exception) {
            Log.e("SelectedMobsManager", "Failed to load selected mobs", e)
        }
    }
}