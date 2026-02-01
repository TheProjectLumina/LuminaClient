package com.project.lumina.client.game.registry

import android.content.Context

class ItemMappingProvider(context: Context) : MappingProvider<ItemMapping>(context) {
    override val assetPath: String = "mcpedata/items"

    override fun readMapping(version: Short): ItemMapping {
        return ItemMapping.read(context, version)
    }
}
