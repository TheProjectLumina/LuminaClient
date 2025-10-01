/*
 * © Project Lumina 2025 — GPLv3 Licensed
 * You may use, modify, and share this code under the GPL.
 *
 * Just know: changing names and colors doesn't make you a developer.
 * Think before you fork. Build something real — or don't bother.
 */

package com.project.lumina.client.game.module.api.setting

import com.project.lumina.client.constructors.Configurable
import com.project.lumina.client.constructors.ListItem
import com.project.lumina.client.constructors.Element
import kotlin.reflect.KProperty

class StringItem(override val name: String) : ListItem


fun Element.stringValue(
    name: String,
    defaultValue: String,
    options: List<String>
): StringValueDelegate {
    val items = options.map { StringItem(it) }.toSet()
    val defaultItem = StringItem(defaultValue)
    return StringValueDelegate(this, name, defaultItem, items)
}


fun stringValue(
    module: Configurable,
    name: String,
    defaultValue: String,
    options: List<String>
): StringValueDelegate {
    val items = options.map { StringItem(it) }.toSet()
    val defaultItem = StringItem(defaultValue)
    return StringValueDelegate(module, name, defaultItem, items)
}

class StringValueDelegate(
    private val module: Configurable,
    private val name: String,
    private val defaultValue: StringItem,
    private val options: Set<StringItem>
) {
    private val value = module.listValue(name, defaultValue, options)

    operator fun getValue(thisRef: Any?, property: KProperty<*>): String {
        return (value.value as StringItem).name
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: String) {
        val item = options.find { it.name == value } ?: defaultValue
        this.value.value = item
    }
} 