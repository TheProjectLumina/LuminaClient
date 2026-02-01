package com.project.lumina.client.game.module.impl.world

import android.util.Log
import com.project.lumina.client.constructors.CheatCategory
import com.project.lumina.client.constructors.Element
import com.project.lumina.client.game.InterceptablePacket
import com.project.lumina.client.util.AssetManager
import org.cloudburstmc.protocol.bedrock.data.entity.EntityEventType
import org.cloudburstmc.protocol.bedrock.packet.EntityEventPacket
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket

class AutoXPElement : Element(
    name = "AutoXP",
    category = CheatCategory.World,
    displayNameResId = AssetManager.getString("module_auto_xp_display_name")
) {
    
    private val intervalValue by intValue("Interval", 1000, 100..5000)
    private val xpAmountValue by intValue("XP Amount", 10, 1..1000)
    
    private var lastXPTime = 0L
    
    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) {
            return
        }
        
        val packet = interceptablePacket.packet
        if (packet is PlayerAuthInputPacket) {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastXPTime >= intervalValue) {
                lastXPTime = currentTime
                giveXP()
            }
        }
    }
    
    private fun giveXP() {
        try {
            sendEntityEvent()
            Log.d("AutoXPElement", "Sent XP using Entity Event method")
        } catch (e: Exception) {
            Log.e("AutoXPElement", "Error giving XP: ${e.message}")
        }
    }
    
    private fun sendEntityEvent() {
        val entityEventPacket = EntityEventPacket().apply {
            runtimeEntityId = session.localPlayer.runtimeEntityId
            type = EntityEventType.PLAYER_ADD_XP_LEVELS
            data = xpAmountValue
        }
        session.serverBound(entityEventPacket)
    }
}