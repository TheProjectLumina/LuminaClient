package com.project.lumina.client.util

import com.project.lumina.client.application.AppContext

object AssetManager {
    fun getAsset(name: String): Int {
        return AppContext.instance.resources.getIdentifier(name, "drawable", AppContext.instance.packageName)
            .takeIf { it != 0 } ?: error("Drawable resource $name not found")
    }

    fun getString(name: String): Int {
        return AppContext.instance.resources.getIdentifier(name, "string", AppContext.instance.packageName)
            .takeIf { it != 0 } ?: error("String resource $name not found")
    }
}
