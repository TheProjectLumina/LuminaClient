package com.project.lumina.client.util

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.TranslatableComponent

object TextComponentUtil {
    /**
     * Extracts plain text from a Component, handling various component types.
     *
     * @param component The input Component to sanitize.
     * @return The plain text representation of the Component, or an empty string if null/empty.
     */
    fun sanitize(component: Component?): String {
        if (component == null || component == Component.empty()) return ""

        return when (component) {
            is TextComponent -> component.content()
            is TranslatableComponent -> component.fallback() ?: component.key()
            else -> component.children().joinToString("") { sanitize(it) }
        }
    }
}