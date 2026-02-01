package com.project.lumina.client.game.module.impl.misc

import com.project.lumina.client.game.InterceptablePacket
import com.project.lumina.client.constructors.Element
import com.project.lumina.client.constructors.CheatCategory
import com.project.lumina.client.util.AssetManager
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket
import org.cloudburstmc.protocol.bedrock.packet.SetTimePacket

class TimeShiftElement : Element(
    name = "time_shift",
    category = CheatCategory.Misc,
    displayNameResId = AssetManager.getString("module_time_shift_display_name")
) {

    private val time by intValue("Time", 6000, 0..24000)
    private var lastTimeUpdate = 0L
    private var timeMultiplier by floatValue("Time Multiplier", 2f, 0.1f..10f)

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) {
            return
        }

        val packet = interceptablePacket.packet
        if (packet is PlayerAuthInputPacket) {
            val currentTime = System.currentTimeMillis()

            
            if (currentTime - lastTimeUpdate >= 100) {
                lastTimeUpdate = currentTime

                val timePacket = SetTimePacket()
                timePacket.time = time
                session.clientBound(timePacket)
            }
        }
    }

    override fun onDisabled() {
        super.onDisabled()
        if (isSessionCreated) {
            val timePacket = SetTimePacket()
            timePacket.time = 0
            session.clientBound(timePacket)
        }
    }
}
