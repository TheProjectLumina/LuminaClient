package com.project.lumina.client.util


import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File
import java.net.URL

@Serializable
data class MCPack(
    val name: String,
    val url: String,
    val image: String
)

object MCPackUtils {
    suspend fun fetchPacksFromJson(jsonUrl: String): List<MCPack> = withContext(Dispatchers.IO) {
        try {
            val jsonText = URL(jsonUrl).readText()
            Json.decodeFromString<List<MCPack>>(jsonText)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun downloadAndOpenPack(
        context: Context,
        pack: MCPack,
        onProgress: (Float) -> Unit 
    ) = withContext(Dispatchers.IO) {
        try {
            
            val url = URL(pack.url)
            val fileName = "${pack.name}.mcpack"
            val file = File(context.getExternalFilesDir(null), fileName)

            val connection = url.openConnection()
            val totalSize = connection.contentLengthLong
            var downloadedSize = 0L

            url.openStream().use { input ->
                file.outputStream().use { output ->
                    val buffer = ByteArray(1024)
                    var bytesRead: Int
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        downloadedSize += bytesRead
                        if (totalSize > 0) {
                            onProgress((downloadedSize.toFloat() / totalSize))
                        }
                    }
                }
            }

            
            val contentUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(contentUri, "application/x-mcpack")
                setPackage("com.mojang.minecraftpe")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            withContext(Dispatchers.Main) {
                context.startActivity(intent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            throw e 
        }
    }
}