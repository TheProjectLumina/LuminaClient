/*
 * © Project Lumina 2025 — GPLv3 Licensed
 * You may use, modify, and share this code under the GPL.
 *
 * Just know: changing names and colors doesn't make you a developer.
 * Think before you fork. Build something real — or don't bother.
 */

package com.project.lumina.client.game.module.api.setting

import com.project.lumina.client.constructors.Configurable
import kotlin.reflect.KProperty

class EffectSetting(
    private val module: Configurable,
    private val dataType: Any,
    private val effectId: Int
) {
    private var effectValue: Int = effectId

    operator fun getValue(thisRef: Any?, property: KProperty<*>): Int {
        return effectValue
    }
    
    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: Int) {
        effectValue = value
    }
}


object Effects {
    const val ABSORPTION = 22
    const val BAD_OMEN = 28
    const val BLINDNESS = 15
    const val CONDUIT_POWER = 26
    const val DARKNESS = 30
    const val FATAL_POISON = 25
    const val FIRE_RESISTANCE = 12
    const val HASTE = 3
    const val HEALTH_BOOST = 21
    const val HUNGER = 17
    const val INSTANT_DAMAGE = 7
    const val INSTANT_HEALTH = 6
    const val INVISIBILITY = 14
    const val JUMP_BOOST = 8
    const val LEVITATION = 24
    const val NAUSEA = 9
    const val NIGHT_VISION = 16
    const val POISON = 19
    const val REGENERATION = 10
    const val RESISTANCE = 11
    const val SATURATION = 23
    const val SLOW_FALLING = 27
    const val STRENGTH = 5
    const val SWIFTNESS = 1
    const val VILLAGE_HERO = 29
    const val WATER_BREATHING = 13
    const val WEAKNESS = 18
    const val WITHER = 20
} 