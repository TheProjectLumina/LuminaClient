/*
 * © Project Lumina 2025 — GPLv3 Licensed
 * You may use, modify, and share this code under the GPL.
 *
 * Just know: changing names and colors doesn't make you a developer.
 * Think before you fork. Build something real — or don't bother.
 */

package com.project.lumina.client.game.module.api.setting

import com.project.lumina.client.constructors.BoolValue
import com.project.lumina.client.constructors.Configurable
import kotlin.reflect.KProperty

class BooleanValue {
    private val value: BoolValue

    constructor(
        module: Configurable,
        name: String,
        defaultValue: Boolean
    ) {
        value = module.boolValue(name, defaultValue)
    }

    
    constructor(
        name: String,
        defaultValue: Boolean
    ) {
        
        
        value = (this as? Configurable)?.boolValue(name, defaultValue)
            ?: throw IllegalStateException("BooleanValue must be used within a Configurable class")
    }

    operator fun getValue(thisRef: Any?, property: KProperty<*>): Boolean {
        return value.value
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: Boolean) {
        this.value.value = value
    }
} 