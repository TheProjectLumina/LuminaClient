package com.project.lumina.client.game.module.impl.motion

import com.project.lumina.client.R
import com.project.lumina.client.game.InterceptablePacket
import com.project.lumina.client.constructors.Element
import com.project.lumina.client.constructors.CheatCategory
import com.project.lumina.client.util.AssetManager
import org.cloudburstmc.math.vector.Vector3f
import org.cloudburstmc.protocol.bedrock.data.PlayerAuthInputData
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket
import org.cloudburstmc.protocol.bedrock.packet.SetEntityMotionPacket


class BhopElement(iconResId: Int = AssetManager.getAsset("ic_chevron_double_up_black_24dp")) : Element(
    name = "Bhop",
    category = CheatCategory.Motion,
    iconResId,
    displayNameResId = AssetManager.getString("module_bhop_display_name")
) {

    private val jumpHeight by floatValue("Jump Height", 0.42f, 0.4f..3.0f)
    private val motionInterval by intValue("Interval", 120, 50..2000)
    private val times by intValue("Time", 1, 1..20)
    private var lastMotionTime = 0L
    private var speed by floatValue("Speed", 0.5f, 0.1f..1f)

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {

        val packet = interceptablePacket.packet

        if (!isEnabled) {
            return
        }

        val currentTime = System.currentTimeMillis()

        
        if (currentTime - lastMotionTime >= motionInterval) {
            


            if (packet is PlayerAuthInputPacket) {

                if (packet.inputData.contains(PlayerAuthInputData.VERTICAL_COLLISION)) {


                    val motionPacket = SetEntityMotionPacket().apply {
                        runtimeEntityId = session.localPlayer.runtimeEntityId

                        
                        motion = Vector3f.from(
                            session.localPlayer.motionX,  
                            if ((currentTime / (motionInterval / times)) % 2 == 0L) jumpHeight else -jumpHeight,  
                            session.localPlayer.motionZ   
                        )
                    }


                    
                    session.clientBound(motionPacket)
                }
            }

            
            lastMotionTime = currentTime
        }
    }
}