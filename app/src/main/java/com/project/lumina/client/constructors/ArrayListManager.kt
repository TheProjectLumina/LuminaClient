/*
 * © Project Lumina 2025 — Licensed under GNU GPLv3
 * You are free to use, modify, and redistribute this code under the terms
 * of the GNU General Public License v3. See the LICENSE file for details.
 *
 * ─────────────────────────────────────────────────────────────────────────────
 * This is open source — not open credit.
 * [Same philosophical message as original]
 * ─────────────────────────────────────────────────────────────────────────────
 *
 * GPLv3 Summary:
 * - You have the freedom to run, study, share, and modify this software.
 * - If you distribute modified versions, you must also share the source code.
 * - You must keep this license and copyright intact.
 * - You cannot apply further restrictions — the freedom stays with everyone.
 * - This license is irrevocable, and applies to all future redistributions.
 *
 * Full text: https://www.gnu.org/licenses/gpl-3.0.html
 */

package com.project.lumina.client.constructors

import android.content.Context
import android.media.SoundPool
import com.project.lumina.client.R
import com.project.lumina.client.util.translatedSelf

object ArrayListManager {
    private val enabledModules = mutableSetOf<String>()
    private var soundPool: SoundPool? = null

    
    enum class SoundSet {
        CELESTIAL,
        ALTERNATE,
        SPECIAL
    }

    
    data class SoundPair(val onId: Int, val offId: Int)

    
    private var soundPairs: MutableMap<SoundSet, SoundPair> = mutableMapOf()

    
    private var currentSoundSet: SoundSet = SoundSet.CELESTIAL

    
    private var soundEnabled: Boolean = true

    fun initializeSounds(context: Context) {
        soundPool = SoundPool.Builder()
            .setMaxStreams(3)
            .build().apply {
                
                soundPairs[SoundSet.CELESTIAL] = SoundPair(
                    load(context, R.raw.celestial_on, 1),
                    load(context, R.raw.celestial_off, 1)
                )
                soundPairs[SoundSet.ALTERNATE] = SoundPair(
                    load(context, R.raw.smooth_on, 1),
                    load(context, R.raw.smooth_off, 1)
                )
                soundPairs[SoundSet.SPECIAL] = SoundPair(
                    load(context, R.raw.nursultan_on, 1),
                    load(context, R.raw.nursultan_off, 1)
                )
            }
    }

    fun releaseSounds() {
        soundPool?.release()
        soundPool = null
        soundPairs.clear()
    }

    
    fun setSoundIds(set: SoundSet, onId: Int, offId: Int) {
        soundPairs[set] = SoundPair(onId, offId)
    }

    
    fun getSoundIds(set: SoundSet): SoundPair? {
        return soundPairs[set]
    }

    
    fun setCurrentSoundSet(set: SoundSet) {
        currentSoundSet = set
    }

    
    fun getCurrentSoundSet(): SoundSet {
        return currentSoundSet
    }

    
    fun setSoundEnabled(enabled: Boolean) {
        soundEnabled = enabled
    }

    
    fun isSoundEnabled(): Boolean {
        return soundEnabled
    }

    fun addModule(element: Element) {
        synchronized(enabledModules) {
            if (enabledModules.contains(element.name)) {
                return
            }
            try {
                if (soundEnabled) {
                    soundPairs[currentSoundSet]?.onId?.let { soundId ->
                        soundPool?.play(soundId, 1f, 1f, 1, 0, 1f)
                    }
                }
                enabledModules.add(element.name)
            } catch (e: Exception) {
                //Log.e("ModuleArrayListManager", "Failed to add item: ${element.name.translatedSelf}", e)
            }
        }
    }

    fun removeModule(element: Element) {
        synchronized(enabledModules) {
            try {
                if (soundEnabled) {
                    soundPairs[currentSoundSet]?.offId?.let { soundId ->
                        soundPool?.play(soundId, 1f, 1f, 1, 0, 1f)
                    }
                }
                enabledModules.remove(element.name)
            } catch (e: Exception) {
                //Log.e("ModuleArrayListManager", "Failed to remove item: ${element.name.translatedSelf}", e)
            }
        }
    }

    fun clear() {
        synchronized(enabledModules) {
            enabledModules.clear()
            try {
                if (soundEnabled) {
                    soundPairs[currentSoundSet]?.offId?.let { soundId ->
                        soundPool?.play(soundId, 1f, 1f, 1, 0, 1f)
                    }
                }
            } catch (e: Exception) {
                //Log.e("ModuleArrayListManager", "Failed to clear list", e)
            }
        }
    }

    fun getEnabledModules(): Set<String> {
        synchronized(enabledModules) {
            return enabledModules.toSet()
        }
    }
}