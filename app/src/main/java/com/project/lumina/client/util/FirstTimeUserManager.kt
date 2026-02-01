package com.project.lumina.client.util

import android.content.Context
import android.content.SharedPreferences

object FirstTimeUserManager {
    private const val PREFS_NAME = "FirstTimeUserPrefs"
    private const val KEY_IS_FIRST_TIME = "isFirstTime"
    private const val KEY_AUTO_LAUNCH_MODE = "autoLaunchMode"
    private const val KEY_DIALOG_SHOWN = "dialogShown"
    
    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
    
    fun isFirstTimeUser(context: Context): Boolean {
        return getPreferences(context).getBoolean(KEY_IS_FIRST_TIME, true)
    }
    
    fun setFirstTimeUserComplete(context: Context) {
        getPreferences(context).edit()
            .putBoolean(KEY_IS_FIRST_TIME, false)
            .apply()
    }
    
    fun shouldShowDialog(context: Context): Boolean {
        val prefs = getPreferences(context)
        return prefs.getBoolean(KEY_IS_FIRST_TIME, true) && 
               !prefs.getBoolean(KEY_DIALOG_SHOWN, false)
    }
    
    fun setDialogShown(context: Context) {
        getPreferences(context).edit()
            .putBoolean(KEY_DIALOG_SHOWN, true)
            .apply()
    }
    
    fun setAutoLaunchMode(context: Context, mode: String) {
        getPreferences(context).edit()
            .putString(KEY_AUTO_LAUNCH_MODE, mode)
            .putBoolean(KEY_IS_FIRST_TIME, false)
            .putBoolean(KEY_DIALOG_SHOWN, true)
            .apply()
    }
    
    fun getAutoLaunchMode(context: Context): String? {
        return getPreferences(context).getString(KEY_AUTO_LAUNCH_MODE, null)
    }
    
    fun hasAutoLaunchMode(context: Context): Boolean {
        return getAutoLaunchMode(context) != null
    }
    
    fun clearAutoLaunchMode(context: Context) {
        getPreferences(context).edit()
            .remove(KEY_AUTO_LAUNCH_MODE)
            .apply()
    }
    
    fun resetFirstTimeUser(context: Context) {
        getPreferences(context).edit()
            .putBoolean(KEY_IS_FIRST_TIME, true)
            .putBoolean(KEY_DIALOG_SHOWN, false)
            .remove(KEY_AUTO_LAUNCH_MODE)
            .apply()
    }
}