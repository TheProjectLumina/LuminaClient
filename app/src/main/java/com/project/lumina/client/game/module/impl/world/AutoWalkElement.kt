package com.project.lumina.client.game.module.impl.world

import com.project.lumina.client.R
import com.project.lumina.client.game.InterceptablePacket
import com.project.lumina.client.constructors.Element
import com.project.lumina.client.constructors.CheatCategory
import org.cloudburstmc.math.vector.Vector3f
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket
import org.cloudburstmc.protocol.bedrock.packet.SetEntityMotionPacket
import kotlin.concurrent.timer
import kotlin.math.cos
import kotlin.math.sin

class AutoWalkElement : Element(
    name = "AutoWalk",
    category = CheatCategory.World,
    displayNameResId = R.string.module_auto_walk_display_name
) {

    private val speed = 0.5f  
    private var lastJumpTime = System.currentTimeMillis()  
    private var isJumping = false  
    private var disableYAxis = false  

    init {
        
        timer(period = 2000) {
            if (isEnabled) {  
                
                if (System.currentTimeMillis() - lastJumpTime >= 2000) {
                    lastJumpTime = System.currentTimeMillis()  
                    applyJump()  
                }
            }
        }
    }

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) {
            return
        }

        val packet = interceptablePacket.packet
        if (packet is PlayerAuthInputPacket) {  
            
            controlXZMovement(packet)

            
            if (!disableYAxis) {
                controlYMovement()  
            }
        }
    }

    
    private fun controlXZMovement(packet: PlayerAuthInputPacket) {
        
        val yaw = Math.toRadians(packet.rotation.y.toDouble())
            .toFloat()  
        val pitch =
            Math.toRadians(packet.rotation.x.toDouble()).toFloat()  

        
        val motionX = -sin(yaw) * cos(pitch) * speed  
        val motionZ = cos(yaw) * cos(pitch) * speed  

        
        val motionPacket = SetEntityMotionPacket().apply {
            runtimeEntityId = session.localPlayer.runtimeEntityId
            motion = Vector3f.from(
                motionX.toFloat(),
                0f,  
                motionZ.toFloat()
            )
        }
        session.clientBound(motionPacket)
    }

    
    private fun controlYMovement() {
        if (!isJumping) {  
            isJumping = true  

            
            val motionPacket = SetEntityMotionPacket().apply {
                runtimeEntityId = session.localPlayer.runtimeEntityId
                motion = Vector3f.from(0f, 1.5f, 0f)  
            }
            session.clientBound(motionPacket)

            
            disableYAxis = true

            
            timer(period = 1000, initialDelay = 1000) {
                disableYAxis = false  
            }
        }
    }

    
    private fun applyJump() {
        if (!isSessionCreated) {
            return
        }

        if (!isJumping) {  
            isJumping = true  

            
            val motionPacket = SetEntityMotionPacket().apply {
                runtimeEntityId = session.localPlayer.runtimeEntityId
                motion = Vector3f.from(0f, 1.5f, 0f)  
            }
            session.clientBound(motionPacket)

            
            disableYAxis = true

            
            timer(period = 1000, initialDelay = 1000) {
                disableYAxis = false  
            }
        }
    }
}