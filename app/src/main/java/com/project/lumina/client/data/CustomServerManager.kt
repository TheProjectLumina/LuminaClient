package com.project.lumina.client.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.project.lumina.client.application.AppContext

data class CustomServer(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val serverAddress: String,
    val port: Int
)

class CustomServerManager {
    private val sharedPreferences: SharedPreferences by lazy {
        AppContext.instance.getSharedPreferences("custom_servers", Context.MODE_PRIVATE)
    }
    
    private val gson = Gson()
    
    companion object {
        @Volatile
        private var INSTANCE: CustomServerManager? = null
        
        fun getInstance(): CustomServerManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: CustomServerManager().also { INSTANCE = it }
            }
        }
    }
    
    fun saveServer(server: CustomServer) {
        val servers = getServers().toMutableList()
        
        val existingIndex = servers.indexOfFirst { it.name == server.name }
        if (existingIndex != -1) {
            servers[existingIndex] = server
        } else {
            servers.add(server)
        }
        
        saveServers(servers)
    }
    
    fun deleteServer(serverId: String) {
        val servers = getServers().toMutableList()
        servers.removeAll { it.id == serverId }
        saveServers(servers)
    }
    
    fun getServers(): List<CustomServer> {
        val serversJson = sharedPreferences.getString("servers", null)
        return if (serversJson != null) {
            try {
                val type = object : TypeToken<List<CustomServer>>() {}.type
                gson.fromJson(serversJson, type) ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }
    }
    
    private fun saveServers(servers: List<CustomServer>) {
        val serversJson = gson.toJson(servers)
        sharedPreferences.edit {
            putString("servers", serversJson)
        }
    }
    
    fun clearAllServers() {
        sharedPreferences.edit {
            remove("servers")
        }
    }
}