package com.project.lumina.client.util

import android.util.Log

object RealmErrorHandler {
    private const val TAG = "RealmErrorHandler"
    
    fun translateError(throwable: Throwable?, fallbackMessage: String = "An unexpected error occurred"): String {
        if (throwable == null) return fallbackMessage
        
        val errorMessage = throwable.message ?: ""
        val errorClass = throwable.javaClass.simpleName
        
        Log.d(TAG, "Translating error: $errorClass - $errorMessage")
        
        return when {
            isAuthenticationError(errorMessage, errorClass) -> handleAuthenticationError(errorMessage)
            isJsonParsingError(errorMessage, errorClass) -> handleJsonParsingError()
            isNetworkError(errorMessage, errorClass) -> handleNetworkError()
            isRealmServiceError(errorMessage, errorClass) -> handleRealmServiceError(errorMessage)
            isSessionError(errorMessage, errorClass) -> handleSessionError()
            else -> handleGenericError(errorMessage, fallbackMessage)
        }
    }
    
    private fun isAuthenticationError(message: String, errorClass: String): Boolean {
        return message.contains("401") ||
               message.contains("Unauthorized") ||
               message.contains("authentication") ||
               message.contains("auth") ||
               errorClass.contains("Auth")
    }
    
    private fun isJsonParsingError(message: String, errorClass: String): Boolean {
        return message.contains("JsonReader") ||
               message.contains("setStrictness") ||
               message.contains("malformed JSON") ||
               message.contains("JSON") ||
               errorClass.contains("Json") ||
               errorClass.contains("Gson")
    }
    
    private fun isNetworkError(message: String, errorClass: String): Boolean {
        return message.contains("ConnectException") ||
               message.contains("SocketTimeoutException") ||
               message.contains("UnknownHostException") ||
               message.contains("network") ||
               errorClass.contains("Connect") ||
               errorClass.contains("Socket") ||
               errorClass.contains("Network")
    }
    
    private fun isRealmServiceError(message: String, errorClass: String): Boolean {
        return message.contains("BedrockRealmsService") ||
               message.contains("realms service") ||
               message.contains("Realms is not supported")
    }
    
    private fun isSessionError(message: String, errorClass: String): Boolean {
        return message.contains("session") ||
               message.contains("realmsXsts") ||
               message.contains("token")
    }
    
    private fun handleAuthenticationError(message: String): String {
        return when {
            message.contains("401") || message.contains("Unauthorized") -> 
                "Please sign in again to access your Realms"
            else -> 
                "Authentication failed. Please check your Microsoft account"
        }
    }
    
    private fun handleJsonParsingError(): String {
        return "Server response error. Please try again in a moment"
    }
    
    private fun handleNetworkError(): String {
        return "Network connection failed. Please check your internet connection"
    }
    
    private fun handleRealmServiceError(message: String): String {
        return when {
            message.contains("not supported") -> 
                "Realms service is temporarily unavailable"
            else -> 
                "Realms service error. Please try again later"
        }
    }
    
    private fun handleSessionError(): String {
        return "Session expired. Please sign in again"
    }
    
    private fun handleGenericError(message: String, fallbackMessage: String): String {
        return when {
            message.isBlank() -> fallbackMessage
            message.length > 100 -> "Service temporarily unavailable. Please try again"
            else -> fallbackMessage
        }
    }
    
    fun translateFetchError(throwable: Throwable?): String {
        return translateError(throwable, "Unable to load Realms. Please try again")
    }
    
    fun translateJoinError(throwable: Throwable?): String {
        return translateError(throwable, "Unable to join Realm. Please try again")
    }
}
