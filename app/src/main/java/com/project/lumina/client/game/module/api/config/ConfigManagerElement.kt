/*
 * © Project Lumina 2025 — GPLv3 Licensed
 * You may use, modify, and share this code under the GPL.
 *
 * Just know: changing names and colors doesn't make you a developer.
 * Think before you fork. Build something real — or don't bother.
 */

package com.project.lumina.client.game.module.api.config

import android.content.Context
import android.os.Environment
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.project.lumina.client.R
import com.project.lumina.client.application.AppContext
import com.project.lumina.client.game.InterceptablePacket
import com.project.lumina.client.constructors.Element
import com.project.lumina.client.constructors.CheatCategory
import com.project.lumina.client.constructors.GameManager
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.core.content.ContextCompat
import kotlinx.serialization.json.jsonObject

class ConfigManagerElement : Element(
    name = "config_manager",
    category = CheatCategory.Config,
    displayNameResId = R.string.module_config_manager
) {
    
    val configFiles = mutableStateListOf<ConfigFile>()

    
    var selectedConfig by mutableStateOf<ConfigFile?>(null)

    init {
        refreshConfigFiles()
    }

    /**
     * Get the configs directory in app's internal storage
     */
    private fun getConfigsDirectory(): File {
        val configsDir = AppContext.instance.getDir("configs", Context.MODE_PRIVATE)
        if (!configsDir.exists()) {
            configsDir.mkdirs()
        }
        return configsDir
    }
    
    /**
     * Check if storage permissions are granted for the export functionality
     */
    private fun hasExportPermissions(): Boolean {
        val context = AppContext.instance
        
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return Environment.isExternalStorageManager()
        } 
        
        else {
            val writePermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            return writePermission == PackageManager.PERMISSION_GRANTED
        }
    }
    
    /**
     * Request storage permissions for Android 11+ (API level 30+)
     */
    fun requestStoragePermission() {
        val context = AppContext.instance
        
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                val uri = Uri.fromParts("package", context.packageName, null)
                intent.data = uri
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
                
                Toast.makeText(
                    context,
                    "Please grant 'All files access' permission to export to Downloads",
                    Toast.LENGTH_LONG
                ).show()
            }
        } catch (e: Exception) {
            Log.e("ConfigManager", "Failed to request permission: ${e.message}", e)
        }
    }
    
    /**
     * Import a configuration from a content URI
     */
    fun importConfig(uri: Uri): Boolean {
        val context = AppContext.instance
        
        try {
            val contentResolver = context.contentResolver
            
            
            val inputStream = contentResolver.openInputStream(uri) ?: throw Exception("Cannot open input stream")
            
            
            var fileName = ""
            contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val displayNameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (displayNameIndex != -1) {
                        fileName = cursor.getString(displayNameIndex)
                    }
                }
            }
            
            
            if (fileName.isEmpty() || !fileName.endsWith(".json")) {
                fileName = "imported_config_${System.currentTimeMillis()}.json"
            }
            
            
            val configName = fileName.substringBeforeLast(".json").substringBeforeLast(".")
            
            
            val jsonContent = inputStream.bufferedReader().use { it.readText() }
            
            
            try {
                val jsonObject = GameManager.json.parseToJsonElement(jsonContent).jsonObject
                if (!jsonObject.containsKey("modules")) {
                    Toast.makeText(
                        context,
                        "Invalid config file format: missing 'modules' key",
                        Toast.LENGTH_LONG
                    ).show()
                    return false
                }
            } catch (e: Exception) {
                Toast.makeText(
                    context,
                    "Invalid JSON format: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
                return false
            }
            
            
            val configsDir = getConfigsDirectory()
            val internalFile = File(configsDir, "$configName.json")
            
            
            if (internalFile.exists()) {
                internalFile.delete()
                
                
                if (selectedConfig?.name == configName) {
                    selectedConfig = null
                }
            }
            
            
            FileOutputStream(internalFile).use { output ->
                contentResolver.openInputStream(uri)?.use { input ->
                    input.copyTo(output)
                } ?: throw Exception("Cannot re-open input stream")
            }
            
            
            refreshConfigFiles()
            
            Toast.makeText(
                context,
                "Config '$configName' imported successfully",
                Toast.LENGTH_SHORT
            ).show()
            
            return true
        } catch (e: Exception) {
            Log.e("ConfigManager", "Import error: ${e.message}", e)
            Toast.makeText(
                context,
                "Failed to import config: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
            return false
        }
    }
    
    /**
     * Get next config number for automatic naming
     */
    private fun getNextConfigNumber(): Int {
        val existing = configFiles.map { 
            val name = it.name
            if (name.startsWith("config") && name.endsWith(".json")) {
                val numberPart = name.removePrefix("config").removeSuffix(".json")
                numberPart.toIntOrNull() ?: 0
            } else {
                0
            }
        }
        
        return if (existing.isEmpty()) 1 else (existing.maxOrNull() ?: 0) + 1
    }

    /**
     * Save configuration with automatic name
     */
    fun saveConfig() {
        try {
            val configsDir = getConfigsDirectory()
            val nextNumber = getNextConfigNumber()
            val configName = "config$nextNumber"
            
            val config = File(configsDir, "$configName.json")
            GameManager.saveConfigToFile(config)
            
            Toast.makeText(
                AppContext.instance,
                "Config saved successfully",
                Toast.LENGTH_SHORT
            ).show()

            refreshConfigFiles()
        } catch (e: Exception) {
            Toast.makeText(
                AppContext.instance,
                "Error saving config: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    /**
     * Load configuration from the given file
     */
    fun loadConfig(configFile: ConfigFile) {
        try {
            GameManager.loadConfigFromFile(configFile.file)
            selectedConfig = configFile
            Toast.makeText(
                AppContext.instance,
                "Config loaded: ${configFile.name}",
                Toast.LENGTH_SHORT
            ).show()
        } catch (e: Exception) {
            Toast.makeText(
                AppContext.instance,
                "Error loading config: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    /**
     * Delete a configuration file
     */
    fun deleteConfig(configFile: ConfigFile) {
        try {
            if (configFile.file.exists()) {
                configFile.file.delete()
            }

            if (selectedConfig == configFile) {
                selectedConfig = null
            }

            refreshConfigFiles()
            
            Toast.makeText(
                AppContext.instance,
                "Config deleted",
                Toast.LENGTH_SHORT
            ).show()
        } catch (e: Exception) {
            Toast.makeText(
                AppContext.instance,
                "Error deleting config: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    /**
     * Export a configuration to the Downloads directory
     */
    fun exportConfig(configFile: ConfigFile): Boolean {
        try {
            
            if (!hasExportPermissions()) {
                
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    requestStoragePermission()
                    Toast.makeText(
                        AppContext.instance,
                        "Please grant storage permissions and try again",
                        Toast.LENGTH_LONG
                    ).show()
                    return false
                }
                
                Toast.makeText(
                    AppContext.instance,
                    "Storage permission required to export config",
                    Toast.LENGTH_LONG
                ).show()
                return false
            }
            
            
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!downloadsDir.exists()) {
                downloadsDir.mkdirs()
            }

            val exportFile = File(downloadsDir, configFile.name + ".json")

            
            configFile.file.inputStream().use { input ->
                FileOutputStream(exportFile).use { output ->
                    input.copyTo(output)
                }
            }

            Toast.makeText(
                AppContext.instance,
                "Config exported to Downloads: ${exportFile.name}",
                Toast.LENGTH_LONG
            ).show()

            return true
        } catch (e: Exception) {
            Log.e("ConfigManager", "Export error: ${e.message}", e)
            Toast.makeText(
                AppContext.instance,
                "Failed to export config: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
            return false
        }
    }

    /**
     * Refresh the list of available configuration files
     */
    fun refreshConfigFiles() {
        try {
            val configsDir = getConfigsDirectory()
            
            
            configFiles.clear()

            
            configsDir.listFiles()?.filter { it.isFile && it.name.endsWith(".json") }?.forEach { file ->
                val creationTime = Date(file.lastModified())
                val nameWithoutExt = file.nameWithoutExtension
                configFiles.add(ConfigFile(nameWithoutExt, file, creationTime))
            }
        } catch (e: Exception) {
            Toast.makeText(
                AppContext.instance,
                "Error refreshing configs: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    /**
     * Required implementation of abstract method from Module
     */
    override fun beforePacketBound(packet: InterceptablePacket): Unit {
        
    }

    /**
     * Represents a configuration file in the list
     */
    data class ConfigFile(
        val name: String,
        val file: File,
        val creationTime: Date = Date()
    ) {
        fun getFormattedTime(): String {
            val dateFormat = SimpleDateFormat("MMM d, yyyy 'at' h:mm a", Locale.getDefault())
            return dateFormat.format(creationTime)
        }
    }
}