package com.project.lumina.client.game.registry

import android.content.Context

class LegacyBlockMappingProvider(context: Context) : MappingProvider<LegacyBlockMapping>(context) {
    override val assetPath: String = "mcpedata/legacy_blocks"

    override fun readMapping(version: Short): LegacyBlockMapping {
        return LegacyBlockMapping.read(context, version)
    }
}
