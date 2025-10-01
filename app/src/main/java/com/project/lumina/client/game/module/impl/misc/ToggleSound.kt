package com.project.lumina.client.game.module.impl.misc

import com.project.lumina.client.constructors.ArrayListManager
import com.project.lumina.client.constructors.CheatCategory
import com.project.lumina.client.constructors.Element
import com.project.lumina.client.game.InterceptablePacket
import com.project.lumina.client.constructors.BoolValue
import com.project.lumina.client.util.AssetManager

class ToggleSound : Element(
    name = "ToggleSound",
    category = CheatCategory.Misc,
    displayNameResId = AssetManager.getString("module_togglesound"),
    iconResId = AssetManager.getAsset("ic_music")
) {
    private var celestial by boolValue("Celestial", true)
    private var nursultan by boolValue("Nursultan", false)
    private var smooth by boolValue("Smooth", false)

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) {
            session.toggleSounds(false)
            return
        }

        session.toggleSounds(true)

        when {
            celestial -> {
                disableOthers(except = 1)
                session.soundList(ArrayListManager.SoundSet.CELESTIAL)
            }

            nursultan -> {
                disableOthers(except = 2)
                session.soundList(ArrayListManager.SoundSet.ALTERNATE)
            }

            smooth -> {
                disableOthers(except = 3)
                session.soundList(ArrayListManager.SoundSet.SPECIAL)
            }
        }
    }

    private fun disableOthers(except: Int) {
        when (except) {
            1 -> {
                nursultan = false
                smooth = false
            }

            2 -> {
                celestial = false
                smooth = false
            }

            3 -> {
                celestial = false
                nursultan = false
            }
        }
    }
}