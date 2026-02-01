package com.project.lumina.client.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URL

object InjectNeko{
    private const val PACK_URL = API.FILES_PACKS_LUMINA_PACK_MCPACK

    suspend fun injectNeko (
        context: Context,
        onProgress: (Float) -> Unit
    ) = withContext(Dispatchers.IO) {
        try {
            val url = URL(PACK_URL)
            val fileName = url.path.substringAfterLast("/")
                .takeIf { it.isNotEmpty() } ?: "lumina_pack.mcpack"

            val file = File(context.getExternalFilesDir(null), fileName)

            if (!file.exists()) {
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


                if (!file.exists()) {
                    throw IllegalStateException("File not downloaded: ${file.absolutePath}")
                }
            }

            val contentUri = FileProvider.getUriForFile(
                context,
                "com.project.lumina.client.fileprovider",
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
            throw e
        }
    }
}