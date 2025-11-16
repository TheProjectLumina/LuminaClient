package com.project.lumina.client.constructors

import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import com.project.lumina.client.application.AppContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

object MobAlertManager {
    private const val PREFS_NAME = "mob_alerts"
    private const val ALERTS_KEY = "alerts_list"
    
    private val alertedMobs = mutableStateListOf<String>()
    private val detectedMobs = mutableSetOf<String>()
    private val scope = CoroutineScope(Dispatchers.Main)
    
    var onMobDetected: ((String) -> Unit)? = null
    
    init {
        loadAlerts()
    }
    
    fun addAlert(mobName: String) {
        val normalizedName = mobName.lowercase().trim()
        if (!alertedMobs.contains(normalizedName)) {
            alertedMobs.add(normalizedName)
            saveAlerts()
            Log.d("MobAlertManager", "Added alert for: $normalizedName")
        }
    }
    
    fun removeAlert(mobName: String) {
        val normalizedName = mobName.lowercase().trim()
        alertedMobs.remove(normalizedName)
        detectedMobs.remove(normalizedName)
        saveAlerts()
        Log.d("MobAlertManager", "Removed alert for: $normalizedName")
    }
    
    fun getAlerts(): List<String> = alertedMobs.toList()
    
    fun hasAlert(mobName: String): Boolean {
        val normalizedName = mobName.lowercase().trim()
        return alertedMobs.contains(normalizedName)
    }
    
    fun checkMobDetection(mobName: String) {
        val normalizedName = mobName.lowercase().trim()
        
        if (alertedMobs.contains(normalizedName) && !detectedMobs.contains(normalizedName)) {
            detectedMobs.add(normalizedName)
            onMobDetected?.invoke(mobName)
            Log.d("MobAlertManager", "Mob detected: $mobName")
        }
    }
    
    fun resetDetections() {
        detectedMobs.clear()
    }
    
    fun clearAllAlerts() {
        alertedMobs.clear()
        detectedMobs.clear()
        saveAlerts()
    }
    
    private fun saveAlerts() {
        scope.launch(Dispatchers.IO) {
            try {
                val prefs = AppContext.instance.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                val jsonArray = JSONArray(alertedMobs)
                prefs.edit().putString(ALERTS_KEY, jsonArray.toString()).apply()
                Log.d("MobAlertManager", "Saved ${alertedMobs.size} alerts")
            } catch (e: Exception) {
                Log.e("MobAlertManager", "Failed to save alerts", e)
            }
        }
    }
    
    private fun loadAlerts() {
        try {
            val prefs = AppContext.instance.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val jsonString = prefs.getString(ALERTS_KEY, null)
            
            if (jsonString != null) {
                val jsonArray = JSONArray(jsonString)
                alertedMobs.clear()
                for (i in 0 until jsonArray.length()) {
                    alertedMobs.add(jsonArray.getString(i))
                }
                Log.d("MobAlertManager", "Loaded ${alertedMobs.size} alerts")
            }
        } catch (e: Exception) {
            Log.e("MobAlertManager", "Failed to load alerts", e)
        }
    }
}