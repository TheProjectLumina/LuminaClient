package com.project.lumina.client.chat

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

class ChatRepository(private val context: Context) {
    
    private val json = Json { 
        ignoreUnknownKeys = true
        prettyPrint = true
    }
    
    private val chatFile = File(context.filesDir, "chat_messages.json")
    
    suspend fun saveMessages(messages: List<ChatMessage>) = withContext(Dispatchers.IO) {
        try {
            val jsonString = json.encodeToString(messages)
            chatFile.writeText(jsonString)
            Log.d("ChatRepository", "Saved ${messages.size} messages")
        } catch (e: Exception) {
            Log.e("ChatRepository", "Error saving messages", e)
        }
    }
    
    suspend fun loadMessages(): List<ChatMessage> = withContext(Dispatchers.IO) {
        try {
            if (!chatFile.exists()) {
                return@withContext emptyList()
            }
            
            val jsonString = chatFile.readText()
            val messages = json.decodeFromString<List<ChatMessage>>(jsonString)
            Log.d("ChatRepository", "Loaded ${messages.size} messages")
            messages
        } catch (e: Exception) {
            Log.e("ChatRepository", "Error loading messages", e)
            emptyList()
        }
    }
    
    suspend fun addMessage(message: ChatMessage): List<ChatMessage> {
        val currentMessages = loadMessages().toMutableList()
        currentMessages.add(message)
        saveMessages(currentMessages)
        return currentMessages
    }
    
    suspend fun updateMessage(updatedMessage: ChatMessage): List<ChatMessage> {
        val currentMessages = loadMessages().toMutableList()
        val index = currentMessages.indexOfFirst { it.id == updatedMessage.id }
        if (index != -1) {
            currentMessages[index] = updatedMessage
            saveMessages(currentMessages)
        }
        return currentMessages
    }
    
    suspend fun deleteMessage(messageId: String): List<ChatMessage> {
        val currentMessages = loadMessages().toMutableList()
        currentMessages.removeAll { it.id == messageId }
        saveMessages(currentMessages)
        return currentMessages
    }

    suspend fun updateMessageSettings(messageId: String, spamInterval: Int): List<ChatMessage> {
        val currentMessages = loadMessages().toMutableList()
        val index = currentMessages.indexOfFirst { it.id == messageId }
        if (index != -1) {
            val message = currentMessages[index]
            currentMessages[index] = message.copy(
                spamInterval = spamInterval
            )
            saveMessages(currentMessages)
        }
        return currentMessages
    }
}